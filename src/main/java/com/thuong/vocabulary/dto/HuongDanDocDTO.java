package com.thuong.vocabulary.dto;

import java.util.List;

public class HuongDanDocDTO {

    private String tu;
    private String phienAm;
    private String nghia;
    private List<String> amTiet;
    private List<String> amTietIpa;
    private List<String> amTietBoi;
    private Integer amNhanIndex;
    private String phienAmTiengViet;
    private String trongAm;
    private String khauHinh;
    private String amDuoi;
    private String loiThuongGap;
    private String meoGhiNho;

    public HuongDanDocDTO() {
    }

    public HuongDanDocDTO(String tu, String phienAm, String nghia, List<String> amTiet, List<String> amTietIpa,
                          List<String> amTietBoi, Integer amNhanIndex, String phienAmTiengViet, String trongAm,
                          String khauHinh, String amDuoi, String loiThuongGap, String meoGhiNho) {
        this.tu = tu;
        this.phienAm = phienAm;
        this.nghia = nghia;
        this.amTiet = amTiet;
        this.amTietIpa = amTietIpa;
        this.amTietBoi = amTietBoi;
        this.amNhanIndex = amNhanIndex;
        this.phienAmTiengViet = phienAmTiengViet;
        this.trongAm = trongAm;
        this.khauHinh = khauHinh;
        this.amDuoi = amDuoi;
        this.loiThuongGap = loiThuongGap;
        this.meoGhiNho = meoGhiNho;
    }

    public String getTu() {
        return tu;
    }

    public void setTu(String tu) {
        this.tu = tu;
    }

    public String getPhienAm() {
        return phienAm;
    }

    public void setPhienAm(String phienAm) {
        this.phienAm = phienAm;
    }

    public String getNghia() {
        return nghia;
    }

    public void setNghia(String nghia) {
        this.nghia = nghia;
    }

    public List<String> getAmTiet() {
        return amTiet;
    }

    public void setAmTiet(List<String> amTiet) {
        this.amTiet = amTiet;
    }

    public List<String> getAmTietIpa() {
        return amTietIpa;
    }

    public void setAmTietIpa(List<String> amTietIpa) {
        this.amTietIpa = amTietIpa;
    }

    public List<String> getAmTietBoi() {
        return amTietBoi;
    }

    public void setAmTietBoi(List<String> amTietBoi) {
        this.amTietBoi = amTietBoi;
    }

    public Integer getAmNhanIndex() {
        return amNhanIndex;
    }

    public void setAmNhanIndex(Integer amNhanIndex) {
        this.amNhanIndex = amNhanIndex;
    }

    public String getPhienAmTiengViet() {
        return phienAmTiengViet;
    }

    public void setPhienAmTiengViet(String phienAmTiengViet) {
        this.phienAmTiengViet = phienAmTiengViet;
    }

    public String getTrongAm() {
        return trongAm;
    }

    public void setTrongAm(String trongAm) {
        this.trongAm = trongAm;
    }

    public String getKhauHinh() {
        return khauHinh;
    }

    public void setKhauHinh(String khauHinh) {
        this.khauHinh = khauHinh;
    }

    public String getAmDuoi() {
        return amDuoi;
    }

    public void setAmDuoi(String amDuoi) {
        this.amDuoi = amDuoi;
    }

    public String getLoiThuongGap() {
        return loiThuongGap;
    }

    public void setLoiThuongGap(String loiThuongGap) {
        this.loiThuongGap = loiThuongGap;
    }

    public String getMeoGhiNho() {
        return meoGhiNho;
    }

    public void setMeoGhiNho(String meoGhiNho) {
        this.meoGhiNho = meoGhiNho;
    }
}
