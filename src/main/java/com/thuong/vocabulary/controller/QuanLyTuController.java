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

        // Tính số lượng từ trong mỗi bộ theo danh sách đang hiển thị (để làm rowspan)
        // và gán chỉ số màu xen kẽ cho từng bộ
        java.util.Map<Long, Integer> soLuongTuTheoBo = new java.util.LinkedHashMap<>();
        java.util.Map<Long, Integer> boColorIndex = new java.util.LinkedHashMap<>();
        int colorIdx = 0;

        if (dsTu != null) {
            for (TuVung tu : dsTu) {
                Long idBo = (tu.getBoTuVung() != null) ? tu.getBoTuVung().getId() : -1L;
                soLuongTuTheoBo.put(idBo, soLuongTuTheoBo.getOrDefault(idBo, 0) + 1);
                if (!boColorIndex.containsKey(idBo)) {
                    boColorIndex.put(idBo, colorIdx++);
                }
            }
        }

        model.addAttribute("dsTu", dsTu);
        model.addAttribute("dsBo", dsBo);
        model.addAttribute("soLuongTuTheoBo", soLuongTuTheoBo);
        model.addAttribute("boColorIndex", boColorIndex);
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

    // =========================================================
    // XÓA BỘ TỪ VỰNG (KÈM TOÀN BỘ TỪ TRONG BỘ)
    // =========================================================
    @PostMapping("/xoa-bo/{boId}")
    public String xoaBo(
            @PathVariable Long boId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        boolean thanhCong = quanLyTuService.xoaBo(boId, taiKhoan.getId());
        if (thanhCong) {
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã xóa bộ từ vựng và toàn bộ từ trong bộ thành công!");
        } else {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Xóa bộ từ vựng thất bại hoặc không tìm thấy bộ từ!");
        }

        return "redirect:/quan-ly-tu";
    }

    // =========================================================
    // SỬA / ĐỔI TÊN BỘ TỪ VỰNG
    // =========================================================
    @PostMapping("/sua-bo")
    public String suaTenBo(
            @RequestParam Long boId,
            @RequestParam String tenBoMoi,
            @RequestParam(required = false) Long boIdLoc,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        if (tenBoMoi == null || tenBoMoi.isBlank()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Tên bộ từ không được để trống!");
            if (boIdLoc != null && boIdLoc > 0) return "redirect:/quan-ly-tu?boId=" + boIdLoc;
            return "redirect:/quan-ly-tu";
        }

        boolean thanhCong = quanLyTuService.suaTenBo(boId, tenBoMoi, taiKhoan.getId());
        if (thanhCong) {
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã đổi tên bộ từ thành \"" + tenBoMoi.trim() + "\" thành công!");
        } else {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Đổi tên bộ từ thất bại (tên bộ có thể đã trùng lặp hoặc không tìm thấy bộ từ)!");
        }

        if (boIdLoc != null && boIdLoc > 0) {
            return "redirect:/quan-ly-tu?boId=" + boIdLoc;
        }
        return "redirect:/quan-ly-tu";
    }

    // =========================================================
    // TẠO BỘ TỪ VỰNG MỚI
    // =========================================================
    @PostMapping("/tao-bo")
    public String taoBoMoi(
            @RequestParam String tenBo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        TaiKhoan taiKhoan = layTaiKhoanDangNhap(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        if (tenBo == null || tenBo.isBlank()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Tên bộ từ mới không được để trống!");
            return "redirect:/quan-ly-tu";
        }

        BoTuVung bo = quanLyTuService.taoBoMoi(tenBo, taiKhoan.getId());
        if (bo != null) {
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã tạo bộ từ mới \"" + bo.getTenBo() + "\" thành công!");
            return "redirect:/quan-ly-tu?boId=" + bo.getId();
        } else {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Tạo bộ từ thất bại! Tên bộ \"" + tenBo.trim() + "\" đã tồn tại trong tài khoản của bạn.");
            return "redirect:/quan-ly-tu";
        }
    }
}

