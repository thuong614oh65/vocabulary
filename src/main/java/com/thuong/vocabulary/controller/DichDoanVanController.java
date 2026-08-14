package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.DichNghiaService;
import com.thuong.vocabulary.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class DichDoanVanController {

    private final TuVungRepository tuVungRepository;
    private final GeminiService geminiService;

    private final DichNghiaService dichNghiaService;

    public DichDoanVanController(
            TuVungRepository tuVungRepository,
            GeminiService geminiService,
            DichNghiaService dichNghiaService
    ) {

        this.tuVungRepository = tuVungRepository;
        this.geminiService = geminiService;
        this.dichNghiaService =
                dichNghiaService;
    }


    // =====================================================
    // MỞ TRANG DỊCH ĐOẠN VĂN
    // =====================================================

    @GetMapping("/dich-doan-van")
    public String dichDoanVan(Model model) {

        List<TuVung> dsTuVung =
                tuVungRepository.findAll();

        model.addAttribute(
                "dsTuVung",
                dsTuVung
        );

        return "dich-doan-van";
    }


    // =====================================================
    // TẠO ĐOẠN VĂN
    // =====================================================

    @PostMapping("/dich-doan-van")
    public String taoDoanVan(Model model) {

        // Lấy toàn bộ từ trong CSDL

        List<TuVung> dsTuVung =
                tuVungRepository.findAll();


        // =================================================
        // KIỂM TRA CÓ TỪ VỰNG HAY KHÔNG
        // =================================================

        if (dsTuVung.isEmpty()) {

            model.addAttribute(
                    "loi",
                    "Bạn chưa có từ vựng nào trong cơ sở dữ liệu."
            );

            model.addAttribute(
                    "dsTuVung",
                    dsTuVung
            );

            return "dich-doan-van";
        }


        // =================================================
        // TRỘN TỪ NGẪU NHIÊN
        // =================================================

        List<TuVung> tuNgauNhien =
                new ArrayList<>(dsTuVung);

        Collections.shuffle(tuNgauNhien);


        // =================================================
        // CHỌN TỐI ĐA 50 TỪ
        // =================================================

        int soLuong =
                Math.min(50, tuNgauNhien.size());

        List<TuVung> tuDuocChon =
                tuNgauNhien.subList(0, soLuong);


        // =================================================
        // TẠO DANH SÁCH TỪ TIẾNG ANH
        // ĐỂ GỬI CHO GEMINI
        // =================================================

        StringBuilder danhSachTu =
                new StringBuilder();

        for (TuVung tu : tuDuocChon) {

            danhSachTu
                    .append(tu.getTiengAnh())
                    .append(", ");
        }


        // =================================================
        // GỌI GEMINI
        // =================================================

        String doanVan;

        try {

            doanVan =
                    geminiService.taoDoanVan(
                            danhSachTu.toString()
                    );

        } catch (Exception e) {

            System.out.println(
                    "===== LỖI KHI TẠO ĐOẠN VĂN ====="
            );

            System.out.println(
                    e.getMessage()
            );

            model.addAttribute(
                    "loi",
                    "Gemini đang hết lượt miễn phí hoặc đang quá tải. Vui lòng thử lại sau khoảng 1 phút."
            );

            model.addAttribute(
                    "dsTuVung",
                    dsTuVung
            );

            model.addAttribute(
                    "tuVungTuCSDL",
                    dsTuVung
            );

            return "dich-doan-van";
        }

        // =================================================
        // GỬI KẾT QUẢ VỀ HTML
        // =================================================

        model.addAttribute(
                "doanVan",
                doanVan
        );


// =================================================
// TẠO DỮ LIỆU TỪ VỰNG CHO JAVASCRIPT
// =================================================

        List<java.util.Map<String, String>> tuVungJS =
                new ArrayList<>();

        for (TuVung tu : dsTuVung) {

            java.util.Map<String, String> item =
                    new java.util.HashMap<>();

            item.put(
                    "tiengAnh",
                    tu.getTiengAnh() != null
                            ? tu.getTiengAnh()
                            : ""
            );

            item.put(
                    "tiengViet",
                    tu.getTiengViet() != null
                            ? tu.getTiengViet()
                            : ""
            );

            tuVungJS.add(item);
        }

        model.addAttribute(
                "tuVungTuCSDL",
                tuVungJS
        );

        // Danh sách từ đã chọn

        model.addAttribute(
                "tuDuocChon",
                tuDuocChon
        );


        // Toàn bộ từ vựng

        model.addAttribute(
                "dsTuVung",
                dsTuVung
        );


        return "dich-doan-van";
    }


    // =====================================================
// KIỂM TRA BẢN DỊCH BẰNG GEMINI
// =====================================================

    @PostMapping("/dich-doan-van/kiem-tra")
    public String kiemTraBanDich(
            @RequestParam("doanVan") String doanVan,
            @RequestParam("banDich") String banDich,
            Model model) {

        // =================================================
        // KIỂM TRA DỮ LIỆU
        // =================================================

        if (doanVan == null || doanVan.isBlank()) {

            model.addAttribute(
                    "loi",
                    "Không tìm thấy đoạn văn cần dịch."
            );

            return "dich-doan-van";
        }


        if (banDich == null || banDich.isBlank()) {

            model.addAttribute(
                    "loi",
                    "Bạn chưa nhập bản dịch."
            );

            model.addAttribute(
                    "doanVan",
                    doanVan
            );

            return "dich-doan-van";
        }


        // =================================================
        // IN RA CONSOLE
        // =================================================

        System.out.println(
                "===== ĐOẠN VĂN GỐC ====="
        );

        System.out.println(doanVan);


        System.out.println(
                "===== BẢN DỊCH CỦA NGƯỜI HỌC ====="
        );

        System.out.println(banDich);


        // =================================================
        // GỌI GEMINI ĐỂ CHẤM
        // =================================================

        String ketQua;

        try {

            ketQua =
                    geminiService.kiemTraBanDich(
                            doanVan,
                            banDich
                    );

        } catch (Exception e) {

            System.out.println(
                    "===== LỖI KHI GỌI GEMINI ====="
            );

            System.out.println(
                    e.getMessage()
            );

            model.addAttribute(
                    "loi",
                    "Gemini đang quá tải. Vui lòng thử lại sau một lát."
            );

            model.addAttribute(
                    "doanVan",
                    doanVan
            );

            model.addAttribute(
                    "banDich",
                    banDich
            );

            return "dich-doan-van";
        }


        // =================================================
        // GỬI DỮ LIỆU VỀ HTML
        // =================================================

        model.addAttribute(
                "doanVan",
                doanVan
        );

        model.addAttribute(
                "banDich",
                banDich
        );

        model.addAttribute(
                "ketQua",
                ketQua
        );

        // =================================================
// TÁCH KẾT QUẢ GEMINI
// =================================================

        String danhGia = "";
        String nhanXet = "";
        String loiHoacThieu = "";
        String banDichGoiY = "";
        String goiYCaiThien = "";

        String[] cacDong = ketQua.split("\\R");

        String phanHienTai = "";

        StringBuilder noiDung = new StringBuilder();

        for (String dong : cacDong) {

            String dongTrim = dong.trim();

            if (dongTrim.isEmpty()) {
                continue;
            }

            if (dongTrim.startsWith("ĐÁNH GIÁ:")) {

                danhGia =
                        dongTrim.substring("ĐÁNH GIÁ:".length()).trim();

                phanHienTai = "danhGia";

            } else if (dongTrim.startsWith("NHẬN XÉT:")) {

                nhanXet =
                        dongTrim.substring("NHẬN XÉT:".length()).trim();

                phanHienTai = "nhanXet";

            } else if (dongTrim.startsWith("LỖI HOẶC Ý THIẾU:")) {

                loiHoacThieu =
                        dongTrim.substring("LỖI HOẶC Ý THIẾU:".length()).trim();

                phanHienTai = "loiHoacThieu";

            } else if (dongTrim.startsWith("BẢN DỊCH GỢI Ý:")) {

                banDichGoiY =
                        dongTrim.substring("BẢN DỊCH GỢI Ý:".length()).trim();

                phanHienTai = "banDichGoiY";

            } else if (dongTrim.startsWith("GỢI Ý CẢI THIỆN:")) {

                goiYCaiThien =
                        dongTrim.substring("GỢI Ý CẢI THIỆN:".length()).trim();

                phanHienTai = "goiYCaiThien";

            } else {

                if (phanHienTai.equals("nhanXet")) {

                    nhanXet += " " + dongTrim;

                } else if (phanHienTai.equals("loiHoacThieu")) {

                    loiHoacThieu += " " + dongTrim;

                } else if (phanHienTai.equals("banDichGoiY")) {

                    banDichGoiY += " " + dongTrim;

                } else if (phanHienTai.equals("goiYCaiThien")) {

                    goiYCaiThien += " " + dongTrim;
                }
            }
        }

        model.addAttribute(
                "danhGia",
                danhGia
        );

        model.addAttribute(
                "nhanXet",
                nhanXet
        );

        model.addAttribute(
                "loiHoacThieu",
                loiHoacThieu
        );

        model.addAttribute(
                "banDichGoiY",
                banDichGoiY
        );

        model.addAttribute(
                "goiYCaiThien",
                goiYCaiThien
        );


        return "dich-doan-van";
    }

    // =====================================================
// LẤY NGHĨA TỪ TIẾNG ANH KHÔNG CÓ TRONG CSDL
// =====================================================

    @GetMapping("/dich-doan-van/nghia")
    public ResponseEntity<Map<String, String>> layNghia(
            @RequestParam String tu) {

        try {

            if (tu == null || tu.isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "nghia",
                                        "Không có từ để tra."
                                )
                        );
            }


            String nghia =
                    dichNghiaService.layNghiaTiengViet(
                            tu.trim()
                    );


            if (nghia == null || nghia.isBlank()) {

                return ResponseEntity.ok(
                        Map.of(
                                "nghia",
                                "Không tìm thấy nghĩa."
                        )
                );
            }


            return ResponseEntity.ok(
                    Map.of(
                            "nghia",
                            nghia
                    )
            );


        } catch (Exception e) {

            System.out.println(
                    "===== LỖI LẤY NGHĨA ====="
            );

            e.printStackTrace();


            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "nghia",
                                    "Không thể lấy nghĩa."
                            )
                    );
        }
    }
}