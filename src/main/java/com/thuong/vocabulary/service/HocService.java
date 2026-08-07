package com.thuong.vocabulary.service;

import com.thuong.vocabulary.entity.TuVung;

import java.util.List;

public interface HocService {

    List<TuVung> layTatCa();

    List<TuVung> layTheoBo(Long boId);

    List<TuVung> layNgauNhien();

    List<TuVung> layTheoIds(Long[] ids);

    void tangSoLanSai(Long id);

    List<TuVung> layTuSai();

    void giamSoLanSai(Long id);
}