package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.service.GeminiService;
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

    private final GeminiService geminiService;

    public TrichXuatTuServiceImpl(GeminiService geminiService) {
        this.geminiService = geminiService;
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

            byte[] bytes = file.getBytes();
            return geminiService.trichXuatTuTuHinhAnh(bytes, contentType);
        }

        // Thử đọc dạng văn bản nếu là file khác
        return trichXuatTuVanBan(file);
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
}
