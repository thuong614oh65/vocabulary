package com.thuong.vocabulary.dto.luyende;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChamDiemQ79Request {

    private String tieuDe;
    private String loaiNoiDung;
    private String thongTinDeBai;
    private String tinhHuong;

    private String cauHoi1;
    private String cauTraLoi1;

    private String cauHoi2;
    private String cauTraLoi2;

    private String cauHoi3;
    private String cauTraLoi3;
}
