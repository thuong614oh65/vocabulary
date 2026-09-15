package com.thuong.vocabulary.dto;

public class BuocDanhVanDTO {

    private String amTiet;        // Chữ cái âm tiết (vd: "hel", "lo", "cat", "tab", "le")
    private String ipa;           // Phiên âm IPA âm tiết (vd: "/hə/", "/loʊ/", "/kæt/", "/teɪ/", "/bəl/")
    private String phuAmDau;      // Phụ âm đầu nếu có (vd: "/h/" - "hờ", "/k/" - "cờ", "/t/" - "tờ")
    private String phuAmDauDoc;   // Tên đọc tiếng Việt của phụ âm đầu (vd: "hờ", "cờ", "tờ", "lờ", "bờ")
    private String nguyenAm;      // Nguyên âm hạt nhân (vd: "/ə/", "/æ/", "/oʊ/", "/eɪ/", "/aɪ/")
    private String nguyenAmDoc;   // Tên đọc tiếng Việt của nguyên âm (vd: "ơ/ê", "e bẹt", "âu/ô", "ây", "ai")
    private String amCuoi;        // Phụ âm cuối nếu có (vd: "/t/", "/d/", "/k/", "/s/")
    private String amCuoiDoc;     // Tên đọc phụ âm cuối (vd: "tờ", "đờ", "cờ", "xì")
    private String vanGhep;       // Vần ghép (vd: "/æt/" - "át", "/aɪt/" - "ait")
    private String cachDanhVan;   // Chuỗi hiển thị đánh vần (vd: "hờ + ơ ➔ hơ", "cờ + át ➔ cát", "tờ + ây ➔ TÂY")
    private String amTietDoc;     // Từ để TTS đọc chuẩn âm này
    private String quyTacLienQuan;// Ghi chú quy tắc mặt chữ (nếu có)
    private boolean laTrongAm;    // Có mang trọng âm chính không

    public BuocDanhVanDTO() {
    }

    public BuocDanhVanDTO(String amTiet, String ipa, String phuAmDau, String phuAmDauDoc,
                          String nguyenAm, String nguyenAmDoc, String amCuoi, String amCuoiDoc,
                          String vanGhep, String cachDanhVan, String amTietDoc,
                          String quyTacLienQuan, boolean laTrongAm) {
        this.amTiet = amTiet;
        this.ipa = ipa;
        this.phuAmDau = phuAmDau;
        this.phuAmDauDoc = phuAmDauDoc;
        this.nguyenAm = nguyenAm;
        this.nguyenAmDoc = nguyenAmDoc;
        this.amCuoi = amCuoi;
        this.amCuoiDoc = amCuoiDoc;
        this.vanGhep = vanGhep;
        this.cachDanhVan = cachDanhVan;
        this.amTietDoc = amTietDoc;
        this.quyTacLienQuan = quyTacLienQuan;
        this.laTrongAm = laTrongAm;
    }

    public String getAmTiet() {
        return amTiet;
    }

    public void setAmTiet(String amTiet) {
        this.amTiet = amTiet;
    }

    public String getIpa() {
        return ipa;
    }

    public void setIpa(String ipa) {
        this.ipa = ipa;
    }

    public String getPhuAmDau() {
        return phuAmDau;
    }

    public void setPhuAmDau(String phuAmDau) {
        this.phuAmDau = phuAmDau;
    }

    public String getPhuAmDauDoc() {
        return phuAmDauDoc;
    }

    public void setPhuAmDauDoc(String phuAmDauDoc) {
        this.phuAmDauDoc = phuAmDauDoc;
    }

    public String getNguyenAm() {
        return nguyenAm;
    }

    public void setNguyenAm(String nguyenAm) {
        this.nguyenAm = nguyenAm;
    }

    public String getNguyenAmDoc() {
        return nguyenAmDoc;
    }

    public void setNguyenAmDoc(String nguyenAmDoc) {
        this.nguyenAmDoc = nguyenAmDoc;
    }

    public String getAmCuoi() {
        return amCuoi;
    }

    public void setAmCuoi(String amCuoi) {
        this.amCuoi = amCuoi;
    }

    public String getAmCuoiDoc() {
        return amCuoiDoc;
    }

    public void setAmCuoiDoc(String amCuoiDoc) {
        this.amCuoiDoc = amCuoiDoc;
    }

    public String getVanGhep() {
        return vanGhep;
    }

    public void setVanGhep(String vanGhep) {
        this.vanGhep = vanGhep;
    }

    public String getCachDanhVan() {
        return cachDanhVan;
    }

    public void setCachDanhVan(String cachDanhVan) {
        this.cachDanhVan = cachDanhVan;
    }

    public String getAmTietDoc() {
        return amTietDoc;
    }

    public void setAmTietDoc(String amTietDoc) {
        this.amTietDoc = amTietDoc;
    }

    public String getQuyTacLienQuan() {
        return quyTacLienQuan;
    }

    public void setQuyTacLienQuan(String quyTacLienQuan) {
        this.quyTacLienQuan = quyTacLienQuan;
    }

    public boolean isLaTrongAm() {
        return laTrongAm;
    }

    public void setLaTrongAm(boolean laTrongAm) {
        this.laTrongAm = laTrongAm;
    }
}
