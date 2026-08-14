package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.service.AudioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // PHÁT AUDIO TỪ VỰNG
    // =====================================================

    @GetMapping("/tu-vung/{tenFile}")
    public ResponseEntity<Resource> ngheAudio(
            @PathVariable String tenFile
    ) {

        // Chống truy cập đường dẫn ngoài thư mục audio
        if (
                tenFile.contains("..")
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


        FileSystemResource resource =
                new FileSystemResource(
                        audioFile
                );


        if (!resource.exists()) {

            return ResponseEntity.notFound()
                    .build();
        }


        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + tenFile + "\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "audio/mpeg"
                        )
                )
                .body(resource);
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