package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.ToeicPart5QuestionDTO;
import com.thuong.vocabulary.service.impl.ToeicPart5ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ToeicPart5ServiceTest {

    private ToeicPart5ServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ToeicPart5ServiceImpl();
        service.khoiTaoDuLieu();
    }

    @Test
    void testLoadQuestionsCountAndFields() {
        List<ToeicPart5QuestionDTO> questions = service.layTatCaCauHoi();
        assertNotNull(questions);
        assertEquals(30, questions.size(), "Should have exactly 30 questions");

        Map<String, Long> countByType = questions.stream()
                .collect(Collectors.groupingBy(ToeicPart5QuestionDTO::getQuestionType, Collectors.counting()));

        System.out.println("Question type counts: " + countByType);
        assertTrue(countByType.containsKey("CAU_TRUC"), "Must have CAU_TRUC questions");
        assertTrue(countByType.containsKey("LOAI_TU"), "Must have LOAI_TU questions");
        assertTrue(countByType.containsKey("NGHIA"), "Must have NGHIA questions");

        for (ToeicPart5QuestionDTO q : questions) {
            assertTrue(q.getQuestionNumber() >= 101 && q.getQuestionNumber() <= 130);
            assertNotNull(q.getQuestionText(), "Question text missing for " + q.getQuestionNumber());
            assertNotNull(q.getChoiceA(), "Choice A missing for " + q.getQuestionNumber());
            assertNotNull(q.getChoiceB(), "Choice B missing for " + q.getQuestionNumber());
            assertNotNull(q.getChoiceC(), "Choice C missing for " + q.getQuestionNumber());
            assertNotNull(q.getChoiceD(), "Choice D missing for " + q.getQuestionNumber());
            assertNotNull(q.getQuestionType(), "questionType missing for " + q.getQuestionNumber());

            if ("CAU_TRUC".equals(q.getQuestionType())) {
                assertNotNull(q.getCongThuc(), "congThuc missing for CAU_TRUC: " + q.getQuestionNumber());
                assertNotNull(q.getDauHieuNhanBiet(), "dauHieuNhanBiet missing for CAU_TRUC: " + q.getQuestionNumber());
            } else if ("LOAI_TU".equals(q.getQuestionType())) {
                assertNotNull(q.getLyDoChonLoaiTu(), "lyDoChonLoaiTu missing for LOAI_TU: " + q.getQuestionNumber());
                assertNotNull(q.getDauHieuNhanBiet(), "dauHieuNhanBiet missing for LOAI_TU: " + q.getQuestionNumber());
            } else if ("NGHIA".equals(q.getQuestionType())) {
                assertNotNull(q.getTuKhoaNguCanh(), "tuKhoaNguCanh missing for NGHIA: " + q.getQuestionNumber());
            }

            Map<String, Object> result = service.kiemTraDapAn(q.getQuestionNumber(), "A");
            assertNotNull(result, "Result should not be null for " + q.getQuestionNumber());
            assertEquals(true, result.get("success"));
            assertNotNull(result.get("correctAnswer"));
            assertNotNull(result.get("vietnameseTranslation"));
            assertNotNull(result.get("questionType"));
            assertNotNull(result.get("choicesAnalysis"));
            if ("CAU_TRUC".equals(result.get("questionType")) || "LOAI_TU".equals(result.get("questionType"))) {
                assertNotNull(result.get("dauHieuNhanBiet"));
            } else if ("NGHIA".equals(result.get("questionType"))) {
                assertNotNull(result.get("tuKhoaNguCanh"));
            }
        }
    }
}
