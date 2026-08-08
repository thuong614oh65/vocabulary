package com.thuong.vocabulary.service;

import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;

import java.util.List;

public interface BoTuVungService {

    // Lấy tất cả bộ từ
    List<BoTuVung> layTatCa();

    // Lấy tất cả bộ từ của một tài khoản
    List<BoTuVung> layDanhSachBo(Long taiKhoanId);

    // Tạo bộ từ cho tài khoản
    BoTuVung taoBo(String tenBo, TaiKhoan taiKhoan);

    // Tìm bộ từ nhưng phải thuộc tài khoản đang đăng nhập
    BoTuVung timBo(Long boId, Long taiKhoanId);

    // Xóa bộ từ
    void xoaBo(Long boId, Long taiKhoanId);
}