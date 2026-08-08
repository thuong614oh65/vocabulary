package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.repository.TaiKhoanRepository;
import com.thuong.vocabulary.service.TaiKhoanService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaiKhoanServiceImpl implements TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanServiceImpl(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public TaiKhoan dangKy(
            String tenDangNhap,
            String matKhau,
            String hoTen,
            String email
    ) {

        if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (email != null && !email.isBlank()
                && taiKhoanRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        TaiKhoan taiKhoan = new TaiKhoan();

        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setMatKhau(matKhau);
        taiKhoan.setHoTen(hoTen);
        taiKhoan.setEmail(email);

        return taiKhoanRepository.save(taiKhoan);
    }

    @Override
    public Optional<TaiKhoan> dangNhap(
            String tenDangNhap,
            String matKhau
    ) {

        return taiKhoanRepository
                .findByTenDangNhap(tenDangNhap)
                .filter(taiKhoan ->
                        taiKhoan.getMatKhau().equals(matKhau)
                );
    }

    @Override
    public Optional<TaiKhoan> timTheoTenDangNhap(String tenDangNhap) {
        return taiKhoanRepository.findByTenDangNhap(tenDangNhap);
    }

    @Override
    public Optional<TaiKhoan> timTheoId(Long id) {
        return taiKhoanRepository.findById(id);
    }

    @Override
    public boolean daTonTaiTenDangNhap(String tenDangNhap) {
        return taiKhoanRepository.existsByTenDangNhap(tenDangNhap);
    }

    @Override
    public boolean daTonTaiEmail(String email) {
        return taiKhoanRepository.existsByEmail(email);
    }
}