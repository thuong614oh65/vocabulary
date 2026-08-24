package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.HocDTO;
import com.thuong.vocabulary.dto.ThemTuDTO;
import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.service.DanhSachTuService;
import com.thuong.vocabulary.service.HocService;
import com.thuong.vocabulary.service.TuVungService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    private final DanhSachTuService danhSachTuService;
    private final TuVungService tuVungService;
    private final HocService hocService;

    public HomeController(
            DanhSachTuService danhSachTuService,
            TuVungService tuVungService,
            HocService hocService
    ) {
        this.danhSachTuService = danhSachTuService;
        this.tuVungService = tuVungService;
        this.hocService = hocService;
    }


    // =========================================================
    // KIỂM TRA ĐĂNG NHẬP
    // =========================================================

    private boolean chuaDangNhap(HttpSession session) {
        return session.getAttribute("taiKhoan") == null;
    }


    // =========================================================
    // TRANG CHỦ
    // =========================================================

    @GetMapping("/")
    public String home(HttpSession session) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }

        return "index";
    }


    // =========================================================
    // THÊM TỪ
    // =========================================================

    @GetMapping("/them-tu")
    public String themTu(
            HttpSession session,
            Model model
    ) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }

        model.addAttribute(
                "themTuDTO",
                new ThemTuDTO()
        );

        return "them-tu";
    }


    // =========================================================
    // TRA TỪ HÀNG LOẠT
    // =========================================================

    @PostMapping("/tra-hang-loat")
    public String traHangLoat(
            @ModelAttribute ThemTuDTO themTuDTO,
            Model model,
            HttpSession session
    ) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }

        List<String> danhSach =
                danhSachTuService.tachDanhSach(
                        themTuDTO.getNoiDung()
                );

        List<TuVungDTO> ketQua =
                new ArrayList<>();

        List<String> loi =
                new ArrayList<>();


        for (String tu : danhSach) {

            try {

                TuVungDTO dto =
                        tuVungService.traTu(tu);

                if (dto != null) {
                    ketQua.add(dto);
                } else {
                    loi.add(tu);
                }

            } catch (Exception e) {

                loi.add(tu);
            }
        }


        model.addAttribute(
                "themTuDTO",
                themTuDTO
        );

        model.addAttribute(
                "ketQua",
                ketQua
        );


        if (!loi.isEmpty()) {

            session.setAttribute(
                    "thongBao",
                    "Không tìm thấy: "
                            + String.join(", ", loi)
            );
        }


        session.setAttribute(
                "danhSachTu",
                ketQua
        );


        return "them-tu";
    }


    // =========================================================
    // BẮT ĐẦU HỌC
    // =========================================================

    @PostMapping("/hoc")
    public String batDauHoc(
            @ModelAttribute HocDTO hocDTO,
            Model model,
            HttpSession session
    ) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }


        // LẤY TÀI KHOẢN ĐANG ĐĂNG NHẬP
        TaiKhoan taiKhoan =
                (TaiKhoan) session.getAttribute("taiKhoan");

        Long taiKhoanId =
                taiKhoan.getId();


        System.out.println(
                "Tài khoản đang học: "
                        + taiKhoanId
        );


        List<TuVung> dsHoc =
                new ArrayList<>();


        switch (hocDTO.getKieuHoc()) {


            // -------------------------------------------------
            // NGẪU NHIÊN
            // -------------------------------------------------

            case "NGAU_NHIEN":

                dsHoc =
                        hocService.layNgauNhien(
                                taiKhoanId
                        );

                break;


            // -------------------------------------------------
            // THEO BỘ
            // -------------------------------------------------

            case "THEO_BO":

                dsHoc =
                        hocService.layTheoBo(
                                hocDTO.getBoId(),
                                taiKhoanId
                        );

                if (dsHoc != null && !dsHoc.isEmpty()) {
                    if ("TU_DEN".equals(hocDTO.getPhamViBo())) {
                        int total = dsHoc.size();
                        int from = (hocDTO.getTuTu() != null) ? hocDTO.getTuTu() : 1;
                        int to = (hocDTO.getDenTu() != null) ? hocDTO.getDenTu() : total;
                        if (from > to) {
                            int temp = from;
                            from = to;
                            to = temp;
                        }
                        from = Math.max(1, Math.min(from, total));
                        to = Math.max(1, Math.min(to, total));
                        if (from <= to) {
                            dsHoc = new ArrayList<>(dsHoc.subList(from - 1, to));
                        }
                    }
                }

                break;


            // -------------------------------------------------
            // CHỌN TỪNG TỪ
            // -------------------------------------------------

            case "CHON_TUNG_TU":

                dsHoc =
                        hocService.layTheoIds(
                                hocDTO.getTuIds(),
                                taiKhoanId
                        );

                break;


            // -------------------------------------------------
            // TỪ SAI
            // -------------------------------------------------

            case "TU_SAI":

                dsHoc =
                        hocService.layTuSai(
                                taiKhoanId
                        );

                break;
        }


        model.addAttribute(
                "hocDTO",
                hocDTO
        );

        model.addAttribute(
                "dsHoc",
                dsHoc
        );


        session.setAttribute(
                "dsHoc",
                dsHoc
        );


        return "hoc-bat-dau";
    }


    // =========================================================
    // BẮT ĐẦU VÒNG HỌC
    // =========================================================

    @PostMapping("/bat-dau-hoc")
    public String batDauHoc(
            HttpSession session
    ) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }


        List<TuVung> dsHoc =
                (List<TuVung>)
                        session.getAttribute(
                                "dsHoc"
                        );


        if (dsHoc == null || dsHoc.isEmpty()) {
            return "redirect:/";
        }


        Collections.shuffle(dsHoc);


        session.setAttribute(
                "tuDangHoc",
                dsHoc
        );


        session.setAttribute(
                "luotHoc",
                1
        );


        return "hoc-chon";
    }


    // =========================================================
    // TIẾP TỤC LƯỢT HỌC
    // =========================================================

    @GetMapping("/hoc/tiep")
    public String tiepLuot(
            HttpSession session,
            Model model
    ) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }


        Integer luot =
                (Integer)
                        session.getAttribute(
                                "luotHoc"
                        );


        if (luot == null) {
            return "redirect:/";
        }


        luot++;


        List<TuVung> dsHoc =
                (List<TuVung>)
                        session.getAttribute(
                                "tuDangHoc"
                        );


        if (dsHoc == null || dsHoc.isEmpty()) {
            return "redirect:/";
        }


        Collections.shuffle(dsHoc);


        session.setAttribute(
                "luotHoc",
                luot
        );


        session.setAttribute(
                "tuDangHoc",
                dsHoc
        );


        model.addAttribute(
                "dsHoc",
                dsHoc
        );


        if (luot % 2 == 1) {
            return "hoc-chon";
        } else {
            return "hoc-luot2";
        }
    }


    // =========================================================
    // DỪNG HỌC
    // =========================================================

    @GetMapping("/hoc/dung")
    public String dungHoc(
            HttpSession session
    ) {

        if (chuaDangNhap(session)) {
            return "redirect:/dangnhap";
        }


        session.removeAttribute("tuDangHoc");

        session.removeAttribute("dsHoc");

        session.removeAttribute("luotHoc");


        return "redirect:/";
    }


    // =========================================================
    // TĂNG SỐ LẦN SAI
    // =========================================================

    @ResponseBody
    @PostMapping("/hoc/sai/{id}")
    public void tangSoLanSai(
            @PathVariable Long id,
            HttpSession session
    ) {

        if (chuaDangNhap(session)) {
            return;
        }


        TaiKhoan taiKhoan =
                (TaiKhoan)
                        session.getAttribute(
                                "taiKhoan"
                        );


        Long taiKhoanId =
                taiKhoan.getId();


        hocService.tangSoLanSai(
                id,
                taiKhoanId
        );
    }


    // =========================================================
    // GIẢM SỐ LẦN SAI
    // =========================================================

    @ResponseBody
    @PostMapping("/hoc/dung/{id}")
    public void giamSoLanSai(
            @PathVariable Long id,
            HttpSession session
    ) {

        if (chuaDangNhap(session)) {
            return;
        }


        TaiKhoan taiKhoan =
                (TaiKhoan)
                        session.getAttribute(
                                "taiKhoan"
                        );


        Long taiKhoanId =
                taiKhoan.getId();


        hocService.giamSoLanSai(
                id,
                taiKhoanId
        );
    }
}