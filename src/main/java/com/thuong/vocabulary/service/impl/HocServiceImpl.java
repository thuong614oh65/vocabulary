package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.HocService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class HocServiceImpl implements HocService {

    private final TuVungRepository repository;

    public HocServiceImpl(TuVungRepository repository) {
        this.repository = repository;
    }

    // =========================================================
    // LẤY TẤT CẢ TỪ CỦA TÀI KHOẢN
    // =========================================================

    @Override
    public List<TuVung> layTatCa(Long taiKhoanId) {

        return repository
                .findAllByBoTuVungTaiKhoanIdOrderByBoTuVungIdDescIdAsc(
                        taiKhoanId
                );
    }

    // =========================================================
    // LẤY TỪ THEO BỘ + TÀI KHOẢN
    // =========================================================

    @Override
    public List<TuVung> layTheoBo(
            Long boId,
            Long taiKhoanId
    ) {

        return repository
                .findAllByBoTuVungIdAndBoTuVungTaiKhoanIdOrderByIdAsc(
                        boId,
                        taiKhoanId
                );
    }

    // =========================================================
    // LẤY TỪ NGẪU NHIÊN CỦA TÀI KHOẢN
    // =========================================================

    @Override
    public List<TuVung> layNgauNhien(Long taiKhoanId) {

        List<TuVung> ds =
                repository.findAllByBoTuVungTaiKhoanId(
                        taiKhoanId
                );

        Collections.shuffle(ds);

        return ds;
    }

    // =========================================================
    // LẤY THEO ID + KIỂM TRA TÀI KHOẢN
    // =========================================================

    @Override
    public List<TuVung> layTheoIds(
            Long[] ids,
            Long taiKhoanId
    ) {

        if (ids == null || ids.length == 0) {
            return new ArrayList<>();
        }

        List<TuVung> ketQua = new ArrayList<>();

        for (Long id : ids) {

            if (id == null) {
                continue;
            }

            repository.findById(id)
                    .filter(tu ->
                            tu.getBoTuVung() != null
                                    && tu.getBoTuVung().getTaiKhoan() != null
                                    && tu.getBoTuVung()
                                    .getTaiKhoan()
                                    .getId()
                                    .equals(taiKhoanId)
                    )
                    .ifPresent(ketQua::add);
        }

        return ketQua;
    }

    // =========================================================
    // TĂNG SỐ LẦN SAI
    // =========================================================

    @Override
    public void tangSoLanSai(
            Long id,
            Long taiKhoanId
    ) {

        repository.tangSoLanSai(
                id,
                taiKhoanId
        );
    }

    // =========================================================
    // LẤY TỪ SAI CỦA TÀI KHOẢN
    // =========================================================

    @Override
    public List<TuVung> layTuSai(
            Long taiKhoanId
    ) {

        return repository
                .findBySoLanSaiGreaterThanAndBoTuVungTaiKhoanIdOrderBySoLanSaiDesc(
                        0,
                        taiKhoanId
                );
    }

    // =========================================================
    // GIẢM SỐ LẦN SAI
    // =========================================================

    @Override
    public void giamSoLanSai(
            Long id,
            Long taiKhoanId
    ) {

        repository.giamSoLanSai(
                id,
                taiKhoanId
        );
    }
}