package com.thuong.vocabulary.repository;

import com.thuong.vocabulary.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {

    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    Optional<TaiKhoan> findByEmail(String email);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);
}