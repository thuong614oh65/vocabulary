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

        // 1. Thử bóc tách qua thẻ [TAG]...[/TAG]
        danhGia = trichNoiDungThe(ketQua, "DANH_GIA");
        nhanXet = trichNoiDungThe(ketQua, "NHAN_XET");
        loiHoacThieu = trichNoiDungThe(ketQua, "LOI_HOAC_THIEU");
        if (loiHoacThieu.isEmpty()) {
            loiHoacThieu = trichNoiDungThe(ketQua, "LOI_THIEU");
        }
        banDichGoiY = trichNoiDungThe(ketQua, "BAN_DICH_GOI_Y");
        if (banDichGoiY.isEmpty()) {
            banDichGoiY = trichNoiDungThe(ketQua, "BAN_DICH_MAU");
        }
        goiYCaiThien = trichNoiDungThe(ketQua, "GOI_Y_CAI_THIEN");

        // 2. Fallback nếu Gemini trả theo dạng tiêu đề hoặc markdown thông thường
        if (danhGia.isEmpty() || nhanXet.isEmpty()) {
            String[] cacDong = ketQua.split("\\R");
            String phanHienTai = "";

            for (String dong : cacDong) {
                String dongTrim = dong.trim();
                if (dongTrim.isEmpty()) {
                    continue;
                }

                // Xóa markdown header, số thứ tự, gạch đầu dòng, dấu sao
                String dongClean = dongTrim.replaceAll("^\\s*#{1,6}\\s*", "")
                                           .replaceAll("^\\s*\\d+[\\.\\)]\\s*", "")
                                           .replaceAll("[\\*_\\[\\]]", "")
                                           .trim();
                String dongLower = dongClean.toLowerCase();

                if (dongLower.startsWith("đánh giá:") || dongLower.startsWith("danh gia:")) {
                    int colonIdx = dongClean.indexOf(":");
                    danhGia = dongClean.substring(colonIdx + 1).trim();
                    phanHienTai = "danhGia";
                } else if (dongLower.startsWith("nhận xét:") || dongLower.startsWith("nhan xet:")) {
                    int colonIdx = dongClean.indexOf(":");
                    nhanXet = dongClean.substring(colonIdx + 1).trim();
                    phanHienTai = "nhanXet";
                } else if (dongLower.startsWith("lỗi hoặc ý thiếu:") || dongLower.startsWith("loi hoac y thieu:")
                        || dongLower.startsWith("lỗi hoặc thiếu:") || dongLower.startsWith("loi hoac thieu:")
                        || dongLower.startsWith("lỗi:") || dongLower.startsWith("loi:")) {
                    int colonIdx = dongClean.indexOf(":");
                    loiHoacThieu = dongClean.substring(colonIdx + 1).trim();
                    phanHienTai = "loiHoacThieu";
                } else if (dongLower.startsWith("bản dịch gợi ý:") || dongLower.startsWith("ban dich goi y:")
                        || dongLower.startsWith("bản dịch mẫu:") || dongLower.startsWith("ban dich mau:")) {
                    int colonIdx = dongClean.indexOf(":");
                    banDichGoiY = dongClean.substring(colonIdx + 1).trim();
                    phanHienTai = "banDichGoiY";
                } else if (dongLower.startsWith("gợi ý cải thiện:") || dongLower.startsWith("goi y cai thien:")
                        || dongLower.startsWith("gợi ý:") || dongLower.startsWith("goi y:")) {
                    int colonIdx = dongClean.indexOf(":");
                    goiYCaiThien = dongClean.substring(colonIdx + 1).trim();
                    phanHienTai = "goiYCaiThien";
                } else {
                    if ("nhanXet".equals(phanHienTai)) {
                        nhanXet = (nhanXet.isEmpty() ? "" : nhanXet + "\n") + dongTrim;
                    } else if ("loiHoacThieu".equals(phanHienTai)) {
                        loiHoacThieu = (loiHoacThieu.isEmpty() ? "" : loiHoacThieu + "\n") + dongTrim;
                    } else if ("banDichGoiY".equals(phanHienTai)) {
                        banDichGoiY = (banDichGoiY.isEmpty() ? "" : banDichGoiY + "\n") + dongTrim;
                    } else if ("goiYCaiThien".equals(phanHienTai)) {
                        goiYCaiThien = (goiYCaiThien.isEmpty() ? "" : goiYCaiThien + "\n") + dongTrim;
                    }
                }
            }
        }

        // 3. Làm sạch ký tự thừa
        danhGia = lamSachVanBan(danhGia);
        nhanXet = lamSachVanBan(nhanXet);
        loiHoacThieu = lamSachVanBan(loiHoacThieu);
        banDichGoiY = lamSachVanBan(banDichGoiY);
        goiYCaiThien = lamSachVanBan(goiYCaiThien);

        // 4. Xác định loại đánh giá (class CSS & hiển thị)
        String danhGiaLoai = "dung";
        String danhGiaLower = danhGia.toLowerCase();
        if (danhGiaLower.contains("gần đúng") || danhGiaLower.contains("gan dung") || danhGiaLower.contains("tương đối")) {
            danhGiaLoai = "gan-dung";
            if (danhGia.isEmpty()) danhGia = "Gần đúng";
        } else if (danhGiaLower.contains("sai") || danhGiaLower.contains("thiếu") || danhGiaLower.contains("thieu") || danhGiaLower.contains("chưa")) {
            danhGiaLoai = "sai";
            if (danhGia.isEmpty()) danhGia = "Sai hoặc thiếu ý";
        } else {
            danhGiaLoai = "dung";
            if (danhGia.isEmpty()) danhGia = "Đúng";
        }

        // 5. Fallback nếu còn mục nào trống để không bị rỗng giao diện
        if (nhanXet.isEmpty() && !ketQua.isBlank()) {
            nhanXet = ketQua;
        }
        if (loiHoacThieu.isEmpty() || loiHoacThieu.equalsIgnoreCase("khong co loi nao") || loiHoacThieu.equalsIgnoreCase("không có lỗi nào") || loiHoacThieu.equalsIgnoreCase("none")) {
            if (danhGiaLoai.equals("dung")) {
                loiHoacThieu = "🎉 Tuyệt vời! Bản dịch rất chuẩn xác, không có lỗi sai hoặc thiếu ý nào.";
            } else {
                loiHoacThieu = "Không có lỗi sai nghiêm trọng.";
            }
        }

        model.addAttribute("danhGia", danhGia);
        model.addAttribute("danhGiaLoai", danhGiaLoai);
        model.addAttribute("nhanXet", nhanXet);
        model.addAttribute("loiHoacThieu", loiHoacThieu);
        model.addAttribute("banDichGoiY", banDichGoiY);
        model.addAttribute("goiYCaiThien", goiYCaiThien);

        return "dich-doan-van";
    }

    private String trichNoiDungThe(String vanBan, String tenThe) {
        if (vanBan == null || vanBan.isBlank()) {
            return "";
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\\[" + tenThe + "\\]([\\s\\S]*?)\\[/" + tenThe + "\\]",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(vanBan);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String lamSachVanBan(String str) {
        if (str == null) return "";
        return str.replaceAll("^[\\[\\(\"']+", "")
                  .replaceAll("[\\]\\)\"']+$", "")
                  .trim();
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