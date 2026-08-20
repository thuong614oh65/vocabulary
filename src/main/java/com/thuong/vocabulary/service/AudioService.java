package com.thuong.vocabulary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AudioService {

    // =========================================================
    // THƯ MỤC LƯU AUDIO
    // =========================================================

    private final Path audioDir;

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
            String pythonCommand
    ) {

        this.audioDir =
                Paths.get(audioStoragePath)
                        .toAbsolutePath()
                        .normalize();

        this.pythonCommand =
                xacDinhLenhPython(pythonCommand);

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
            if (exitCode != 0 || audioBytes.length == 0) {
                System.err.println("[AudioService] Lỗi khi stream audio cho văn bản (" + text.length() + " ký tự): exitCode=" + exitCode + ", bytes=" + audioBytes.length);
                return new byte[0];
            }

            return audioBytes;

        } catch (Exception e) {
            System.err.println("[AudioService] Ngoại lệ khi tạo stream audio: " + e.getMessage());
            return new byte[0];
        }
    }

    // =========================================================
    // TẠO TÊN FILE AN TOÀN
    // =========================================================

    private String taoTenFile(String tu) {

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