package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.TaiKhoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DangKyController {

    private final TaiKhoanService taiKhoanService;

    public DangKyController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    // Hiển thị trang đăng ký
    @GetMapping("/dangky")
    public String hienThiDangKy(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        return "dang-ky";
    }

    // Xử lý đăng ký
    @PostMapping("/dangky")
    public String dangKy(
            @RequestParam String tenDangNhap,
            @RequestParam String matKhau,
            @RequestParam String hoTen,
            @RequestParam(required = false) String email,
            Model model
    ) {

        try {

            TaiKhoan taiKhoan = taiKhoanService.dangKy(
                    tenDangNhap,
                    matKhau,
                    hoTen,
                    email
            );

            return "redirect:/dangnhap";

        } catch (RuntimeException e) {

            model.addAttribute("loi", e.getMessage());

            return "dang-ky";
        }
    }
}