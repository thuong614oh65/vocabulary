package com.thuong.vocabulary.service.impl;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.thuong.vocabulary.service.TrichXuatTuService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TrichXuatTuServiceImpl implements TrichXuatTuService {

    private final Client client;

    public TrichXuatTuServiceImpl() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            this.client = Client.builder().apiKey(apiKey).build();
        } else {
            this.client = null;
        }
    }

    @Override
    public List<String> trichXuatTu(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn một file hoặc ảnh hợp lệ!");
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        // Trường hợp file văn bản thuần (.txt, .csv)
        if (fileName.endsWith(".txt") || fileName.endsWith(".csv") || contentType.startsWith("text/")) {
            return trichXuatTuVanBan(file);
        }

        // Trường hợp ảnh hoặc tài liệu PDF cần AI/OCR
        if (contentType.startsWith("image/") || fileName.endsWith(".png") || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg") || fileName.endsWith(".webp") || fileName.endsWith(".pdf")
                || contentType.equals("application/pdf")) {

            if (client == null) {
                throw new IllegalStateException("GEMINI_API_KEY chưa được thiết lập để nhận diện ảnh/file.");
            }

            return trichXuatTuQuaGemini(file);
        }

        // Thử đọc dạng văn bản nếu là file khác
        return trichXuatTuVanBan(file);
    }

    private List<String> trichXuatTuQuaGemini(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank() || contentType.equals("application/octet-stream")) {
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else {
                contentType = "image/jpeg";
            }
        }

        String prompt = """
                Bạn là một trợ lý hỗ trợ học từ vựng tiếng Anh.
                Hãy trích xuất TẤT CẢ các từ vựng tiếng Anh (English words/phrases) xuất hiện trong hình ảnh hoặc tài liệu này.
                
                YÊU CẦU BẮT BUỘC:
                1. Mỗi dòng chỉ chứa đúng một từ hoặc cụm từ tiếng Anh nguyên thể.
                2. Bỏ qua số thứ tự, bullet points, dấu câu, ký tự đặc biệt, giải thích tiếng Việt.
                3. Loại bỏ các từ bị lặp lại.
                4. Không giải thích, không dịch sang tiếng Việt.
                5. Chỉ trả về danh sách từ tiếng Anh, mỗi từ trên một dòng.
                """;

        Part filePart = Part.fromBytes(bytes, contentType);
        Part promptPart = Part.fromText(prompt);

        Content content = Content.builder()
                .parts(List.of(filePart, promptPart))
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3.6-flash",
                content,
                null
        );

        String resultText = response.text();
        return parseDanhSachTu(resultText);
    }

    private List<String> trichXuatTuVanBan(MultipartFile file) throws Exception {
        Set<String> danhSach = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            Pattern wordPattern = Pattern.compile("^[a-zA-Z\\s\\-']+$");
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Nếu mỗi dòng là 1 từ hoặc cụm từ
                if (line.contains(",") || line.contains(";") || line.contains("\t")) {
                    String[] tokens = line.split("[,;\\t]+");
                    for (String token : tokens) {
                        token = token.trim();
                        if (!token.isEmpty() && wordPattern.matcher(token).matches()) {
                            danhSach.add(token.toLowerCase());
                        }
                    }
                } else if (wordPattern.matcher(line).matches()) {
                    danhSach.add(line.toLowerCase());
                } else {
                    // Nếu là đoạn văn bản, tách các từ
                    String[] words = line.split("[^a-zA-Z\\-]+");
                    for (String w : words) {
                        w = w.trim();
                        if (w.length() >= 2) {
                            danhSach.add(w.toLowerCase());
                        }
                    }
                }
            }
        }
        return new ArrayList<>(danhSach);
    }

    private List<String> parseDanhSachTu(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }

        Set<String> setTu = new LinkedHashSet<>();
        String[] lines = rawText.split("\\R");
        for (String line : lines) {
            String cleaned = line.trim()
                    .replaceAll("^[-*•0-9.]+\\s*", "") // Xóa số thứ tự, bullet points ở đầu dòng
                    .replaceAll("[^a-zA-Z\\s\\-']", "") // Giữ lại chữ cái tiếng Anh, dấu cách, gạch nối
                    .trim();

            if (!cleaned.isEmpty() && cleaned.length() >= 2) {
                setTu.add(cleaned.toLowerCase());
            }
        }
        return new ArrayList<>(setTu);
    }
}
