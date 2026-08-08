package com.thuong.vocabulary.service;

import com.thuong.vocabulary.entity.TuVung;

import java.util.List;

public interface HocService {

    List<TuVung> layTatCa(Long taiKhoanId);

    List<TuVung> layTheoBo(
            Long boId,
            Long taiKhoanId
    );

    List<TuVung> layNgauNhien(
            Long taiKhoanId
    );

    List<TuVung> layTheoIds(
            Long[] ids,
            Long taiKhoanId
    );

    void tangSoLanSai(
            Long id,
            Long taiKhoanId
    );

    List<TuVung> layTuSai(
            Long taiKhoanId
    );

    void giamSoLanSai(
            Long id,
            Long taiKhoanId
    );
}