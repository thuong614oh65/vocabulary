package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.ToeicPart5QuestionDTO;
import com.thuong.vocabulary.service.ToeicPart5Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ToeicPart5Controller {

    private final ToeicPart5Service toeicPart5Service;

    public ToeicPart5Controller(ToeicPart5Service toeicPart5Service) {
        this.toeicPart5Service = toeicPart5Service;
    }

    @GetMapping("/toeic-part-5")
    public String trangLuyenPart5(Model model) {
        model.addAttribute("totalQuestions", toeicPart5Service.layTatCaCauHoi().size());
        return "toeic-part5";
    }

    @ResponseBody
    @GetMapping("/api/toeic-part-5/questions")
    public List<ToeicPart5QuestionDTO> layTatCaCauHoi() {
        return toeicPart5Service.layTatCaCauHoi();
    }

    @ResponseBody
    @GetMapping("/api/toeic-part-5/question/{qNum}")
    public ToeicPart5QuestionDTO layChiTietCauHoi(@PathVariable int qNum) {
        return toeicPart5Service.layCauHoiTheoSo(qNum);
    }

    @ResponseBody
    @PostMapping("/api/toeic-part-5/check")
    public Map<String, Object> kiemTraDapAn(
            @RequestParam int questionNumber,
            @RequestParam String answer) {
        return toeicPart5Service.kiemTraDapAn(questionNumber, answer);
    }
}
