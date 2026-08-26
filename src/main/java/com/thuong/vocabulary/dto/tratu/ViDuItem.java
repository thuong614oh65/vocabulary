package com.thuong.vocabulary.dto.tratu;

public class ViDuItem {

    private String cauTiengAnh;
    private String cauTiengViet;

    public ViDuItem() {
    }

    public ViDuItem(String cauTiengAnh, String cauTiengViet) {
        this.cauTiengAnh = cauTiengAnh;
        this.cauTiengViet = cauTiengViet;
    }

    public String getCauTiengAnh() {
        return cauTiengAnh;
    }

    public void setCauTiengAnh(String cauTiengAnh) {
        this.cauTiengAnh = cauTiengAnh;
    }

    public String getCauTiengViet() {
        return cauTiengViet;
    }

    public void setCauTiengViet(String cauTiengViet) {
        this.cauTiengViet = cauTiengViet;
    }
}
