package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.tratu.LuuTuNhanhRequest;
import com.thuong.vocabulary.dto.tratu.TraTuRequest;
import com.thuong.vocabulary.dto.tratu.TraTuResponse;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.service.TraTuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class TraTuController {

    private final TraTuService traTuService;
    private final BoTuVungRepository boTuVungRepository;

    public TraTuController(
            TraTuService traTuService,
            BoTuVungRepository boTuVungRepository
    ) {
        this.traTuService = traTuService;
        this.boTuVungRepository = boTuVungRepository;
    }

    private boolean chuaDangNhap(HttpSession session) {
        return session.getAttribute("taiKhoan") == null;
    }

    private TaiKhoan layTaiKhoanDangNhap(HttpSession session) {
        return (TaiKhoan) session.getAttribute("taiKhoan");
    }

    // =========================================================
    // 1. HIỂN THỊ TRANG TRA TỪ & DỊCH THUẬT
    // =========================================================
    @GetMapping("/tra-tu")
    public String trangTraTu(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String mode,
            HttpSession session,
            Model model
    ) {
        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }

        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        List<BoTuVung> dsBo = boTuVungRepository.findByTaiKhoanId(taiKhoan.getId());

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("tuKhoaBanDau", q != null ? q.trim() : "");
        model.addAttribute("cheDoBanDau", mode != null ? mode.trim() : "AUTO");

        return "tra-tu";
    }

    // =========================================================
    // 2. API TRA TỪ & DỊCH (ANH -> VIỆT / VIỆT -> ANH / AUTO)
    // =========================================================
    @PostMapping("/api/tra-tu/dich")
    @ResponseBody
    public ResponseEntity<TraTuResponse> apiTraTu(
            @RequestBody TraTuRequest request,
            HttpSession session
    ) {
        if (chuaDangNhap(session)) {
            TraTuResponse res = new TraTuResponse();
            res.setThanhCong(false);
            res.setThongBaoLoi("Vui lòng đăng nhập để sử dụng tính năng.");
            return ResponseEntity.status(401).body(res);
        }

        try {
            TraTuResponse response = traTuService.traTu(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            TraTuResponse res = new TraTuResponse();
            res.setThanhCong(false);
            res.setThongBaoLoi("Lỗi khi tra từ: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    // =========================================================
    // 3. API LƯU NHANH TỪ VÀO BỘ TỪ CỦA TÀI KHOẢN
    // =========================================================
    @PostMapping("/api/tra-tu/luu-nhanh")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiLuuNhanh(
            @RequestBody LuuTuNhanhRequest request,
            HttpSession session
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập! Vui lòng đăng nhập để lưu từ."
            ));
        }

        try {
            String ketQua = traTuService.luuTuNhanh(request, taiKhoan.getId());
            boolean success = ketQua.startsWith("SUCCESS:");
            String thongBao = success ? ketQua.substring("SUCCESS:".length()).trim() : ketQua;

            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", thongBao
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu từ: " + e.getMessage()
            ));
        }
    }
}
