package com.thuong.vocabulary.dto.luyende;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeThiQ79DTO {

    private String tieuDe;
    private String loaiNoiDung; // "IMAGE" | "TEXT"
    private String anhUrl;
    private String vanBanThongTin;
    private String tinhHuong;

    private String cauHoi1;
    private int thoiGianCau1; // 15
    private String goiYCau1;

    private String cauHoi2;
    private int thoiGianCau2; // 15
    private String goiYCau2;

    private String cauHoi3;
    private int thoiGianCau3; // 30
    private String goiYCau3;

    private String nguonGoc; // "UPLOAD", "VAN_BAN", "AI_TAO", "DE_MAU"
    private Integer deMauId;
}
