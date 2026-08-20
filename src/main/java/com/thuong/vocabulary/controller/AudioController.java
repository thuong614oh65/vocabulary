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
    // PHÁT AUDIO TẠM THỜI QUA STREAM (KHÔNG LƯU VÀO ĐĨA)
    // =====================================================
    @PostMapping(value = "/tts", produces = "audio/mpeg")
    public ResponseEntity<byte[]> phatAudioTamThoiPost(
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

        byte[] mp3Data = audioService.taoAudioStream(textToSpeak.trim(), speechRate);
        if (mp3Data == null || mp3Data.length == 0) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(mp3Data);
    }

    @GetMapping(value = "/tts", produces = "audio/mpeg")
    public ResponseEntity<byte[]> phatAudioTamThoiGet(
            @RequestParam("text") String text,
            @RequestParam(required = false, defaultValue = "+0%") String rate
    ) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] mp3Data = audioService.taoAudioStream(text.trim(), rate);
        if (mp3Data == null || mp3Data.length == 0) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(mp3Data);
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