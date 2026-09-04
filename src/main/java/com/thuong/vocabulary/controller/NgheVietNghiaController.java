package com.thuong.vocabulary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/nghe-viet-nghia")
public class NgheVietNghiaController {

    private final TuVungRepository tuVungRepository;
    private final BoTuVungService boTuVungService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NgheVietNghiaController(
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
    // MỞ TRANG NGHE VÀ VIẾT LẠI NGHĨA
    // =========================================================
    @GetMapping
    public String hienThiTrang(
            @RequestParam(required = false) Long boId,
            @RequestParam(defaultValue = "1") int capDo,
            @RequestParam(defaultValue = "CAU") String hinhThuc,
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
        model.addAttribute("soCauChon", 15);
        model.addAttribute("capDoChon", capDo);
        model.addAttribute("hinhThucChon", hinhThuc);

        return "nghe-viet-nghia";
    }

    // =========================================================
    // TẠO BÀI LUYỆN NGHE VÀ VIẾT LẠI NGHĨA
    // =========================================================
    @PostMapping("/tao")
    public String taoBaiTap(
            @RequestParam(required = false) Long boId,
            @RequestParam(defaultValue = "15") int soCau,
            @RequestParam(defaultValue = "1") int capDo,
            @RequestParam(defaultValue = "CAU") String hinhThuc,
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

        if (soCau <= 0) {
            soCau = 15;
        }
        if (capDo != 1 && capDo != 2) {
            capDo = 1;
        }
        if (!"TU".equalsIgnoreCase(hinhThuc)) {
            hinhThuc = "CAU";
        }

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("boIdChon", boId);
        model.addAttribute("tongSoTu", dsTu.size());
        model.addAttribute("soCauChon", soCau);
        model.addAttribute("capDoChon", capDo);
        model.addAttribute("hinhThucChon", hinhThuc);

        if (dsTu.isEmpty()) {
            model.addAttribute("loi", "Bạn chưa có từ vựng nào trong danh sách được chọn để tạo bài luyện nghe!");
            return "nghe-viet-nghia";
        }

        // Trường hợp 1: Luyện theo TỪ VỰNG đơn
        if ("TU".equalsIgnoreCase(hinhThuc)) {
            List<TuVung> tuNgauNhien = new ArrayList<>(dsTu);
            Collections.shuffle(tuNgauNhien);
            int soLuong = Math.min(soCau, tuNgauNhien.size());
            List<TuVung> tuDuocChon = tuNgauNhien.subList(0, soLuong);

            List<Map<String, Object>> danhSachMap = new ArrayList<>();
            int stt = 1;
            for (TuVung tu : tuDuocChon) {
                String ta = tu.getTiengAnh() != null ? tu.getTiengAnh().trim() : "";
                String tv = tu.getTiengViet() != null ? tu.getTiengViet().trim() : "";
                String pa = tu.getPhienAm() != null ? tu.getPhienAm().trim() : "";
                if (!ta.isEmpty()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("num", stt++);
                    item.put("english", ta);
                    item.put("meaning", tv);
                    item.put("ipa", pa);
                    danhSachMap.add(item);
                }
            }

            try {
                String rawJson = objectMapper.writeValueAsString(danhSachMap);
                model.addAttribute("rawCauNgheDien", rawJson);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("loi", "Không thể tạo bài luyện từ vựng: " + e.getMessage());
            }

            return "nghe-viet-nghia";
        }

        // Trường hợp 2: Luyện theo CÂU NGỮ CẢNH (AI Gemini)
        List<TuVung> tuNgauNhien = new ArrayList<>(dsTu);
        Collections.shuffle(tuNgauNhien);
        int soLuong = Math.min(30, tuNgauNhien.size());
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
            String rawCauNgheDien = geminiService.taoCauNgheDien(danhSachTu.toString(), soCau, capDo);
            model.addAttribute("rawCauNgheDien", rawCauNgheDien);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("loi", "AI đang quá tải hoặc gặp sự cố tạm thời. Vui lòng thử lại sau 30 giây.");
        }

        return "nghe-viet-nghia";
    }
}
