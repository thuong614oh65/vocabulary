package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.service.DanhSachTuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DanhSachTuServiceImpl implements DanhSachTuService {

    @Override
    public List<String> tachDanhSach(String noiDung) {

        List<String> ketQua = new ArrayList<>();

        if (noiDung == null || noiDung.isBlank()) {
            return ketQua;
        }

        String[] mang = noiDung.split("\\R");

        for (String tu : mang) {

            tu = tu.trim();

            if (!tu.isEmpty()) {
                ketQua.add(tu);
            }

        }

        return ketQua;
    }

}