package com.thuong.vocabulary.service;

import com.thuong.vocabulary.repository.TuVungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AudioService {

    // =========================================================
    // LỚP CACHE ÂM THANH IN-MEMORY (RAM)
    // =========================================================
    public static class CachedAudio {
        private final byte[] data;
        private final long expireAt;
        private final boolean permanent;

        public CachedAudio(byte[] data, long expireAt, boolean permanent) {
            this.data = data;
            this.expireAt = expireAt;
            this.permanent = permanent;
        }

        public byte[] getData() { return data; }
        public boolean isPermanent() { return permanent; }
        public boolean isExpired() {
            return !permanent && System.currentTimeMillis() > expireAt;
        }
    }

    public static class AudioResult {
        private final byte[] data;
        private final boolean permanent;
        private final String filename;

        public AudioResult(byte[] data, boolean permanent, String filename) {
            this.data = data;
            this.permanent = permanent;
            this.filename = filename;
        }

        public byte[] getData() { return data; }
        public boolean isPermanent() { return permanent; }
        public String getFilename() { return filename; }
    }

    // =========================================================
    // BỘ ĐỆM RAM ĐA TẦNG CHO TOÀN HỆ THỐNG (SUB-MILLISECOND LATENCY)
    // =========================================================
    private final Map<String, CachedAudio> memoryCache = new ConcurrentHashMap<>();
    private static final long TEMP_CACHE_TTL_MS = 30 * 60 * 1000L; // Lưu tạm 30 phút

    // =========================================================
    // THƯ MỤC LƯU AUDIO
    // =========================================================

    private final Path audioDir;
    private final TuVungRepository tuVungRepository;

    // =========================================================
    // LỆNH PYTHON
    // =========================================================

    private final String pythonCommand;

    // =========================================================
    // FILE PYTHON ĐƯỢC COPY RA TỪ RESOURCES
    // =========================================================

    private final Path pythonFile;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AudioService(
            @Value("${audio.storage.path}")
            String audioStoragePath,

            @Value("${audio.python.command:python3}")
            String pythonCommand,

            @Autowired(required = false)
            TuVungRepository tuVungRepository
    ) {

        this.audioDir =
                Paths.get(audioStoragePath)
                        .toAbsolutePath()
                        .normalize();

        this.pythonCommand =
                xacDinhLenhPython(pythonCommand);

        this.tuVungRepository = tuVungRepository;

        try {

            this.pythonFile =
                    taoFilePython();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể chuẩn bị tao_audio.py",
                    e
            );
        }

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Audio storage: "
                        + audioDir
        );

        System.out.println(
                "Python command: "
                        + this.pythonCommand
        );

        System.out.println(
                "Python file: "
                        + pythonFile
        );

        System.out.println(
                "========================================"
        );
    }

    private String xacDinhLenhPython(String cauHinh) {
        java.util.List<String> ungVien = new java.util.ArrayList<>();
        if (cauHinh != null && !cauHinh.trim().isEmpty()) {
            ungVien.add(cauHinh.trim());
        }
        ungVien.add("/opt/venv/bin/python3");
        ungVien.add("/opt/venv/bin/python");
        ungVien.add("python3");
        ungVien.add("python");
        ungVien.add("py");

        for (String cmd : ungVien) {
            if (kiemTraLenhPython(cmd)) {
                System.out.println("[AudioService] Tìm thấy lệnh Python hoạt động: " + cmd);
                return cmd;
            }
        }

        System.err.println("[AudioService] Không thể xác định lệnh Python, mặc định dùng: "
                + (cauHinh != null && !cauHinh.isBlank() ? cauHinh : "python"));
        return (cauHinh != null && !cauHinh.isBlank()) ? cauHinh : "python";
    }

    private boolean kiemTraLenhPython(String cmd) {
        try {
            Process process = new ProcessBuilder(cmd, "--version").start();
            boolean xong = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
            return xong && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // COPY tao_audio.py TỪ RESOURCES RA FILE THẬT
    // =========================================================

    private Path taoFilePython()
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource(
                        "python/tao_audio.py"
                );

        if (!resource.exists()) {

            throw new IOException(
                    "Không tìm thấy python/tao_audio.py "
                            + "trong classpath"
            );
        }

        Path tempFile =
                Files.createTempFile(
                        "tao_audio-",
                        ".py"
                );

        try (
                InputStream inputStream =
                        resource.getInputStream()
        ) {

            Files.copy(
                    inputStream,
                    tempFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }

        tempFile.toFile().deleteOnExit();

        return tempFile;
    }

    // =========================================================
    // TẠO AUDIO CHO 1 TỪ
    // =========================================================

    public synchronized void taoAudio(String tu) {

        if (tu == null || tu.trim().isEmpty()) {
            return;
        }

        tu = tu.trim();

        try {

            // -------------------------------------------------
            // TẠO THƯ MỤC AUDIO
            // -------------------------------------------------

            Files.createDirectories(
                    audioDir
            );

            // -------------------------------------------------
            // TÊN FILE
            // -------------------------------------------------

            String tenFile =
                    taoTenFile(tu);

            Path audioFile =
                    audioDir.resolve(
                            tenFile + ".mp3"
                    );

            // -------------------------------------------------
            // CHỐNG FILE NẰM NGOÀI AUDIO DIRECTORY
            // -------------------------------------------------

            if (!audioFile.startsWith(audioDir)) {

                throw new RuntimeException(
                        "Đường dẫn audio không hợp lệ: "
                                + audioFile
                );
            }

            // -------------------------------------------------
            // NẾU FILE ĐÃ TỒN TẠI
            // -------------------------------------------------

            if (
                    Files.exists(audioFile)
                            &&
                            Files.size(audioFile) > 0
            ) {

                System.out.println(
                        "Audio đã tồn tại: "
                                + audioFile
                );

                return;
            }

            // -------------------------------------------------
            // TẠO AUDIO
            // -------------------------------------------------

            System.out.println(
                    "Đang tạo audio cho: "
                            + tu
            );

            // -------------------------------------------------
            // GỌI PYTHON
            // -------------------------------------------------

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            pythonCommand,

                            pythonFile.toAbsolutePath()
                                    .toString(),

                            tu,

                            audioDir.toAbsolutePath()
                                    .toString()
                    );

            // Cho Python chạy từ thư mục hiện tại
            processBuilder.directory(
                    audioDir.toFile()
            );

            // Gộp stderr vào stdout
            processBuilder.redirectErrorStream(
                    true
            );

            Process process =
                    processBuilder.start();

            // -------------------------------------------------
            // ĐỌC OUTPUT PYTHON
            // -------------------------------------------------

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                String line;

                while (
                        (line = reader.readLine())
                                != null
                ) {

                    System.out.println(
                            "[tao_audio.py] "
                                    + line
                    );
                }
            }

            // -------------------------------------------------
            // CHỜ PYTHON
            // -------------------------------------------------

            int exitCode =
                    process.waitFor();

            // -------------------------------------------------
            // KIỂM TRA EXIT CODE
            // -------------------------------------------------

            if (exitCode != 0) {

                throw new RuntimeException(
                        "Tạo audio thất bại cho từ: "
                                + tu
                                + " | exitCode="
                                + exitCode
                );
            }

            // -------------------------------------------------
            // KIỂM TRA FILE
            // -------------------------------------------------

            if (!Files.exists(audioFile)) {

                throw new RuntimeException(
                        "Python chạy xong nhưng "
                                + "không tìm thấy file MP3: "
                                + audioFile
                );
            }

            // -------------------------------------------------
            // KIỂM TRA 0 BYTE
            // -------------------------------------------------

            long fileSize =
                    Files.size(audioFile);

            if (fileSize == 0) {

                throw new RuntimeException(
                        "File MP3 được tạo nhưng "
                                + "có kích thước 0 byte: "
                                + audioFile
                );
            }

            // -------------------------------------------------
            // THÀNH CÔNG
            // -------------------------------------------------

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "Tạo audio thành công!"
            );

            System.out.println(
                    "Từ: "
                            + tu
            );

            System.out.println(
                    "File: "
                            + audioFile
            );

            System.out.println(
                    "Kích thước: "
                            + fileSize
                            + " bytes"
            );

            System.out.println(
                    "========================================"
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể tạo audio cho từ: "
                            + tu,
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Quá trình tạo audio bị gián đoạn: "
                            + tu,
                    e
            );
        }
    }

    // =========================================================
    // TẠO AUDIO DẠNG STREAM (KHÔNG LƯU VÀO ĐĨA)
    // =========================================================
    public byte[] taoAudioStream(String text, String rate) {
        if (text == null || text.trim().isEmpty()) {
            return new byte[0];
        }

        String rateParam = (rate != null && !rate.trim().isEmpty()) ? rate.trim() : "+0%";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    pythonFile.toAbsolutePath().toString(),
                    "--stream",
                    text.trim(),
                    rateParam
            );

            Process process = processBuilder.start();

            byte[] audioBytes;
            try (InputStream is = process.getInputStream()) {
                audioBytes = is.readAllBytes();
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && audioBytes.length > 0) {
                return audioBytes;
            }
            System.err.println("[AudioService] Python stream audio thất bại, chuyển sang Google TTS fallback.");
        } catch (Exception e) {
            System.err.println("[AudioService] Ngoại lệ khi tạo stream audio: " + e.getMessage() + " -> Fallback Google TTS.");
        }

        // Fast fallback sang Google Translate TTS để đảm bảo 100% luôn có âm thanh chuẩn
        return taoAudioGoogleTts(text.trim());
    }

    // =========================================================
    // DỰ PHÒNG SIÊU TỐC: GOOGLE TRANSLATE TTS TRỰC TIẾP QUA JAVA
    // =========================================================
    public byte[] taoAudioGoogleTts(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new byte[0];
        }
        try {
            String encoded = URLEncoder.encode(text.trim(), StandardCharsets.UTF_8);
            String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&tl=en-US&client=tw-ob&q=" + encoded;
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200 && response.body() != null && response.body().length > 0) {
                return response.body();
            }
        } catch (Exception e) {
            System.err.println("[AudioService] Google TTS error: " + e.getMessage());
        }
        return new byte[0];
    }

    // =========================================================
    // PHÁT ÂM THANH TOÀN HỆ THỐNG (CHUẨN BẢN XỨ + ĐA TẦNG CACHE)
    // 1. Kiểm tra CSDL: Có -> lấy file; chưa có -> tải lưu vĩnh viễn
    // 2. Ngoài CSDL (đoạn văn, câu, từ mới): tạo mp3 tạm, lưu RAM cache 30 phút
    // =========================================================
    public AudioResult phatAudioToanHeThong(String text, String rate) {
        if (text == null || text.trim().isEmpty()) {
            return new AudioResult(new byte[0], false, "empty.mp3");
        }

        String clean = text.trim();
        String rateParam = (rate != null && !rate.trim().isEmpty()) ? rate.trim() : "+0%";
        String cacheKey = taoCacheKey(clean, rateParam);

        // -----------------------------------------------------
        // LỚP 1: IN-MEMORY RAM CACHE (ĐỘ TRỄ < 1ms KHI NGHE LẠI)
        // -----------------------------------------------------
        CachedAudio cached = memoryCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return new AudioResult(cached.getData(), cached.isPermanent(), taoTenFile(clean) + ".mp3");
        }

        // -----------------------------------------------------
        // LỚP 2: PHÂN LOẠI: TỪ TRONG CSDL vs ĐOẠN VĂN / TỪ NGOÀI CSDL
        // -----------------------------------------------------
        String tenFile = taoTenFile(clean);
        Path audioFile = audioDir.resolve(tenFile + ".mp3");

        boolean laTuTrongCSDL = false;

        // A. Đã có file vĩnh viễn trên đĩa?
        if (Files.exists(audioFile)) {
            try {
                if (Files.size(audioFile) > 0) {
                    laTuTrongCSDL = true;
                }
            } catch (Exception ignored) {}
        }

        // B. Đã có trong static classpath?
        if (!laTuTrongCSDL) {
            ClassPathResource staticRes = new ClassPathResource("static/audio/tu-vung/" + tenFile + ".mp3");
            if (staticRes.exists()) {
                laTuTrongCSDL = true;
            }
        }

        // C. Là từ/cụm từ ngắn và tồn tại trong bảng tu_vung cơ sở dữ liệu?
        if (!laTuTrongCSDL && !clean.contains("\n") && clean.length() <= 50) {
            try {
                if (tuVungRepository != null && tuVungRepository.existsByTiengAnhTrimIgnoreCase(clean)) {
                    laTuTrongCSDL = true;
                }
            } catch (Exception e) {
                System.err.println("[AudioService] Lỗi kiểm tra tu_vung: " + e.getMessage());
            }
        }

        // -----------------------------------------------------
        // TRƯỜNG HỢP 1: TỪ CÓ TRONG CƠ SỞ DỮ LIỆU
        // "đọc từ ấy kiểm tra trong cơ sở dữ liệu nếu có thì lấy mp3 đó đọc.
        //  ko có thì tải mp3 chuẩn cho âm đó và lưu lại."
        // -----------------------------------------------------
        if (laTuTrongCSDL) {
            // 1. Nếu đã có file trên đĩa -> Đọc file & nạp RAM cache
            if (Files.exists(audioFile)) {
                try {
                    byte[] data = Files.readAllBytes(audioFile);
                    if (data.length > 0) {
                        memoryCache.put(cacheKey, new CachedAudio(data, Long.MAX_VALUE, true));
                        return new AudioResult(data, true, tenFile + ".mp3");
                    }
                } catch (Exception e) {
                    System.err.println("[AudioService] Lỗi đọc file MP3 đã có: " + e.getMessage());
                }
            }

            // 2. Nếu chưa có file -> Tải MP3 chuẩn và LƯU LẠI VĨNH VIỄN
            System.out.println("[AudioService] Từ có trong CSDL nhưng chưa có MP3, đang tải và lưu vĩnh viễn: " + clean);
            try {
                taoAudio(clean);
                if (Files.exists(audioFile) && Files.size(audioFile) > 0) {
                    byte[] data = Files.readAllBytes(audioFile);
                    memoryCache.put(cacheKey, new CachedAudio(data, Long.MAX_VALUE, true));
                    return new AudioResult(data, true, tenFile + ".mp3");
                }
            } catch (Exception e) {
                System.err.println("[AudioService] Lỗi khi tạo audio vĩnh viễn cho " + clean + ": " + e.getMessage());
            }

            // Fallback lưu file vĩnh viễn qua Google TTS nếu Python lỗi
            byte[] fallbackBytes = taoAudioGoogleTts(clean);
            if (fallbackBytes.length > 0) {
                try {
                    Files.createDirectories(audioDir);
                    Files.write(audioFile, fallbackBytes);
                    memoryCache.put(cacheKey, new CachedAudio(fallbackBytes, Long.MAX_VALUE, true));
                    return new AudioResult(fallbackBytes, true, tenFile + ".mp3");
                } catch (Exception ignored) {}
            }
        }

        // -----------------------------------------------------
        // TRƯỜNG HỢP 2: ĐOẠN VĂN / CÂU / TỪ NGOÀI CSDL
        // "từ ko có thì đưa về server và gửi mp3 chuẩn tạm thời từ đó để phát.
        //  đoạn văn hay tất cả những gì trừ từ trong cơ sở dữ liệu thì để gủi về server
        //  để mấy bản mp3 chuẩn tạm thời, lưu tạm trong vòng mây phút đó đê nghe lại
        //  ko phải tại lại quá nhiều."
        // -----------------------------------------------------
        byte[] mp3Data = taoAudioStream(clean, rateParam);
        if (mp3Data == null || mp3Data.length == 0) {
            mp3Data = taoAudioGoogleTts(clean);
        }

        if (mp3Data != null && mp3Data.length > 0) {
            // Lưu tạm thời 30 phút trong RAM (Temporary Cache)
            long expireAt = System.currentTimeMillis() + TEMP_CACHE_TTL_MS;
            memoryCache.put(cacheKey, new CachedAudio(mp3Data, expireAt, false));
            return new AudioResult(mp3Data, false, "speech.mp3");
        }

        return new AudioResult(new byte[0], false, "error.mp3");
    }

    private String taoCacheKey(String text, String rate) {
        String r = (rate != null && !rate.trim().isEmpty()) ? rate.trim() : "+0%";
        return text.trim().toLowerCase() + "|||" + r;
    }

    // =========================================================
    // DỌN DẸP BỘ NHỚ ĐỆM TẠM THỜI ĐỊNH KỲ (MỖI 5 PHÚT)
    // =========================================================
    @Scheduled(fixedRate = 300000)
    public void donDepCacheHetHan() {
        int truoc = memoryCache.size();
        memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int sau = memoryCache.size();
        if (truoc != sau) {
            System.out.printf("[AudioService] Đã dọn %d bản audio tạm hết hạn. Hiện còn %d audio trong RAM cache.%n", (truoc - sau), sau);
        }
    }

    // =========================================================
    // TẠO TÊN FILE AN TOÀN
    // =========================================================

    public String taoTenFile(String tu) {

        String tenFile =
                tu.toLowerCase()
                        .trim();

        tenFile =
                tenFile.replaceAll(
                        "[\\\\/:*?\"<>|]",
                        ""
                );

        tenFile =
                tenFile.replaceAll(
                        "\\s+",
                        "-"
                );

        return tenFile;
    }
}