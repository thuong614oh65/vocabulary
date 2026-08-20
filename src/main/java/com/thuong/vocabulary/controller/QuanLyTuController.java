package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.CapNhatTuDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.service.QuanLyTuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/quan-ly-tu")
public class QuanLyTuController {

    private final QuanLyTuService quanLyTuService;

    public QuanLyTuController(QuanLyTuService quanLyTuService) {
        this.quanLyTuService = quanLyTuService;
    }

    private TaiKhoan layTaiKhoanDangNhap(HttpSession session) {
        return (TaiKhoan) session.getAttribute("taiKhoan");
    }

    // =========================================================
    // HIỂN THỊ TRANG QUẢN LÝ TỪ CỦA MÌNH
    // =========================================================
    @GetMapping
    public String hienThiTrangQuanLy(
            @RequestParam(required = false) Long boId,
            HttpSession session,
            Model model
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();
        List<BoTuVung> dsBo = quanLyTuService.layDanhSachBo(taiKhoanId);
        List<TuVung> dsTu;

        if (boId != null && boId > 0) {
            dsTu = quanLyTuService.layTuTheoBo(boId, taiKhoanId);
        } else {
            dsTu = quanLyTuService.layTatCaTu(taiKhoanId);
        }

        model.addAttribute("dsTu", dsTu);
        model.addAttribute("dsBo", dsBo);
        model.addAttribute("boIdHienTai", boId);
        model.addAttribute("tongSoTu", dsTu.size());
        model.addAttribute("capNhatTuDTO", new CapNhatTuDTO());

        return "quan-ly-tu";
    }

    // =========================================================
    // CẬP NHẬT / CHỈNH SỬA TỪ VỰNG
    // =========================================================
    @PostMapping("/cap-nhat")
    public String capNhatTu(
            @ModelAttribute CapNhatTuDTO capNhatTuDTO,
            @RequestParam(required = false) Long boIdLoc,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        boolean thanhCong = quanLyTuService.capNhatTu(capNhatTuDTO, taiKhoan.getId());
        if (thanhCong) {
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã cập nhật từ vựng \"" + capNhatTuDTO.getTiengAnh() + "\" thành công!");
        } else {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Cập nhật từ vựng thất bại hoặc bạn không có quyền sửa từ này!");
        }

        if (boIdLoc != null && boIdLoc > 0) {
            return "redirect:/quan-ly-tu?boId=" + boIdLoc;
        }
        return "redirect:/quan-ly-tu";
    }

    // =========================================================
    // XÓA TỪ VỰNG
    // =========================================================
    @PostMapping("/xoa/{id}")
    public String xoaTu(
            @PathVariable Long id,
            @RequestParam(required = false) Long boIdLoc,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        boolean thanhCong = quanLyTuService.xoaTu(id, taiKhoan.getId());
        if (thanhCong) {
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã xóa từ vựng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Xóa từ vựng thất bại hoặc không tìm thấy từ!");
        }

        if (boIdLoc != null && boIdLoc > 0) {
            return "redirect:/quan-ly-tu?boId=" + boIdLoc;
        }
        return "redirect:/quan-ly-tu";
    }
}
