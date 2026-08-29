package com.thuong.vocabulary.dto.luyende;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaChamQ79DTO {

    private int tongDiem; // Thang 0 - 9 (chuẩn 3 câu TOEIC Q7-9)
    private String xepLoai; // Xuất sắc (8-9/9), Tốt (6-7/9), Khá (4-5/9), Cần cải thiện (0-3/9)
    private String nhanXetTongQuan;

    private List<DanhGiaCauHoiDTO> danhSachCauHoi;
}
