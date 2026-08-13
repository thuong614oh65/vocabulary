package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.HocDTO;
import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.BoTuVungService;
import com.thuong.vocabulary.service.HocService;
import com.thuong.vocabulary.service.LuuTuVungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thuong.vocabulary.service.AudioService;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HocController {

    private final BoTuVungService boTuVungService;

    private final HocService hocService;

    private final LuuTuVungService luuTuVungService;

    private final AudioService audioService;

    public HocController(
            BoTuVungService boTuVungService,
            HocService hocService,
            LuuTuVungService luuTuVungService,
            AudioService audioService
    ) {
        this.boTuVungService = boTuVungService;
        this.hocService = hocService;
        this.luuTuVungService = luuTuVungService;
        this.audioService = audioService;
    }


    // =====================================================
    // TRANG CHỌN HỌC
    // =====================================================

    @GetMapping("/hoc")
    public String hoc(
            Model model,
            HttpSession session
    ) {

        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        // Chưa đăng nhập
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();

        HocDTO dto = new HocDTO();

        dto.setKieuHoc("NGAU_NHIEN");

        model.addAttribute(
                "hocDTO",
                dto
        );


        // CHỈ LẤY BỘ CỦA USER ĐANG ĐĂNG NHẬP
        model.addAttribute(
                "dsBo",
                boTuVungService.layDanhSachBo(taiKhoanId)
        );


        // CHỈ LẤY TỪ CỦA USER ĐANG ĐĂNG NHẬP
        model.addAttribute(
                "dsTatCa",
                hocService.layTatCa(taiKhoanId)
        );


        model.addAttribute(
                "dsTheoBo",
                new ArrayList<>()
        );

        return "hoc";
    }


    // =====================================================
    // HỌC THEO BỘ
    // =====================================================

    @GetMapping("/hoc/bo/{id}")
    public String hocTheoBo(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {

        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        // Chưa đăng nhập
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();


        // Kiểm tra bộ có thuộc user này không
        if (boTuVungService.timBo(id, taiKhoanId) == null) {
            return "redirect:/hoc";
        }


        HocDTO dto = new HocDTO();

        dto.setKieuHoc("THEO_BO");

        dto.setBoId(id);


        // CHỈ LẤY BỘ CỦA USER
        model.addAttribute(
                "dsBo",
                boTuVungService.layDanhSachBo(taiKhoanId)
        );


        // CHỈ LẤY TỪ CỦA USER
        model.addAttribute(
                "dsTatCa",
                hocService.layTatCa(taiKhoanId)
        );


        // CHỈ LẤY TỪ TRONG BỘ CỦA USER
        model.addAttribute(
                "dsTheoBo",
                hocService.layTheoBo(
                        id,
                        taiKhoanId
                )
        );


        model.addAttribute(
                "hocDTO",
                dto
        );

        return "hoc";
    }


    // =====================================================
    // LƯU BỘ TỪ
    // =====================================================

    @PostMapping("/luu-bo")
    public String luuBo(

            @RequestParam List<String> tiengAnh,

            @RequestParam List<String> tiengViet,

            @RequestParam(required = false)
            List<String> phienAm,

            @RequestParam(required = false)
            List<String> viDu,

            HttpSession session
    ) {

        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        // Chưa đăng nhập
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        List<TuVungDTO> danhSach =
                new ArrayList<>();


        for (int i = 0; i < tiengAnh.size(); i++) {

            TuVungDTO dto =
                    new TuVungDTO();


            dto.setTiengAnh(
                    tiengAnh.get(i)
            );


            dto.setTiengViet(
                    tiengViet.get(i)
            );


            if (phienAm != null
                    && i < phienAm.size()) {

                dto.setPhienAm(
                        phienAm.get(i)
                );
            }


            if (viDu != null
                    && i < viDu.size()) {

                dto.setViDu(
                        viDu.get(i)
                );
            }


            danhSach.add(dto);
        }

        String thongBao =
                luuTuVungService.luuBo(
                        danhSach,
                        taiKhoan
                );


// =====================================================
// TẠO AUDIO CHO CÁC TỪ VỪA LƯU
// =====================================================

        List<String> loiAudio = new ArrayList<>();


        for (TuVungDTO dto : danhSach) {

            try {

                audioService.taoAudio(
                        dto.getTiengAnh()
                );

            } catch (Exception e) {

                loiAudio.add(
                        dto.getTiengAnh()
                );

                e.printStackTrace();
            }
        }


// =====================================================
// THÔNG BÁO
// =====================================================

        if (!loiAudio.isEmpty()) {

            thongBao +=
                    " | Không tạo được audio: "
                            + String.join(
                            ", ",
                            loiAudio
                    );
        }


        session.setAttribute(
                "thongBao",
                thongBao
        );


        session.removeAttribute(
                "danhSachTu"
        );


        return "redirect:/them-tu";
    }
}