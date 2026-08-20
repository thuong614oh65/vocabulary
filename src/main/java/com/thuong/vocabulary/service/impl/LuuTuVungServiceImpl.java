package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.LuuTuVungService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


        List<String> daCo = new ArrayList<>();

        List<String> trung = new ArrayList<>();

        List<TuVungDTO> dsLuu = new ArrayList<>();


        // =====================================================
        // KIỂM TRA TỪ TRÙNG
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
            // Trùng trong danh sách đang nhập
            // -------------------------------------------------

            if (daCo.contains(tu)) {

                trung.add(tu);

                continue;
            }


            // -------------------------------------------------
            // Trùng trong database
            // CHỈ kiểm tra trong tài khoản hiện tại
            // -------------------------------------------------

            if (tuRepo.existsByTiengAnhIgnoreCaseAndBoTuVungTaiKhoanId(
                    tu,
                    taiKhoanId
            )) {

                trung.add(tu);

                continue;
            }


            daCo.add(tu);

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
        // TẠO BỘ TỪ MỚI THEO TÀI KHOẢN
        // =====================================================

        long soThuTu = boRepo.countByTaiKhoanId(taiKhoanId) + 1;
        while (boRepo.existsByTenBoAndTaiKhoanId("Bộ " + soThuTu, taiKhoanId)) {
            soThuTu++;
        }

        BoTuVung bo = new BoTuVung();

        bo.setTenBo(
                "Bộ " + soThuTu
        );

        bo.setNgayTao(
                LocalDateTime.now()
        );


        // QUAN TRỌNG:
        // Gắn bộ từ với tài khoản đang đăng nhập

        bo.setTaiKhoan(taiKhoan);


        boRepo.save(bo);


        // =====================================================
        // LƯU CÁC TỪ
        // =====================================================

        for (TuVungDTO dto : dsLuu) {

            TuVung tu = new TuVung();


            tu.setTiengAnh(
                    dto.getTiengAnh()
            );


            tu.setTiengViet(
                    dto.getTiengViet()
            );


            tu.setPhienAm(
                    dto.getPhienAm()
            );


            tu.setViDu(
                    dto.getViDu()
            );


            // Gắn từ vào bộ

            tu.setBoTuVung(bo);


            tuRepo.save(tu);
        }


        // =====================================================
        // THÔNG BÁO
        // =====================================================

        String ketQua =
                "Đã lưu "
                        + dsLuu.size()
                        + " từ";


        if (!trung.isEmpty()) {

            ketQua +=
                    ". Bỏ qua từ trùng: "
                            + trung;
        }


        return ketQua;
    }
}