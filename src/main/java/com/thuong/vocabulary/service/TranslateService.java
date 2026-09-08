package com.thuong.vocabulary.service;

public interface TranslateService {

    String dich(String text);

    String dich(String text, String fromLang, String toLang);

    String dichAnhSangViet(String text);

    String dichVietSangAnh(String text);

    java.util.Map<String, String> dichHangLoat(java.util.List<String> danhSach);

}