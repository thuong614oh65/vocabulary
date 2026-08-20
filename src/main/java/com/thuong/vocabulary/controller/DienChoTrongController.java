package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.BoTuVungService;
import com.thuong.vocabulary.service.GeminiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/dien-cho-trong")
public class DienChoTrongController {

    private final TuVungRepository tuVungRepository;
    private final BoTuVungService boTuVungService;
    private final GeminiService geminiService;

    public DienChoTrongController(
            TuVungRepository tuVungRepository,
            BoTuVungService boTuVungService,
            GeminiService geminiService
    ) {
        this.tuVungRepository = tuVungRepository;
        this.boTuVungService = boTuVungService;
        this.geminiService = geminiService;
    }

    private TaiKhoan layTaiKhoan(HttpSession session) {
        return (TaiKhoan) session.getAttribute("taiKhoan");
    }

    // =========================================================
    // MỞ TRANG ĐIỀN CHỖ TRỐNG
    // =========================================================
    @GetMapping
    public String hienThiTrangDienChoTrong(
            @RequestParam(required = false) Long boId,
            HttpSession session,
            Model model
    ) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();
        List<BoTuVung> dsBo = boTuVungService.layDanhSachBo(taiKhoanId);
        List<TuVung> dsTu = (boId != null && boId > 0)
                ? tuVungRepository.findAllByBoTuVungIdAndBoTuVungTaiKhoanId(boId, taiKhoanId)
                : tuVungRepository.findAllByBoTuVungTaiKhoanId(taiKhoanId);

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("boIdChon", boId);
        model.addAttribute("tongSoTu", dsTu.size());

        return "dien-cho-trong";
    }

    // =========================================================
    // TẠO BÀI TẬP ĐIỀN TỪ BẰNG AI
    // =========================================================
    @PostMapping("/tao")
    public String taoBaiTap(
            @RequestParam(required = false) Long boId,
            HttpSession session,
            Model model
    ) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();
        List<BoTuVung> dsBo = boTuVungService.layDanhSachBo(taiKhoanId);
        List<TuVung> dsTu = (boId != null && boId > 0)
                ? tuVungRepository.findAllByBoTuVungIdAndBoTuVungTaiKhoanId(boId, taiKhoanId)
                : tuVungRepository.findAllByBoTuVungTaiKhoanId(taiKhoanId);

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("boIdChon", boId);
        model.addAttribute("tongSoTu", dsTu.size());

        if (dsTu.isEmpty()) {
            model.addAttribute("loi", "Bạn chưa có từ vựng nào trong danh sách được chọn để tạo bài tập!");
            return "dien-cho-trong";
        }

        // Chọn ngẫu nhiên tối đa 25 từ để gửi cho AI
        List<TuVung> tuNgauNhien = new ArrayList<>(dsTu);
        Collections.shuffle(tuNgauNhien);
        int soLuong = Math.min(25, tuNgauNhien.size());
        List<TuVung> tuDuocChon = tuNgauNhien.subList(0, soLuong);

        StringBuilder danhSachTu = new StringBuilder();
        for (TuVung tu : tuDuocChon) {
            String ta = tu.getTiengAnh() != null ? tu.getTiengAnh().trim() : "";
            String tv = tu.getTiengViet() != null ? tu.getTiengViet().trim() : "";
            if (!ta.isEmpty()) {
                danhSachTu.append("- ").append(ta);
                if (!tv.isEmpty()) {
                    danhSachTu.append(" (nghĩa: ").append(tv).append(")");
                }
                danhSachTu.append("\n");
            }
        }

        try {
            String doanVanRaw = geminiService.taoDoanVanDienTu(danhSachTu.toString());
            model.addAttribute("doanVanRaw", doanVanRaw);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("loi", "AI đang quá tải hoặc gặp sự cố tạm thời. Vui lòng thử lại sau 30 giây.");
        }

        return "dien-cho-trong";
    }
}
