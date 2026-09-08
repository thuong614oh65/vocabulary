package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.HocDTO;
import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.BoTuVungService;
import com.thuong.vocabulary.service.HocService;
import com.thuong.vocabulary.service.LuuTuVungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thuong.vocabulary.service.AudioService;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.GeminiService;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HocController {

    private final BoTuVungService boTuVungService;

    private final HocService hocService;

    private final LuuTuVungService luuTuVungService;

    private final AudioService audioService;

    private final GeminiService geminiService;

    private final TuVungRepository tuVungRepository;

    public HocController(
            BoTuVungService boTuVungService,
            HocService hocService,
            LuuTuVungService luuTuVungService,
            AudioService audioService,
            GeminiService geminiService,
            TuVungRepository tuVungRepository
    ) {
        this.boTuVungService = boTuVungService;
        this.hocService = hocService;
        this.luuTuVungService = luuTuVungService;
        this.audioService = audioService;
        this.geminiService = geminiService;
        this.tuVungRepository = tuVungRepository;
    }

    // =====================================================
    // API ĐỀ XUẤT 10 TỪ MỚI THEO CHỦ ĐỀ (CHƯA CÓ TRONG CSDL)
    // =====================================================
    @GetMapping("/api/hoc/de-xuat-chu-de")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deXuatTuTheoChuDe(
            @RequestParam("chuDe") String chuDe,
            @RequestParam(value = "loaiTru", required = false) List<String> loaiTru,
            HttpSession session
    ) {
        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("taiKhoan");
        if (taiKhoan == null) {
            return org.springframework.http.ResponseEntity.status(401).body("Chưa đăng nhập");
        }
        if (chuDe == null || chuDe.trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Vui lòng nhập tên chủ đề");
        }

        Long taiKhoanId = taiKhoan.getId();
        List<String> tuDaCo = tuVungRepository.findAllTiengAnhByTaiKhoanId(taiKhoanId);
        if (tuDaCo == null) tuDaCo = new ArrayList<>();
        if (loaiTru != null) {
            for (String w : loaiTru) {
                if (w != null && !w.isBlank()) tuDaCo.add(w.trim().toLowerCase());
            }
        }

        List<TuVungDTO> ketQua = geminiService.deXuatTuTheoChuDe(chuDe.trim(), tuDaCo);

        // Lọc kỹ lại 1 lần nữa qua CSDL để cam kết 100% không trùng từ nào của user
        List<TuVungDTO> ketQuaChuan = new ArrayList<>();
        for (TuVungDTO dto : ketQua) {
            if (dto.getTiengAnh() != null && !dto.getTiengAnh().isBlank()) {
                String word = dto.getTiengAnh().trim();
                boolean exists = tuVungRepository.existsByTiengAnhIgnoreCaseAndBoTuVungTaiKhoanId(word, taiKhoanId);
                if (!exists) {
                    ketQuaChuan.add(dto);
                }
            }
            if (ketQuaChuan.size() == 10) break;
        }

        return org.springframework.http.ResponseEntity.ok(ketQuaChuan);
    }


    // =====================================================
    // TRANG CHỌN HỌC
    // =====================================================

    @GetMapping("/hoc")
    public String hoc(
            Model model,
            HttpSession session
    ) {

        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        // Chưa đăng nhập
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();

        HocDTO dto = new HocDTO();

        dto.setKieuHoc("NGAU_NHIEN");

        model.addAttribute(
                "hocDTO",
                dto
        );


        // CHỈ LẤY BỘ CỦA USER ĐANG ĐĂNG NHẬP
        model.addAttribute(
                "dsBo",
                boTuVungService.layDanhSachBo(taiKhoanId)
        );


        // CHỈ LẤY TỪ CỦA USER ĐANG ĐĂNG NHẬP
        model.addAttribute(
                "dsTatCa",
                hocService.layTatCa(taiKhoanId)
        );


        model.addAttribute(
                "dsTheoBo",
                new ArrayList<>()
        );

        return "hoc";
    }


    // =====================================================
    // HỌC THEO BỘ
    // =====================================================

    @GetMapping("/hoc/bo/{id}")
    public String hocTheoBo(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {

        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        // Chưa đăng nhập
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();


        // Kiểm tra bộ có thuộc user này không
        if (boTuVungService.timBo(id, taiKhoanId) == null) {
            return "redirect:/hoc";
        }


        HocDTO dto = new HocDTO();

        dto.setKieuHoc("THEO_BO");

        dto.setBoId(id);


        // CHỈ LẤY BỘ CỦA USER
        model.addAttribute(
                "dsBo",
                boTuVungService.layDanhSachBo(taiKhoanId)
        );


        // CHỈ LẤY TỪ CỦA USER
        model.addAttribute(
                "dsTatCa",
                hocService.layTatCa(taiKhoanId)
        );


        // CHỈ LẤY TỪ TRONG BỘ CỦA USER
        model.addAttribute(
                "dsTheoBo",
                hocService.layTheoBo(
                        id,
                        taiKhoanId
                )
        );


        model.addAttribute(
                "hocDTO",
                dto
        );

        return "hoc";
    }


    // =====================================================
    // LƯU BỘ TỪ
    // =====================================================

    @PostMapping("/luu-bo")
    public String luuBo(

            @RequestParam List<String> tiengAnh,

            @RequestParam List<String> tiengViet,

            @RequestParam(required = false)
            List<String> phienAm,

            @RequestParam(required = false)
            List<String> viDu,

            @RequestParam(required = false)
            String boId,

            @RequestParam(required = false)
            String tenBo,

            HttpSession session
    ) {

        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        // Chưa đăng nhập
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        List<TuVungDTO> danhSach =
                new ArrayList<>();


        for (int i = 0; i < tiengAnh.size(); i++) {

            TuVungDTO dto =
                    new TuVungDTO();


            dto.setTiengAnh(
                    tiengAnh.get(i)
            );


            dto.setTiengViet(
                    tiengViet.get(i)
            );


            if (phienAm != null
                    && i < phienAm.size()) {

                dto.setPhienAm(
                        phienAm.get(i)
                );
            }


            if (viDu != null
                    && i < viDu.size()) {

                dto.setViDu(
                        viDu.get(i)
                );
            }


            danhSach.add(dto);
        }

        Long parsedBoId = null;
        if (boId != null && !boId.isBlank()) {
            try {
                parsedBoId = Long.parseLong(boId.trim());
            } catch (Exception ignored) {
            }
        }

        String thongBao =
                luuTuVungService.luuBo(
                        danhSach,
                        taiKhoan,
                        parsedBoId,
                        tenBo
                );




// =====================================================
// TẠO AUDIO CHẠY NGẦM BẰNG VIRTUAL THREAD (KHÔNG LÀM CHỜ/TREO GIAO DIỆN)
// =====================================================

        List<String> dsTuCanTaoAudio = new ArrayList<>();
        for (TuVungDTO dto : danhSach) {
            if (dto != null && dto.getTiengAnh() != null && !dto.getTiengAnh().isBlank()) {
                dsTuCanTaoAudio.add(dto.getTiengAnh().trim());
            }
        }

        if (!dsTuCanTaoAudio.isEmpty()) {
            Thread.startVirtualThread(() -> {
                for (String tu : dsTuCanTaoAudio) {
                    try {
                        audioService.taoAudio(tu);
                    } catch (Exception ignored) {
                    }
                }
            });
        }


// =====================================================
// THÔNG BÁO VÀ CHUYỂN HƯỚNG TỨC THÌ
// =====================================================

        session.setAttribute(
                "thongBao",
                thongBao
        );


        session.removeAttribute(
                "danhSachTu"
        );


        return "redirect:/them-tu";
    }
}