package com.thuong.vocabulary.dto;

public class CapNhatTuDTO {

    private Long id;
    private String tiengAnh;
    private String tiengViet;
    private String phienAm;
    private String viDu;
    private Long boId;
    private Integer soLanSai;

    public CapNhatTuDTO() {
    }

    public CapNhatTuDTO(Long id, String tiengAnh, String tiengViet, String phienAm, String viDu, Long boId, Integer soLanSai) {
        this.id = id;
        this.tiengAnh = tiengAnh;
        this.tiengViet = tiengViet;
        this.phienAm = phienAm;
        this.viDu = viDu;
        this.boId = boId;
        this.soLanSai = soLanSai;
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

    public Integer getSoLanSai() {
        return soLanSai;
    }

    public void setSoLanSai(Integer soLanSai) {
        this.soLanSai = soLanSai;
    }
}
