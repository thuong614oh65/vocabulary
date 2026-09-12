package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.ToeicPart5QuestionDTO;
import java.util.List;
import java.util.Map;

public interface ToeicPart5Service {
    List<ToeicPart5QuestionDTO> layTatCaCauHoi();
    ToeicPart5QuestionDTO layCauHoiTheoSo(int questionNumber);
    Map<String, Object> kiemTraDapAn(int questionNumber, String dapAnChon);
}
