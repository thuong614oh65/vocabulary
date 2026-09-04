package com.thuong.vocabulary.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.GeminiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

@Controller
public class ToeicPart2Controller {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private List<Map<String, Object>> deGocTest1 = new ArrayList<>();

    public ToeicPart2Controller(GeminiService geminiService, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
        loadDeGoc();
    }

    private void loadDeGoc() {
        try {
            ClassPathResource resource = new ClassPathResource("data/toeic_part2_test1.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    deGocTest1 = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
                    System.out.println("[ToeicPart2Controller] Đã nạp thành công " + deGocTest1.size() + " câu hỏi TOEIC Part 2 từ toeic_part2_test1.json");
                }
            } else {
                System.err.println("[ToeicPart2Controller] Không tìm thấy file data/toeic_part2_test1.json");
            }
        } catch (Exception e) {
            System.err.println("[ToeicPart2Controller] Lỗi khi đọc file toeic_part2_test1.json: " + e.getMessage());
        }
    }

    @GetMapping("/toeic-part2")
    public String trangToeicPart2(HttpSession session, Model model) {
        TaiKhoan nguoiDung = (TaiKhoan) session.getAttribute("nguoiDung");
        if (nguoiDung != null) {
            model.addAttribute("nguoiDung", nguoiDung);
        }
        model.addAttribute("tongSoCauGoc", deGocTest1.size());
        return "toeic-part2";
    }

    @GetMapping("/api/toeic-part2/de-goc")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> layDeGoc() {
        if (deGocTest1.isEmpty()) {
            loadDeGoc();
        }
        return ResponseEntity.ok(deGocTest1);
    }

    @PostMapping("/api/toeic-part2/tao-de-ai")
    @ResponseBody
    public ResponseEntity<?> taoDeAi(@RequestBody(required = false) Map<String, Object> body) {
        try {
            int soCau = 5;
            if (body != null && body.containsKey("soCau")) {
                try {
                    soCau = Integer.parseInt(body.get("soCau").toString());
                } catch (Exception ignored) {}
            }
            soCau = Math.max(3, Math.min(soCau, 20));

            String jsonRaw = geminiService.taoBoDeToeicPart2(soCau);
            if (jsonRaw == null || jsonRaw.isBlank()) {
                return ResponseEntity.internalServerError().body(Map.of("error", "AI không phản hồi câu hỏi nào."));
            }

            String clean = jsonRaw.trim();
            if (clean.startsWith("```json")) clean = clean.substring(7);
            else if (clean.startsWith("```")) clean = clean.substring(3);
            if (clean.endsWith("```")) clean = clean.substring(0, clean.length() - 3);
            clean = clean.trim();

            List<Map<String, Object>> danhSachCau = objectMapper.readValue(clean, new TypeReference<List<Map<String, Object>>>() {});
            return ResponseEntity.ok(danhSachCau);
        } catch (Exception e) {
            System.err.println("[ToeicPart2Controller] Lỗi tạo đề AI: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi tạo đề AI: " + e.getMessage()));
        }
    }
}