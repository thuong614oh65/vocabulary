package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.TaiKhoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DangNhapController {

    private final TaiKhoanService taiKhoanService;

    public DangNhapController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping("/dangnhap")
    public String hienThiDangNhap() {
        return "dang-nhap";
    }

    @PostMapping("/dangnhap")
    public String dangNhap(
            @RequestParam String tenDangNhap,
            @RequestParam String matKhau,
            HttpSession session,
            Model model
    ) {

        return taiKhoanService
                .dangNhap(tenDangNhap, matKhau)
                .map(taiKhoan -> {

                    session.setAttribute("taiKhoan", taiKhoan);

                    return "redirect:/";

                })
                .orElseGet(() -> {

                    model.addAttribute(
                            "loi",
                            "Tên đăng nhập hoặc mật khẩu không đúng"
                    );

                    return "dang-nhap";
                });
    }

    @GetMapping("/dangxuat")
    public String dangXuat(HttpSession session) {

        session.invalidate();

        return "redirect:/dangnhap";
    }
}