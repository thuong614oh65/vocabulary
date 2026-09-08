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

import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.AudioService;

@Controller
public class HomeController {

    private final DanhSachTuService danhSachTuService;
    private final TuVungService tuVungService;
    private final HocService hocService;
    private final com.thuong.vocabulary.repository.BoTuVungRepository boTuVungRepository;
    private final TuVungRepository tuVungRepository;
    private final AudioService audioService;

    public HomeController(
            DanhSachTuService danhSachTuService,
            TuVungService tuVungService,
            HocService hocService,
            com.thuong.vocabulary.repository.BoTuVungRepository boTuVungRepository,
            TuVungRepository tuVungRepository,
            AudioService audioService
    ) {
        this.danhSachTuService = danhSachTuService;
        this.tuVungService = tuVungService;
        this.hocService = hocService;
        this.boTuVungRepository = boTuVungRepository;
        this.tuVungRepository = tuVungRepository;
        this.audioService = audioService;
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

        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("taiKhoan");
        List<com.thuong.vocabulary.entity.BoTuVung> dsBo = boTuVungRepository.findByTaiKhoanId(taiKhoan.getId());
        long soThuTu = boTuVungRepository.countByTaiKhoanId(taiKhoan.getId()) + 1;

        model.addAttribute(
                "themTuDTO",
                new ThemTuDTO()
        );
        model.addAttribute("dsBo", dsBo);
        model.addAttribute("tenBoGoiY", "Bộ " + soThuTu);

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

        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("taiKhoan");
        List<com.thuong.vocabulary.entity.BoTuVung> dsBo = boTuVungRepository.findByTaiKhoanId(taiKhoan.getId());
        long soThuTu = boTuVungRepository.countByTaiKhoanId(taiKhoan.getId()) + 1;

        List<String> danhSach =
                danhSachTuService.tachDanhSach(
                        themTuDTO.getNoiDung()
                );

        List<String> loi = new ArrayList<>();
        List<TuVungDTO> ketQua = tuVungService.traTuHangLoat(danhSach, loi);


        model.addAttribute(
                "themTuDTO",
                themTuDTO
        );

        model.addAttribute(
                "ketQua",
                ketQua
        );

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("tenBoGoiY", "Bộ " + soThuTu);


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
            @RequestParam(value = "action", required = false) String action,
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

            // -------------------------------------------------
            // THEO CHỦ ĐỀ (LƯU BỘ TỪ MỚI VÀO CSDL)
            // -------------------------------------------------

            case "THEO_CHU_DE":
                if (hocDTO.getChuDeTuJson() != null && !hocDTO.getChuDeTuJson().isBlank()) {
                    List<TuVungDTO> dsTuMoi = parseJsonTuVung(hocDTO.getChuDeTuJson());
                    if (dsTuMoi != null && !dsTuMoi.isEmpty()) {
                        String tenChuDe = (hocDTO.getTenChuDe() != null && !hocDTO.getTenChuDe().isBlank())
                                ? hocDTO.getTenChuDe().trim()
                                : "Chủ đề mới";

                        // Đảm bảo tạo mới một Bộ từ vào CSDL (tự động thêm số nếu trùng tên bộ)
                        String tenBoTao = tenChuDe;
                        int count = 2;
                        while (boTuVungRepository.existsByTenBoAndTaiKhoanId(tenBoTao, taiKhoanId)) {
                            tenBoTao = tenChuDe + " (" + count + ")";
                            count++;
                        }

                        com.thuong.vocabulary.entity.BoTuVung boMoi = new com.thuong.vocabulary.entity.BoTuVung();
                        boMoi.setTenBo(tenBoTao);
                        boMoi.setNgayTao(java.time.LocalDateTime.now());
                        boMoi.setTaiKhoan(taiKhoan);
                        boMoi = boTuVungRepository.save(boMoi);

                        // Lưu 10 từ vào bộ mới này trong CSDL dạng batch
                        List<TuVung> dsEntity = new ArrayList<>();
                        List<String> dsTuCanTaoAudio = new ArrayList<>();
                        for (TuVungDTO dto : dsTuMoi) {
                            if (dto.getTiengAnh() != null && !dto.getTiengAnh().isBlank()) {
                                TuVung tv = new TuVung();
                                tv.setTiengAnh(dto.getTiengAnh().trim());
                                tv.setTiengViet(dto.getTiengViet() != null ? dto.getTiengViet().trim() : "");
                                tv.setPhienAm(dto.getPhienAm() != null ? dto.getPhienAm().trim() : "");
                                tv.setViDu(dto.getViDu() != null ? dto.getViDu().trim() : "");
                                tv.setBoTuVung(boMoi);
                                dsEntity.add(tv);
                                dsTuCanTaoAudio.add(dto.getTiengAnh().trim());
                            }
                        }
                        tuVungRepository.saveAll(dsEntity);

                        // Tạo file audio phát âm chạy ngầm trong Virtual Thread
                        if (!dsTuCanTaoAudio.isEmpty()) {
                            Thread.startVirtualThread(() -> {
                                for (String tu : dsTuCanTaoAudio) {
                                    try {
                                        audioService.taoAudio(tu);
                                    } catch (Exception ignored) {}
                                }
                            });
                        }

                        // Lấy danh sách từ vừa lưu từ DB để đưa vào vòng học
                        dsHoc = tuVungRepository.findAllByBoTuVungIdAndBoTuVungTaiKhoanId(boMoi.getId(), taiKhoanId);
                        hocDTO.setBoId(boMoi.getId());
                    }
                }
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

        session.setAttribute(
                "tuDangHoc",
                dsHoc
        );

        if ("phan-xa".equalsIgnoreCase(action)) {
            return "redirect:/luyen-phan-xa?kieuHoc=DANG_HOC";
        }

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


        return "redirect:/hoc";
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

    private List<TuVungDTO> parseJsonTuVung(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(
                    json,
                    new com.fasterxml.jackson.core.type.TypeReference<List<TuVungDTO>>() {}
            );
        } catch (Exception e) {
            System.err.println("[HomeController] Lỗi parse JSON từ vựng chủ đề: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}