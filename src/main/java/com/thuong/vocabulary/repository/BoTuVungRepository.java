package com.thuong.vocabulary.repository;


import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BoTuVungRepository
        extends JpaRepository<BoTuVung,Long> {

    List<BoTuVung> findByTaiKhoanId(Long taiKhoanId);

}