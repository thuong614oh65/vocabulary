package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.CapNhatTuDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TuVung;

import java.util.List;

public interface QuanLyTuService {

    List<TuVung> layTatCaTu(Long taiKhoanId);

    List<TuVung> layTuTheoBo(Long boId, Long taiKhoanId);

    List<BoTuVung> layDanhSachBo(Long taiKhoanId);

    TuVung timTu(Long id, Long taiKhoanId);

    boolean capNhatTu(CapNhatTuDTO dto, Long taiKhoanId);

    boolean xoaTu(Long id, Long taiKhoanId);

    boolean xoaBo(Long boId, Long taiKhoanId);

    boolean suaTenBo(Long boId, String tenBoMoi, Long taiKhoanId);

    BoTuVung taoBoMoi(String tenBo, Long taiKhoanId);
}

