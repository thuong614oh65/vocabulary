package com.thuong.vocabulary.dto;

public class TuMinhHoaDTO {
    private String tu;
    private String phanHighlight;
    private String phienAm;
    private String phanIpaHighlight;
    private String nghia;
    private String icon;
    private String audioUrl;

    public TuMinhHoaDTO() {
    }

    public TuMinhHoaDTO(String tu, String phanHighlight, String phienAm, String phanIpaHighlight, String nghia, String icon, String audioUrl) {
        this.tu = tu;
        this.phanHighlight = phanHighlight;
        this.phienAm = phienAm;
        this.phanIpaHighlight = phanIpaHighlight;
        this.nghia = nghia;
        this.icon = icon;
        this.audioUrl = audioUrl;
    }

    public String getTu() {
        return tu;
    }

    public void setTu(String tu) {
        this.tu = tu;
    }

    public String getPhanHighlight() {
        return phanHighlight;
    }

    public void setPhanHighlight(String phanHighlight) {
        this.phanHighlight = phanHighlight;
    }

    public String getPhienAm() {
        return phienAm;
    }

    public void setPhienAm(String phienAm) {
        this.phienAm = phienAm;
    }

    public String getPhanIpaHighlight() {
        return phanIpaHighlight;
    }

    public void setPhanIpaHighlight(String phanIpaHighlight) {
        this.phanIpaHighlight = phanIpaHighlight;
    }

    public String getNghia() {
        return nghia;
    }

    public void setNghia(String nghia) {
        this.nghia = nghia;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
