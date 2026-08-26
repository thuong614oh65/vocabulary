package com.thuong.vocabulary.dto.tratu;

public class TraTuRequest {

    private String text;
    private String mode; // "EN_VI", "VI_EN", "AUTO"

    public TraTuRequest() {
    }

    public TraTuRequest(String text, String mode) {
        this.text = text;
        this.mode = mode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
