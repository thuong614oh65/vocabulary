package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.service.BoTuVungService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoTuVungServiceImpl
        implements BoTuVungService {

    private final BoTuVungRepository repository;

    public BoTuVungServiceImpl(
            BoTuVungRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public List<BoTuVung> layTatCa() {

        return repository.findAll();

    }

}