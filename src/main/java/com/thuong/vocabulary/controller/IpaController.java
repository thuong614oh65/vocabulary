package com.thuong.vocabulary.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IpaController {

    private boolean chuaDangNhap(HttpSession session) {
        return session.getAttribute("taiKhoan") == null;
    }

    @GetMapping("/ipa")
    public String bangIpa(HttpSession session) {
        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }
        return "ipa";
    }
}
