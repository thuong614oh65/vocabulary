package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.TaiKhoan;

import java.util.List;

public interface LuuTuVungService {

    String luuBo(
            List<TuVungDTO> danhSach,
            TaiKhoan taiKhoan
    );
}