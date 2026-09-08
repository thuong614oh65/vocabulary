package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.LuuTuVungService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LuuTuVungServiceImpl implements LuuTuVungService {

    private final BoTuVungRepository boRepo;

    private final TuVungRepository tuRepo;

    public LuuTuVungServiceImpl(
            BoTuVungRepository boRepo,
            TuVungRepository tuRepo
    ) {
        this.boRepo = boRepo;
        this.tuRepo = tuRepo;
    }


    @Override
    public String luuBo(
            List<TuVungDTO> danhSach,
            TaiKhoan taiKhoan
    ) {
        return luuBo(danhSach, taiKhoan, null, null);
    }

    @Override
    @Transactional
    public String luuBo(
            List<TuVungDTO> danhSach,
            TaiKhoan taiKhoan,
            Long boId,
            String tenBoMoi
    ) {

        // =====================================================
        // KIỂM TRA TÀI KHOẢN
        // =====================================================

        if (taiKhoan == null || taiKhoan.getId() == null) {
            return "Chưa đăng nhập";
        }


        // =====================================================
        // KIỂM TRA DANH SÁCH
        // =====================================================

        if (danhSach == null || danhSach.isEmpty()) {
            return "Không có từ nào để lưu";
        }


        Long taiKhoanId = taiKhoan.getId();

        // 1 lần truy vấn duy nhất lấy toàn bộ từ tiếng Anh đã có của tài khoản
        Set<String> tuDaCoTrongDb = new HashSet<>();
        List<String> dbWords = tuRepo.findAllTiengAnhByTaiKhoanId(taiKhoanId);
        if (dbWords != null) {
            for (String w : dbWords) {
                if (w != null) {
                    tuDaCoTrongDb.add(w.trim().toLowerCase());
                }
            }
        }

        Set<String> daCoTrongDanhSach = new HashSet<>();
        List<String> trung = new ArrayList<>();
        List<TuVungDTO> dsLuu = new ArrayList<>();


        // =====================================================
        // KIỂM TRA TỪ TRÙNG SIÊU TỐC TRONG BỘ NHỚ (O(1))
        // =====================================================

        for (TuVungDTO dto : danhSach) {

            // Không có dữ liệu
            if (dto == null
                    || dto.getTiengAnh() == null
                    || dto.getTiengAnh().isBlank()) {

                continue;
            }


            String tu = dto.getTiengAnh()
                    .trim()
                    .toLowerCase();


            // -------------------------------------------------
            // Trùng trong danh sách đang nhập hoặc database
            // -------------------------------------------------

            if (daCoTrongDanhSach.contains(tu) || tuDaCoTrongDb.contains(tu)) {
                trung.add(tu);
                continue;
            }

            daCoTrongDanhSach.add(tu);
            dsLuu.add(dto);
        }


        // =====================================================
        // KHÔNG CÒN TỪ NÀO ĐỂ LƯU
        // =====================================================

        if (dsLuu.isEmpty()) {

            return "Không có từ mới để lưu. Từ trùng: "
                    + trung;
        }


        // =====================================================
        // XÁC ĐỊNH BỘ TỪ (CHỌN BỘ CÓ SẴN HOẶC TẠO BỘ MỚI VỚI TÊN TÙY CHỈNH)
        // =====================================================

        BoTuVung bo = null;

        // 1. Trường hợp chọn bộ từ đã có
        if (boId != null && boId > 0) {
            bo = boRepo.findById(boId)
                    .filter(b -> b.getTaiKhoan() != null && b.getTaiKhoan().getId().equals(taiKhoanId))
                    .orElse(null);
        }

        // 2. Trường hợp tự đặt tên cho bộ mới
        if (bo == null && tenBoMoi != null && !tenBoMoi.isBlank()) {
            String tenChuanHoa = tenBoMoi.trim();
            bo = boRepo.findByTenBoAndTaiKhoanId(tenChuanHoa, taiKhoanId).orElse(null);
            if (bo == null) {
                bo = new BoTuVung();
                bo.setTenBo(tenChuanHoa);
                bo.setNgayTao(LocalDateTime.now());
                bo.setTaiKhoan(taiKhoan);
                bo = boRepo.save(bo);
            }
        }

        // 3. Trường hợp mặc định: Tạo tên bộ tự động ("Bộ X")
        if (bo == null) {
            long soThuTu = boRepo.countByTaiKhoanId(taiKhoanId) + 1;
            while (boRepo.existsByTenBoAndTaiKhoanId("Bộ " + soThuTu, taiKhoanId)) {
                soThuTu++;
            }

            bo = new BoTuVung();
            bo.setTenBo("Bộ " + soThuTu);
            bo.setNgayTao(LocalDateTime.now());
            bo.setTaiKhoan(taiKhoan);
            bo = boRepo.save(bo);
        }


        // =====================================================
        // LƯU TOÀN BỘ CÁC TỪ TRONG 1 BATCH DUY NHẤT (saveAll)
        // =====================================================

        List<TuVung> danhSachEntity = new ArrayList<>(dsLuu.size());
        for (TuVungDTO dto : dsLuu) {

            TuVung tu = new TuVung();

            tu.setTiengAnh(
                    dto.getTiengAnh().trim()
            );

            tu.setTiengViet(
                    dto.getTiengViet() != null ? dto.getTiengViet().trim() : ""
            );

            tu.setPhienAm(
                    dto.getPhienAm() != null ? dto.getPhienAm().trim() : ""
            );

            tu.setViDu(
                    dto.getViDu() != null ? dto.getViDu().trim() : ""
            );

            // Gắn từ vào bộ
            tu.setBoTuVung(bo);

            danhSachEntity.add(tu);
        }

        tuRepo.saveAll(danhSachEntity);


        // =====================================================
        // THÔNG BÁO
        // =====================================================

        String ketQua =
                "Đã lưu "
                        + dsLuu.size()
                        + " từ vào \""
                        + bo.getTenBo()
                        + "\"";


        if (!trung.isEmpty()) {

            ketQua +=
                    ". Bỏ qua từ trùng: "
                            + trung;
        }


        return ketQua;
    }
}