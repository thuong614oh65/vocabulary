package com.thuong.vocabulary.dto.tratu;

public class LuuTuNhanhRequest {

    private String tiengAnh;
    private String tiengViet;
    private String phienAm;
    private String viDu;
    private Long boId; // null nếu muốn tạo bộ mới hoặc bộ mặc định
    private String tenBoMoi; // Tên bộ mới nếu người dùng nhập

    public LuuTuNhanhRequest() {
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

    public String getViDu() {
        return viDu;
    }

    public void setViDu(String viDu) {
        this.viDu = viDu;
    }

    public Long getBoId() {
        return boId;
    }

    public void setBoId(Long boId) {
        this.boId = boId;
    }

    public String getTenBoMoi() {
        return tenBoMoi;
    }

    public void setTenBoMoi(String tenBoMoi) {
        this.tenBoMoi = tenBoMoi;
    }
}
