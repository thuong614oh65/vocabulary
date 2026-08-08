package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.service.BoTuVungService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoTuVungServiceImpl implements BoTuVungService {

    private final BoTuVungRepository boTuVungRepository;

    public BoTuVungServiceImpl(BoTuVungRepository boTuVungRepository) {
        this.boTuVungRepository = boTuVungRepository;
    }

    @Override
    public List<BoTuVung> layTatCa() {
        return boTuVungRepository.findAll();
    }

    // Lấy tất cả bộ từ của một tài khoản
    @Override
    public List<BoTuVung> layDanhSachBo(Long taiKhoanId) {
        return boTuVungRepository.findByTaiKhoanId(taiKhoanId);
    }

    // Tạo bộ từ cho tài khoản
    @Override
    public BoTuVung taoBo(String tenBo, TaiKhoan taiKhoan) {

        BoTuVung bo = new BoTuVung();

        bo.setTenBo(tenBo);
        bo.setTaiKhoan(taiKhoan);

        return boTuVungRepository.save(bo);
    }

    // Tìm bộ từ nhưng phải thuộc tài khoản đang đăng nhập
    @Override
    public BoTuVung timBo(Long boId, Long taiKhoanId) {

        return boTuVungRepository.findById(boId)
                .filter(bo ->
                        bo.getTaiKhoan() != null
                                && bo.getTaiKhoan().getId().equals(taiKhoanId)
                )
                .orElse(null);
    }

    // Xóa bộ từ
    @Override
    public void xoaBo(Long boId, Long taiKhoanId) {

        BoTuVung bo = timBo(boId, taiKhoanId);

        if (bo != null) {
            boTuVungRepository.delete(bo);
        }
    }
}