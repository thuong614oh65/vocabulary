package com.thuong.vocabulary.service;

public interface PhienAmService {

    /**
     * Tra cứu hoặc tạo phiên âm chuẩn IPA (dạng /.../) cho từ hoặc cụm từ tiếng Anh.
     *
     * @param tu từ đơn, từ ghép hoặc cụm từ tiếng Anh
     * @return chuỗi phiên âm IPA đã được chuẩn hóa (ví dụ: "/ˈæp.əl/", "/ˌaɪsˈkriːm/")
     */
    String layPhienAm(String tu);

}
