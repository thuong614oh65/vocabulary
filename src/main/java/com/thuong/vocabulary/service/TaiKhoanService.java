package com.thuong.vocabulary.service;

import com.thuong.vocabulary.entity.TaiKhoan;

import java.util.Optional;

public interface TaiKhoanService {

    TaiKhoan dangKy(
            String tenDangNhap,
            String matKhau,
            String hoTen,
            String email
    );

    Optional<TaiKhoan> dangNhap(
            String tenDangNhap,
            String matKhau
    );

    Optional<TaiKhoan> timTheoTenDangNhap(String tenDangNhap);

    Optional<TaiKhoan> timTheoId(Long id);

    boolean daTonTaiTenDangNhap(String tenDangNhap);

    boolean daTonTaiEmail(String email);
}