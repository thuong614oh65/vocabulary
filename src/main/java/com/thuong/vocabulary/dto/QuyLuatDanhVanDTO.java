package com.thuong.vocabulary.dto;

import java.util.ArrayList;
import java.util.List;

public class QuyLuatDanhVanDTO {
    private String id;
    private String cumChu;
    private String docLaIpa;
    private String amDoc;
    private String tieuDeQuyTac;
    private String moTaQuyTac;
    private String phanLoai;
    private String tenPhanLoai;
    private String icon;
    private String huongDanPhatAm;
    private List<TuMinhHoaDTO> danhSachTu = new ArrayList<>();

    public QuyLuatDanhVanDTO() {
    }

    public QuyLuatDanhVanDTO(String id, String cumChu, String docLaIpa, String amDoc, String tieuDeQuyTac,
                             String moTaQuyTac, String phanLoai, String tenPhanLoai, String icon,
                             String huongDanPhatAm, List<TuMinhHoaDTO> danhSachTu) {
        this.id = id;
        this.cumChu = cumChu;
        this.docLaIpa = docLaIpa;
        this.amDoc = amDoc;
        this.tieuDeQuyTac = tieuDeQuyTac;
        this.moTaQuyTac = moTaQuyTac;
        this.phanLoai = phanLoai;
        this.tenPhanLoai = tenPhanLoai;
        this.icon = icon;
        this.huongDanPhatAm = huongDanPhatAm;
        this.danhSachTu = (danhSachTu != null) ? danhSachTu : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCumChu() {
        return cumChu;
    }

    public void setCumChu(String cumChu) {
        this.cumChu = cumChu;
    }

    public String getDocLaIpa() {
        return docLaIpa;
    }

    public void setDocLaIpa(String docLaIpa) {
        this.docLaIpa = docLaIpa;
    }

    public String getAmDoc() {
        return amDoc;
    }

    public void setAmDoc(String amDoc) {
        this.amDoc = amDoc;
    }

    public String getTieuDeQuyTac() {
        return tieuDeQuyTac;
    }

    public void setTieuDeQuyTac(String tieuDeQuyTac) {
        this.tieuDeQuyTac = tieuDeQuyTac;
    }

    public String getMoTaQuyTac() {
        return moTaQuyTac;
    }

    public void setMoTaQuyTac(String moTaQuyTac) {
        this.moTaQuyTac = moTaQuyTac;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public void setPhanLoai(String phanLoai) {
        this.phanLoai = phanLoai;
    }

    public String getTenPhanLoai() {
        return tenPhanLoai;
    }

    public void setTenPhanLoai(String tenPhanLoai) {
        this.tenPhanLoai = tenPhanLoai;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHuongDanPhatAm() {
        return huongDanPhatAm;
    }

    public void setHuongDanPhatAm(String huongDanPhatAm) {
        this.huongDanPhatAm = huongDanPhatAm;
    }

    public List<TuMinhHoaDTO> getDanhSachTu() {
        return danhSachTu;
    }

    public void setDanhSachTu(List<TuMinhHoaDTO> danhSachTu) {
        this.danhSachTu = danhSachTu;
    }
}
