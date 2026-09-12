package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.service.AudioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/audio")
public class AudioController {

    private final Path audioDir;

    private final AudioService audioService;

    public AudioController(
            @Value("${audio.storage.path}")
            String audioStoragePath,
            AudioService audioService
    ) {

        this.audioDir =
                Paths.get(audioStoragePath)
                        .toAbsolutePath()
                        .normalize();

        this.audioService = audioService;
    }


    // =====================================================
    // PHÁT AUDIO TỪ VỰNG (TỰ ĐỘNG TẠO NẾU CHƯA CÓ)
    // =====================================================

    @GetMapping("/tu-vung/{tenFile}")
    public ResponseEntity<Resource> ngheAudio(
            @PathVariable String tenFile
    ) {

        // Chống truy cập đường dẫn ngoài thư mục audio
        if (
                tenFile == null
                        || tenFile.contains("..")
                        || tenFile.contains("/")
                        || tenFile.contains("\\")
        ) {

            return ResponseEntity.badRequest()
                    .build();
        }


        Path audioFile =
                audioDir
                        .resolve(tenFile)
                        .normalize();


        // Đảm bảo file nằm trong audioDir
        if (
                !audioFile.startsWith(audioDir)
        ) {

            return ResponseEntity.badRequest()
                    .build();
        }


        // 1. Nếu file đã tồn tại trên đĩa và có kích thước > 0
        if (Files.exists(audioFile)) {
            try {
                if (Files.size(audioFile) > 0) {
                    return phatFileMp3(new FileSystemResource(audioFile), tenFile);
                }
            } catch (Exception ignored) {}
        }


        // 2. Thử tìm trong ClassPath static (nếu có sẵn từ project đóng gói)
        ClassPathResource staticResource =
                new ClassPathResource("static/audio/tu-vung/" + tenFile);
        if (staticResource.exists()) {
            return phatFileMp3(staticResource, tenFile);
        }


        // 3. Tự động tạo audio on-demand nếu chưa có (rất quan trọng trên Railway)
        try {
            String tenTu = tenFile;
            if (tenTu.toLowerCase().endsWith(".mp3")) {
                tenTu = tenTu.substring(0, tenTu.length() - 4);
            }
            String tuCanTao = tenTu.replace("-", " ").trim();

            if (!tuCanTao.isEmpty()) {
                System.out.println("[AudioController] Chưa có file " + tenFile + ", đang tự động tạo on-demand cho từ: " + tuCanTao);
                audioService.taoAudio(tuCanTao);
            }
        } catch (Exception e) {
            System.err.println("[AudioController] Lỗi khi tạo audio on-demand cho " + tenFile + ": " + e.getMessage());
        }


        // 4. Kiểm tra lại sau khi tạo
        if (Files.exists(audioFile)) {
            try {
                if (Files.size(audioFile) > 0) {
                    return phatFileMp3(new FileSystemResource(audioFile), tenFile);
                }
            } catch (Exception ignored) {}
        }


        return ResponseEntity.notFound()
                .build();
    }

    private ResponseEntity<Resource> phatFileMp3(Resource resource, String tenFile) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + tenFile + "\""
                )
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "public, max-age=86400"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "audio/mpeg"
                        )
                )
                .body(resource);
    }


    // =====================================================
    // ENDPOINT TOÀN HỆ THỐNG: PHÁT AUDIO CHUẨN ĐA TẦNG CACHE
    // 1. Kiểm tra CSDL: nếu có -> lấy mp3; chưa có -> tải lưu vĩnh viễn
    // 2. Ngoài CSDL / đoạn văn: tạo mp3 tạm, lưu RAM cache 30 phút
    // =====================================================
    @GetMapping(value = "/phat", produces = "audio/mpeg")
    public ResponseEntity<byte[]> phatAudioGet(
            @RequestParam("text") String text,
            @RequestParam(required = false, defaultValue = "+0%") String rate
    ) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AudioService.AudioResult result = audioService.phatAudioToanHeThong(text.trim(), rate);
        if (result == null || result.getData() == null || result.getData().length == 0) {
            return ResponseEntity.internalServerError().build();
        }

        String cacheControl = result.isPermanent()
                ? "public, max-age=86400, stale-while-revalidate=604800"
                : "public, max-age=1800, stale-while-revalidate=3600";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + result.getFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, cacheControl)
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(result.getData());
    }

    @PostMapping(value = "/phat", produces = "audio/mpeg")
    public ResponseEntity<byte[]> phatAudioPost(
            @RequestBody(required = false) java.util.Map<String, String> body,
            @RequestParam(required = false) String text,
            @RequestParam(required = false, defaultValue = "+0%") String rate
    ) {
        String textToSpeak = text;
        String speechRate = rate;

        if (body != null) {
            if (body.containsKey("text") && body.get("text") != null) {
                textToSpeak = body.get("text");
            }
            if (body.containsKey("rate") && body.get("rate") != null) {
                speechRate = body.get("rate");
            }
        }

        if (textToSpeak == null || textToSpeak.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AudioService.AudioResult result = audioService.phatAudioToanHeThong(textToSpeak.trim(), speechRate);
        if (result == null || result.getData() == null || result.getData().length == 0) {
            return ResponseEntity.internalServerError().build();
        }

        String cacheControl = result.isPermanent()
                ? "public, max-age=86400, stale-while-revalidate=604800"
                : "public, max-age=1800, stale-while-revalidate=3600";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + result.getFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, cacheControl)
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(result.getData());
    }

    // =====================================================
    // ENDPOINT TƯƠNG THÍCH CŨ: /audio/tts (ĐƯỢC NÂNG CẤP CACHE 30 PHÚT)
    // =====================================================
    @PostMapping(value = "/tts", produces = "audio/mpeg")
    public ResponseEntity<byte[]> phatAudioTamThoiPost(
            @RequestBody(required = false) java.util.Map<String, String> body,
            @RequestParam(required = false) String text,
            @RequestParam(required = false, defaultValue = "+0%") String rate
    ) {
        return phatAudioPost(body, text, rate);
    }

    @GetMapping(value = "/tts", produces = "audio/mpeg")
    public ResponseEntity<byte[]> phatAudioTamThoiGet(
            @RequestParam("text") String text,
            @RequestParam(required = false, defaultValue = "+0%") String rate
    ) {
        return phatAudioGet(text, rate);
    }

    // =====================================================
    // TEST TẠO AUDIO
    // =====================================================

    @GetMapping("/test/{tu}")
    public ResponseEntity<String> testAudio(
            @PathVariable String tu
    ) {

        try {

            audioService.taoAudio(tu);

            return ResponseEntity.ok(
                    "Tạo audio thành công: " + tu
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            "Lỗi tạo audio: "
                                    + e.getMessage()
                    );
        }
    }
}