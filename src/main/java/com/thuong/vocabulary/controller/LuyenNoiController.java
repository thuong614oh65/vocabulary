package com.thuong.vocabulary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.service.BoTuVungService;
import com.thuong.vocabulary.service.GeminiService;
import com.thuong.vocabulary.service.HocService;
import com.thuong.vocabulary.service.PhienAmService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
public class LuyenNoiController {

    private final BoTuVungService boTuVungService;
    private final HocService hocService;
    private final PhienAmService phienAmService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public LuyenNoiController(
            BoTuVungService boTuVungService,
            HocService hocService,
            PhienAmService phienAmService,
            GeminiService geminiService
    ) {
        this.boTuVungService = boTuVungService;
        this.hocService = hocService;
        this.phienAmService = phienAmService;
        this.geminiService = geminiService;
        this.objectMapper = new ObjectMapper();
    }

    private TaiKhoan layTaiKhoanDangNhap(HttpSession session) {
        return (TaiKhoan) session.getAttribute("taiKhoan");
    }

    // =========================================================
    // TRANG CHỌN CHẾ ĐỘ LUYỆN NÓI
    // =========================================================
    @GetMapping("/luyen-noi")
    public String trangChonLuyenNoi(
            @RequestParam(required = false) Long boId,
            @RequestParam(required = false, defaultValue = "NGAU_NHIEN") String kieuHoc,
            HttpSession session,
            Model model
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();
        List<BoTuVung> dsBo = boTuVungService.layDanhSachBo(taiKhoanId);
        List<TuVung> tatCaTu = hocService.layTatCa(taiKhoanId);

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("tongSoTu", tatCaTu.size());
        model.addAttribute("boIdChon", boId);
        model.addAttribute("kieuHocChon", kieuHoc);

        return "luyen-noi";
    }

    // =========================================================
    // BẮT ĐẦU LUYỆN NÓI
    // =========================================================
    @PostMapping("/luyen-noi/bat-dau")
    public String batDauLuyenNoi(
            @RequestParam(defaultValue = "NGAU_NHIEN") String kieuHoc,
            @RequestParam(required = false) Long boId,
            @RequestParam(required = false, defaultValue = "10") Integer soTu,
            HttpSession session,
            Model model
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();
        List<TuVung> dsTuGoc;

        if ("THEO_BO".equalsIgnoreCase(kieuHoc) && boId != null && boId > 0) {
            dsTuGoc = hocService.layTheoBo(boId, taiKhoanId);
        } else if ("TU_SAI".equalsIgnoreCase(kieuHoc)) {
            dsTuGoc = hocService.layTuSai(taiKhoanId);
            if (dsTuGoc.isEmpty()) {
                dsTuGoc = hocService.layNgauNhien(taiKhoanId);
            }
        } else {
            dsTuGoc = hocService.layNgauNhien(taiKhoanId);
        }

        if (dsTuGoc == null || dsTuGoc.isEmpty()) {
            model.addAttribute("loi", "Bạn chưa có từ vựng nào trong danh sách hoặc bộ từ đang chọn chưa có từ!");
            model.addAttribute("dsBo", boTuVungService.layDanhSachBo(taiKhoanId));
            model.addAttribute("tongSoTu", 0);
            return "luyen-noi";
        }

        // Xáo trộn danh sách từ
        List<TuVung> dsLuyen = new ArrayList<>(dsTuGoc);
        Collections.shuffle(dsLuyen);

        // Giới hạn số từ nếu cần
        int limit = (soTu != null && soTu > 0) ? Math.min(soTu, dsLuyen.size()) : dsLuyen.size();
        dsLuyen = dsLuyen.subList(0, limit);

        // Đảm bảo các từ đều có phiên âm IPA chuẩn từ PhienAmService và chuyển sang TuVungDTO thuần túy
        List<com.thuong.vocabulary.dto.TuVungDTO> dsLuyenDTO = new ArrayList<>();
        for (TuVung t : dsLuyen) {
            com.thuong.vocabulary.dto.TuVungDTO dto = new com.thuong.vocabulary.dto.TuVungDTO();
            dto.setTiengAnh(t.getTiengAnh());
            dto.setTiengViet(t.getTiengViet());

            String ipa = t.getPhienAm();
            if (ipa == null || ipa.isBlank()) {
                ipa = phienAmService.layPhienAm(t.getTiengAnh());
            }
            dto.setPhienAm(ipa);
            dto.setViDu(t.getViDu());
            dsLuyenDTO.add(dto);
        }

        model.addAttribute("dsLuyen", dsLuyenDTO);
        model.addAttribute("tongSoCauLuyen", dsLuyenDTO.size());
        model.addAttribute("kieuHoc", kieuHoc);
        model.addAttribute("boId", boId);
        model.addAttribute("dsBo", boTuVungService.layDanhSachBo(taiKhoanId));
        model.addAttribute("tongSoTu", hocService.layTatCa(taiKhoanId).size());

        return "luyen-noi";
    }

    // =========================================================
    // API CHẤM ĐIỂM PHÁT ÂM QUA AUDIO FILE BẰNG GEMINI TRÊN SERVER
    // =========================================================
    @PostMapping(value = "/api/luyen-noi/cham-diem-audio", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> chamDiemAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tuGoc") String tuGoc,
            @RequestParam(value = "phienAm", required = false) String phienAm,
            HttpSession session
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body("{\"error\": \"Chưa đăng nhập\"}");
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Không nhận được file âm thanh\"}");
        }

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType();
            String jsonKetQua = geminiService.chamDiemPhatAmAudio(bytes, contentType, tuGoc, phienAm);
            return ResponseEntity.ok(jsonKetQua);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
