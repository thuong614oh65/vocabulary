package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.HuongDanDocDTO;

public interface HuongDanDocService {

    /**
     * Phân tích ngữ âm chi tiết cho từ vựng: tách âm tiết, bồi tiếng Việt chuẩn hóa,
     * hướng dẫn khẩu hình và mẹo phát âm theo chuẩn Google Dịch & ELSA Speak.
     *
     * @param tu       Từ tiếng Anh
     * @param phienAm  Phiên âm IPA (nếu có)
     * @param nghia    Nghĩa tiếng Việt (nếu có)
     * @return HuongDanDocDTO chứa toàn bộ dữ liệu hướng dẫn
     */
    HuongDanDocDTO layHuongDanDoc(String tu, String phienAm, String nghia);
}
