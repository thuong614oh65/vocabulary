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

    public HocServiceImpl(
            TuVungRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public List<TuVung> layTatCa() {

        return repository.findAllByOrderByBoTuVungIdDescIdAsc();

    }

    @Override
    public List<TuVung> layTheoBo(Long boId){

        return repository.findAllByBoTuVungId(boId);

    }

    @Override
    public List<TuVung> layNgauNhien() {

        List<TuVung> ds = repository.findAll();

        Collections.shuffle(ds);

        return ds;

    }

    @Override
    public List<TuVung> layTheoIds(Long[] ids) {

        if(ids == null){

            return new ArrayList<>();

        }

        return repository.findAllById(
                Arrays.asList(ids)
        );

    }

    @Override
    public void tangSoLanSai(Long id){

        repository.tangSoLanSai(id);

    }

    @Override
    public List<TuVung> layTuSai() {
        return repository.findBySoLanSaiGreaterThanOrderBySoLanSaiDesc(0);
    }

    @Override
    public void giamSoLanSai(Long id) {
        repository.giamSoLanSai(id);
    }

}