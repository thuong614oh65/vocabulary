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


}