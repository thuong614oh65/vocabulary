package com.thuong.vocabulary.dto;

import java.util.List;

public class HocDTO {

    private String kieuHoc;

    private Long boId;

    private Long[] tuIds;

    public String getKieuHoc() {
        return kieuHoc;
    }

    public void setKieuHoc(String kieuHoc) {
        this.kieuHoc = kieuHoc;
    }

    public Long getBoId() {
        return boId;
    }

    public void setBoId(Long boId) {
        this.boId = boId;
    }

    public Long[] getTuIds() {
        return tuIds;
    }

    public void setTuIds(Long[] tuIds) {
        this.tuIds = tuIds;
    }

    private String phamViBo = "TAT_CA";

    private Integer tuTu = 1;

    private Integer denTu;

    public String getPhamViBo() {
        return phamViBo;
    }

    public void setPhamViBo(String phamViBo) {
        this.phamViBo = phamViBo;
    }

    public Integer getTuTu() {
        return tuTu;
    }

    public void setTuTu(Integer tuTu) {
        this.tuTu = tuTu;
    }

    public Integer getDenTu() {
        return denTu;
    }

    public void setDenTu(Integer denTu) {
        this.denTu = denTu;
    }

    private String tenChuDe;

    private String chuDeTuJson;

    public String getTenChuDe() {
        return tenChuDe;
    }

    public void setTenChuDe(String tenChuDe) {
        this.tenChuDe = tenChuDe;
    }

    public String getChuDeTuJson() {
        return chuDeTuJson;
    }

    public void setChuDeTuJson(String chuDeTuJson) {
        this.chuDeTuJson = chuDeTuJson;
    }
}