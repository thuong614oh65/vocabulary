package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.CapNhatTuDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.AudioService;
import com.thuong.vocabulary.service.QuanLyTuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuanLyTuServiceImpl implements QuanLyTuService {

    private final TuVungRepository tuVungRepository;
    private final BoTuVungRepository boTuVungRepository;
    private final AudioService audioService;

    public QuanLyTuServiceImpl(
            TuVungRepository tuVungRepository,
            BoTuVungRepository boTuVungRepository,
            AudioService audioService
    ) {
        this.tuVungRepository = tuVungRepository;
        this.boTuVungRepository = boTuVungRepository;
        this.audioService = audioService;
    }

    @Override
    public List<TuVung> layTatCaTu(Long taiKhoanId) {
        if (taiKhoanId == null) {
            return List.of();
        }
        return tuVungRepository.findAllByBoTuVungTaiKhoanIdOrderByBoTuVungIdDescIdAsc(taiKhoanId);
    }

    @Override
    public List<TuVung> layTuTheoBo(Long boId, Long taiKhoanId) {
        if (boId == null || taiKhoanId == null) {
            return List.of();
        }
        return tuVungRepository.findAllByBoTuVungIdAndBoTuVungTaiKhoanId(boId, taiKhoanId);
    }

    @Override
    public List<BoTuVung> layDanhSachBo(Long taiKhoanId) {
        if (taiKhoanId == null) {
            return List.of();
        }
        return boTuVungRepository.findByTaiKhoanId(taiKhoanId);
    }

    @Override
    public TuVung timTu(Long id, Long taiKhoanId) {
        if (id == null || taiKhoanId == null) {
            return null;
        }
        return tuVungRepository.findById(id)
                .filter(tu -> tu.getBoTuVung() != null
                        && tu.getBoTuVung().getTaiKhoan() != null
                        && tu.getBoTuVung().getTaiKhoan().getId().equals(taiKhoanId))
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean capNhatTu(CapNhatTuDTO dto, Long taiKhoanId) {
        if (dto == null || dto.getId() == null || taiKhoanId == null) {
            return false;
        }

        TuVung tu = timTu(dto.getId(), taiKhoanId);
        if (tu == null) {
            return false;
        }

        String tuMoi = dto.getTiengAnh() != null ? dto.getTiengAnh().trim() : "";
        String tuCu = tu.getTiengAnh() != null ? tu.getTiengAnh().trim() : "";

        boolean thayDoiTiengAnh = !tuMoi.equalsIgnoreCase(tuCu);

        tu.setTiengAnh(tuMoi);
        tu.setTiengViet(dto.getTiengViet() != null ? dto.getTiengViet().trim() : "");
        tu.setPhienAm(dto.getPhienAm() != null ? dto.getPhienAm().trim() : "");
        tu.setViDu(dto.getViDu() != null ? dto.getViDu().trim() : "");

        if (dto.getSoLanSai() != null && dto.getSoLanSai() >= 0) {
            tu.setSoLanSai(dto.getSoLanSai());
        }

        if (dto.getBoId() != null) {
            if (tu.getBoTuVung() == null || !dto.getBoId().equals(tu.getBoTuVung().getId())) {
                boTuVungRepository.findById(dto.getBoId())
                        .filter(bo -> bo.getTaiKhoan() != null && bo.getTaiKhoan().getId().equals(taiKhoanId))
                        .ifPresent(tu::setBoTuVung);
            }
        }

        tuVungRepository.save(tu);

        if (thayDoiTiengAnh && !tuMoi.isEmpty()) {
            try {
                audioService.taoAudio(tuMoi);
            } catch (Exception e) {
                System.err.println("Không thể tạo audio tự động cho từ: " + tuMoi + " | Lỗi: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean xoaTu(Long id, Long taiKhoanId) {
        if (id == null || taiKhoanId == null) {
            return false;
        }

        TuVung tu = timTu(id, taiKhoanId);
        if (tu == null) {
            return false;
        }

        tuVungRepository.delete(tu);
        return true;
    }
}
