package com.thuong.vocabulary.dto;

import java.util.List;

public class PhanXaCauHoiDTO {

    private Long id;
    private String tiengAnh;
    private String tiengViet;
    private String phienAm;
    private String hinhAnhUrl;
    private String audioUrl;
    private String dapAnDung;
    private List<String> luaChon;
    private String loaiCauHoi; // "HINH_ANH_SANG_TU", "NGHE_SANG_NGHIA", "TU_SANG_NGHIA", "DUNG_SAI"
    private boolean cauDungSaiLaDung; // Cho chế độ Đúng/Sai

    public PhanXaCauHoiDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTiengAnh() {
        return tiengAnh;
    }

    public void setTiengAnh(String tiengAnh) {
        this.tiengAnh = tiengAnh;
    }

    public String getTiengViet() {
        return tiengViet;
    }

    public void setTiengViet(String tiengViet) {
        this.tiengViet = tiengViet;
    }

    public String getPhienAm() {
        return phienAm;
    }

    public void setPhienAm(String phienAm) {
        this.phienAm = phienAm;
    }

    public String getHinhAnhUrl() {
        return hinhAnhUrl;
    }

    public void setHinhAnhUrl(String hinhAnhUrl) {
        this.hinhAnhUrl = hinhAnhUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDapAnDung() {
        return dapAnDung;
    }

    public void setDapAnDung(String dapAnDung) {
        this.dapAnDung = dapAnDung;
    }

    public List<String> getLuaChon() {
        return luaChon;
    }

    public void setLuaChon(List<String> luaChon) {
        this.luaChon = luaChon;
    }

    public String getLoaiCauHoi() {
        return loaiCauHoi;
    }

    public void setLoaiCauHoi(String loaiCauHoi) {
        this.loaiCauHoi = loaiCauHoi;
    }

    public boolean isCauDungSaiLaDung() {
        return cauDungSaiLaDung;
    }

    public void setCauDungSaiLaDung(boolean cauDungSaiLaDung) {
        this.cauDungSaiLaDung = cauDungSaiLaDung;
    }
}
