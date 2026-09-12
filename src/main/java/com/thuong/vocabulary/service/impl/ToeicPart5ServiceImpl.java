package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.ToeicPart5QuestionDTO;
import com.thuong.vocabulary.service.ToeicPart5Service;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ToeicPart5ServiceImpl implements ToeicPart5Service {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<ToeicPart5QuestionDTO> danhSachCauHoi = new ArrayList<>();
    private final Map<Integer, ToeicPart5QuestionDTO> bangCauHoi = new LinkedHashMap<>();

    @PostConstruct
    public void khoiTaoDuLieu() {
        try {
            ClassPathResource resource = new ClassPathResource("data/toeic_part5_test1.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    List<ToeicPart5QuestionDTO> list = objectMapper.readValue(is, new TypeReference<List<ToeicPart5QuestionDTO>>() {});
                    danhSachCauHoi.clear();
                    bangCauHoi.clear();
                    for (ToeicPart5QuestionDTO q : list) {
                        danhSachCauHoi.add(q);
                        bangCauHoi.put(q.getQuestionNumber(), q);
                    }
                    System.out.printf("[ToeicPart5Service] ✅ Đã nạp thành công %d câu hỏi TOEIC Part 5 (Test 1)%n", danhSachCauHoi.size());
                }
            } else {
                System.err.println("[ToeicPart5Service] ⚠️ Không tìm thấy file data/toeic_part5_test1.json");
            }
        } catch (Exception e) {
            System.err.println("[ToeicPart5Service] Lỗi khi nạp dữ liệu TOEIC Part 5: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<ToeicPart5QuestionDTO> layTatCaCauHoi() {
        return Collections.unmodifiableList(danhSachCauHoi);
    }

    @Override
    public ToeicPart5QuestionDTO layCauHoiTheoSo(int questionNumber) {
        return bangCauHoi.get(questionNumber);
    }

    @Override
    public Map<String, Object> kiemTraDapAn(int questionNumber, String dapAnChon) {
        ToeicPart5QuestionDTO q = bangCauHoi.get(questionNumber);
        Map<String, Object> ketQua = new LinkedHashMap<>();
        if (q == null) {
            ketQua.put("success", false);
            ketQua.put("message", "Không tìm thấy câu hỏi số " + questionNumber);
            return ketQua;
        }

        String userChoice = dapAnChon != null ? dapAnChon.trim().toUpperCase() : "";
        boolean isCorrect = q.getCorrectAnswer().equalsIgnoreCase(userChoice);

        ketQua.put("success", true);
        ketQua.put("questionNumber", questionNumber);
        ketQua.put("userChoice", userChoice);
        ketQua.put("correctAnswer", q.getCorrectAnswer());
        ketQua.put("isCorrect", isCorrect);
        ketQua.put("category", q.getCategory());
        ketQua.put("subCategory", q.getSubCategory());
        ketQua.put("questionType", q.getQuestionType());
        ketQua.put("congThuc", q.getCongThuc());
        ketQua.put("dauHieuNhanBiet", q.getDauHieuNhanBiet());
        ketQua.put("lyDoChonLoaiTu", q.getLyDoChonLoaiTu());
        ketQua.put("tuKhoaNguCanh", q.getTuKhoaNguCanh());
        ketQua.put("mindmapNode", q.getMindmapNode());
        ketQua.put("vietnameseTranslation", q.getVietnameseTranslation());
        ketQua.put("grammarBreakdown", q.getGrammarBreakdown());
        ketQua.put("explanation", q.getExplanation());
        ketQua.put("choicesAnalysis", q.getChoicesAnalysis());
        ketQua.put("quickTip", q.getQuickTip());

        return ketQua;
    }
}
