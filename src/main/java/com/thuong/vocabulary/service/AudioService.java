package com.thuong.vocabulary.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AudioService {

    // =========================================================
    // THƯ MỤC GỐC CỦA PROJECT
    // =========================================================

    private final Path projectDir =
            Paths.get(
                    System.getProperty("user.dir")
            );


    // =========================================================
    // FILE PYTHON
    // =========================================================

    private final Path pythonFile =
            projectDir.resolve(
                    "tao_audio.py"
            );


    // =========================================================
    // THƯ MỤC AUDIO TỪ VỰNG
    // =========================================================

    private final Path audioDir =
            projectDir.resolve(
                    Paths.get(
                            "src",
                            "main",
                            "resources",
                            "static",
                            "audio",
                            "tu-vung"
                    )
            );

    // =========================================================
    // TẠO AUDIO CHO 1 TỪ
    // =========================================================

    public void taoAudio(String tu) {

        if (tu == null || tu.trim().isEmpty()) {
            return;
        }


        tu = tu.trim();


        try {

            // -------------------------------------------------
            // Kiểm tra file Python
            // -------------------------------------------------

            if (!Files.exists(pythonFile)) {

                throw new RuntimeException(
                        "Không tìm thấy tao_audio.py tại: "
                                + pythonFile.toAbsolutePath()
                );
            }


            // -------------------------------------------------
            // Tạo thư mục audio nếu chưa có
            // -------------------------------------------------

            Files.createDirectories(
                    audioDir
            );


            // -------------------------------------------------
            // Tạo tên file MP3
            // -------------------------------------------------

            String tenFile =
                    taoTenFile(tu);


            Path audioFile =
                    audioDir.resolve(
                            tenFile + ".mp3"
                    );


            // -------------------------------------------------
            // Nếu MP3 đã tồn tại
            // thì không tạo lại
            // -------------------------------------------------

            if (Files.exists(audioFile)) {

                System.out.println(
                        "Audio đã tồn tại: "
                                + audioFile.toAbsolutePath()
                );

                return;
            }


            // -------------------------------------------------
            // Gọi Python
            //
            // python tao_audio.py apple
            // -------------------------------------------------

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "python",
                            pythonFile.toAbsolutePath().toString(),
                            tu
                    );


            processBuilder
                    .directory(
                            projectDir.toFile()
                    );


            processBuilder.redirectErrorStream(
                    true
            );


            Process process =
                    processBuilder.start();


            // -------------------------------------------------
            // Đọc kết quả từ Python
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

                while ((line = reader.readLine()) != null) {

                    System.out.println(
                            "[tao_audio.py] "
                                    + line
                    );
                }
            }


            // -------------------------------------------------
            // Chờ Python chạy xong
            // -------------------------------------------------

            int exitCode =
                    process.waitFor();


            // -------------------------------------------------
            // Kiểm tra kết quả
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
            // Kiểm tra file MP3 có thật sự được tạo
            // -------------------------------------------------

            if (!Files.exists(audioFile)) {

                throw new RuntimeException(
                        "Python chạy xong nhưng không tìm thấy file MP3: "
                                + audioFile.toAbsolutePath()
                );
            }


            System.out.println(
                    "Tạo audio thành công: "
                            + audioFile.toAbsolutePath()
            );


        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể chạy tao_audio.py cho từ: "
                            + tu,
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Quá trình tạo audio bị gián đoạn cho từ: "
                            + tu,
                    e
            );
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