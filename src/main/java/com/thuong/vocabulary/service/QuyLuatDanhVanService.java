package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.QuyLuatDanhVanDTO;
import java.util.List;

public interface QuyLuatDanhVanService {

    /**
     * Lấy toàn bộ danh sách quy luật đánh vần tiếng Anh có sẵn trong thư viện.
     */
    List<QuyLuatDanhVanDTO> layTatCaQuyLuat();

    /**
     * Lấy chi tiết 1 quy luật đánh vần theo ID (ví dụ: "w_or", "ise", "ar", "tion"...).
     */
    QuyLuatDanhVanDTO layTheoId(String id);

    /**
     * Tìm kiếm quy luật theo từ vựng hoặc cụm chữ cái (ví dụ: "world", "ise", "or"...).
     */
    List<QuyLuatDanhVanDTO> timKiemQuyLuat(String tuKhoa);

    /**
     * Gọi AI Gemini tự động phân tích và tạo sơ đồ tư duy quy tắc đánh vần cho BẤT KỲ âm hoặc từ nào.
     */
    QuyLuatDanhVanDTO taoSoDoTuAI(String tuHoacAm);
}
