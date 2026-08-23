package com.thuong.vocabulary.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SoDemController {

    private boolean chuaDangNhap(HttpSession session) {
        return session.getAttribute("taiKhoan") == null;
    }

    @GetMapping("/so-dem")
    public String bangSoDem(HttpSession session) {
        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }
        return "so-dem";
    }

    @GetMapping("/numbers")
    public String numbersRedirect(HttpSession session) {
        return "redirect:/so-dem";
    }
}
