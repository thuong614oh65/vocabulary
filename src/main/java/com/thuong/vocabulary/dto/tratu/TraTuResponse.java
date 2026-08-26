package com.thuong.vocabulary.dto.tratu;

import java.util.ArrayList;
import java.util.List;

public class TraTuResponse {

    private boolean thanhCong = true;
    private String thongBaoLoi;

    private String tuGoc;
    private String loaiDich; // "EN_VI" hoặc "VI_EN"
    private String ngonNguNguon; // "en" hoặc "vi"
    private String ngonNguDich;  // "vi" hoặc "en"

    private String banDich;
    private String phienAm;
    private String tuLoai; // Noun, Verb, Adjective, Phrase, Sentence...

    private String giaiThich; // Giải thích ngắn gọn hoặc ngữ cảnh sử dụng
    private List<String> cacNghiaKhac = new ArrayList<>(); // Các cách dịch khác
    private List<DinhNghiaItem> dinhNghia = new ArrayList<>();
    private List<ViDuItem> viDu = new ArrayList<>();
    private List<String> dongNghia = new ArrayList<>();
    private List<String> traiNghia = new ArrayList<>();
    private String audioUrl; // URL âm thanh US/UK nếu có

    public TraTuResponse() {
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getThongBaoLoi() {
        return thongBaoLoi;
    }

    public void setThongBaoLoi(String thongBaoLoi) {
        this.thongBaoLoi = thongBaoLoi;
    }

    public String getTuGoc() {
        return tuGoc;
    }

    public void setTuGoc(String tuGoc) {
        this.tuGoc = tuGoc;
    }

    public String getLoaiDich() {
        return loaiDich;
    }

    public void setLoaiDich(String loaiDich) {
        this.loaiDich = loaiDich;
    }

    public String getNgonNguNguon() {
        return ngonNguNguon;
    }

    public void setNgonNguNguon(String ngonNguNguon) {
        this.ngonNguNguon = ngonNguNguon;
    }

    public String getNgonNguDich() {
        return ngonNguDich;
    }

    public void setNgonNguDich(String ngonNguDich) {
        this.ngonNguDich = ngonNguDich;
    }

    public String getBanDich() {
        return banDich;
    }

    public void setBanDich(String banDich) {
        this.banDich = banDich;
    }

    public String getPhienAm() {
        return phienAm;
    }

    public void setPhienAm(String phienAm) {
        this.phienAm = phienAm;
    }

    public String getTuLoai() {
        return tuLoai;
    }

    public void setTuLoai(String tuLoai) {
        this.tuLoai = tuLoai;
    }

    public String getGiaiThich() {
        return giaiThich;
    }

    public void setGiaiThich(String giaiThich) {
        this.giaiThich = giaiThich;
    }

    public List<String> getCacNghiaKhac() {
        return cacNghiaKhac;
    }

    public void setCacNghiaKhac(List<String> cacNghiaKhac) {
        this.cacNghiaKhac = cacNghiaKhac;
    }

    public List<DinhNghiaItem> getDinhNghia() {
        return dinhNghia;
    }

    public void setDinhNghia(List<DinhNghiaItem> dinhNghia) {
        this.dinhNghia = dinhNghia;
    }

    public List<ViDuItem> getViDu() {
        return viDu;
    }

    public void setViDu(List<ViDuItem> viDu) {
        this.viDu = viDu;
    }

    public List<String> getDongNghia() {
        return dongNghia;
    }

    public void setDongNghia(List<String> dongNghia) {
        this.dongNghia = dongNghia;
    }

    public List<String> getTraiNghia() {
        return traiNghia;
    }

    public void setTraiNghia(List<String> traiNghia) {
        this.traiNghia = traiNghia;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
