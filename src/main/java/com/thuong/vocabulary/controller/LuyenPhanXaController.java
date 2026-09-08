package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.PhanXaCauHoiDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.service.BoTuVungService;
import com.thuong.vocabulary.service.HocService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class LuyenPhanXaController {

    private final BoTuVungService boTuVungService;
    private final HocService hocService;

    public LuyenPhanXaController(BoTuVungService boTuVungService, HocService hocService) {
        this.boTuVungService = boTuVungService;
        this.hocService = hocService;
    }

    // =========================================================
    // 1. MÀN HÌNH ĐẤU TRƯỜNG PHẢN XẠ NHANH
    // =========================================================
    @GetMapping("/luyen-phan-xa")
    public String trangLuyenPhanXa(
            @RequestParam(value = "boId", required = false) Long boId,
            @RequestParam(value = "kieuHoc", required = false, defaultValue = "THEO_BO") String kieuHoc,
            Model model,
            HttpSession session
    ) {
        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("taiKhoan");
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        Long taiKhoanId = taiKhoan.getId();
        List<BoTuVung> dsBo = boTuVungService.layDanhSachBo(taiKhoanId);

        // Lấy danh sách từ đang học trong session nếu có
        @SuppressWarnings("unchecked")
        List<TuVung> dsTuDangHoc = (List<TuVung>) session.getAttribute("tuDangHoc");
        if (dsTuDangHoc == null || dsTuDangHoc.isEmpty()) {
            dsTuDangHoc = (List<TuVung>) session.getAttribute("dsHoc");
        }
        int soTuDangHoc = (dsTuDangHoc != null) ? dsTuDangHoc.size() : 0;
        model.addAttribute("soTuDangHoc", soTuDangHoc);

        // Nếu chưa chọn bộ và có danh sách bộ, mặc định chọn bộ đầu tiên (trừ khi kieuHoc là DANG_HOC)
        Long boChon = boId;
        if (boChon == null && !dsBo.isEmpty() && !"DANG_HOC".equalsIgnoreCase(kieuHoc)) {
            boChon = dsBo.get(0).getId();
        }

        String tenBoChon = "Luyện Phản Xạ";
        if ("DANG_HOC".equalsIgnoreCase(kieuHoc) && soTuDangHoc > 0) {
            tenBoChon = "Các từ đang học (" + soTuDangHoc + " từ)";
        } else if (boChon != null) {
            for (BoTuVung b : dsBo) {
                if (b.getId().equals(boChon)) {
                    tenBoChon = b.getTenBo();
                    break;
                }
            }
        }

        model.addAttribute("dsBo", dsBo);
        model.addAttribute("boIdChon", boChon);
        model.addAttribute("tenBoChon", tenBoChon);
        model.addAttribute("kieuHoc", kieuHoc);

        return "luyen-phan-xa";
    }

    @GetMapping("/luyen-phan-xa/bo/{boId}")
    public String luyenPhanXaTheoBo(
            @PathVariable Long boId,
            HttpSession session
    ) {
        return "redirect:/luyen-phan-xa?boId=" + boId + "&kieuHoc=THEO_BO";
    }

    // =========================================================
    // 2. API TRẢ VỀ DANH SÁCH CÂU HỎI PHẢN XẠ KÈM ẢNH & AUDIO
    // =========================================================
    @GetMapping("/api/luyen-phan-xa/du-lieu")
    @ResponseBody
    public ResponseEntity<?> layDuLieuPhanXa(
            @RequestParam(value = "boId", required = false) Long boId,
            @RequestParam(value = "kieuHoc", required = false, defaultValue = "THEO_BO") String kieuHoc,
            @RequestParam(value = "cheDo", required = false, defaultValue = "HINH_ANH") String cheDo,
            HttpSession session
    ) {
        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("taiKhoan");
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        Long taiKhoanId = taiKhoan.getId();
        List<TuVung> dsTuGoc = new ArrayList<>();

        if ("DANG_HOC".equalsIgnoreCase(kieuHoc)) {
            @SuppressWarnings("unchecked")
            List<TuVung> dsDangHoc = (List<TuVung>) session.getAttribute("tuDangHoc");
            if (dsDangHoc == null || dsDangHoc.isEmpty()) {
                dsDangHoc = (List<TuVung>) session.getAttribute("dsHoc");
            }
            if (dsDangHoc != null && !dsDangHoc.isEmpty()) {
                dsTuGoc = new ArrayList<>(dsDangHoc);
            }
        } else if ("TU_SAI".equalsIgnoreCase(kieuHoc)) {
            dsTuGoc = hocService.layTuSai(taiKhoanId);
        } else if ("NGAU_NHIEN".equalsIgnoreCase(kieuHoc)) {
            dsTuGoc = hocService.layNgauNhien(taiKhoanId);
        } else if (boId != null) {
            dsTuGoc = hocService.layTheoBo(boId, taiKhoanId);
        }

        // Fallback nếu danh sách rỗng
        if (dsTuGoc.isEmpty()) {
            dsTuGoc = hocService.layTatCa(taiKhoanId);
        }

        if (dsTuGoc.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Tạo câu hỏi phản xạ thông minh
        List<PhanXaCauHoiDTO> danhSachCauHoi = new ArrayList<>();
        Random random = new Random();

        // 1. Kho từ ƯU TIÊN: Lấy chính các từ đang học trong phiên này để ép não phân biệt triệt để
        List<String> khoTiengAnhUuTien = new ArrayList<>();
        List<String> khoTiengVietUuTien = new ArrayList<>();
        for (TuVung tv : dsTuGoc) {
            if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                khoTiengAnhUuTien.add(tv.getTiengAnh().trim());
            }
            if (tv.getTiengViet() != null && !tv.getTiengViet().isBlank()) {
                khoTiengVietUuTien.add(tv.getTiengViet().trim());
            }
        }

        // 2. Kho từ BỔ SUNG: Chỉ dùng làm phương án phụ khi người dùng học ít hơn 4 từ (ví dụ học 2-3 từ)
        List<String> khoTiengAnhBoSung = new ArrayList<>();
        List<String> khoTiengVietBoSung = new ArrayList<>();
        if (khoTiengAnhUuTien.size() < 4) {
            List<TuVung> dsBoSung = hocService.layTatCa(taiKhoanId);
            for (TuVung tv : dsBoSung) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank() && !khoTiengAnhUuTien.contains(tv.getTiengAnh().trim())) {
                    khoTiengAnhBoSung.add(tv.getTiengAnh().trim());
                }
                if (tv.getTiengViet() != null && !tv.getTiengViet().isBlank() && !khoTiengVietUuTien.contains(tv.getTiengViet().trim())) {
                    khoTiengVietBoSung.add(tv.getTiengViet().trim());
                }
            }
        }

        // Trộn ngẫu nhiên thứ tự các câu hỏi
        List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
        Collections.shuffle(dsTron);

        for (TuVung tv : dsTron) {
            if (tv.getTiengAnh() == null || tv.getTiengAnh().isBlank()) continue;

            String tuAnh = tv.getTiengAnh().trim();
            String nghia = tv.getTiengViet() != null ? tv.getTiengViet().trim() : "";
            String phienAm = tv.getPhienAm() != null ? tv.getPhienAm().trim() : "";
            String tenFileMp3 = tuAnh.toLowerCase().replace(" ", "-") + ".mp3";
            String audioUrl = "/audio/tu-vung/" + URLEncoder.encode(tenFileMp3, StandardCharsets.UTF_8);

            String hinhAnhUrl = "";

            PhanXaCauHoiDTO dto = new PhanXaCauHoiDTO();
            dto.setId(tv.getId());
            dto.setTiengAnh(tuAnh);
            dto.setTiengViet(nghia);
            dto.setPhienAm(phienAm);
            dto.setAudioUrl(audioUrl);
            dto.setHinhAnhUrl(hinhAnhUrl);

            // Xử lý các chế độ chơi
            if ("HINH_ANH".equalsIgnoreCase(cheDo)) {
                // Nhìn ảnh chọn từ tiếng Anh
                dto.setLoaiCauHoi("HINH_ANH_SANG_TU");
                dto.setDapAnDung(tuAnh);
                dto.setLuaChon(tao4LuaChon(tuAnh, khoTiengAnhUuTien, khoTiengAnhBoSung, random, false));
            } else if ("NGHE".equalsIgnoreCase(cheDo)) {
                // Nghe âm thanh chọn nghĩa tiếng Việt
                dto.setLoaiCauHoi("NGHE_SANG_NGHIA");
                dto.setDapAnDung(nghia);
                dto.setLuaChon(tao4LuaChon(nghia, khoTiengVietUuTien, khoTiengVietBoSung, random, true));
            } else if ("DUNG_SAI".equalsIgnoreCase(cheDo)) {
                // Chế độ Đúng / Sai siêu tốc
                dto.setLoaiCauHoi("DUNG_SAI");
                boolean laDung = random.nextBoolean();
                dto.setCauDungSaiLaDung(laDung);
                if (laDung) {
                    dto.setDapAnDung("DUNG");
                    dto.setLuaChon(List.of(nghia)); // Hiện nghĩa đúng
                } else {
                    dto.setDapAnDung("SAI");
                    // Ưu tiên chọn 1 nghĩa sai từ chính các từ đang học để ép phân biệt
                    String nghiaSai = nghia;
                    List<String> khoTronV = new ArrayList<>(khoTiengVietUuTien);
                    Collections.shuffle(khoTronV, random);
                    for (String k : khoTronV) {
                        if (!k.equalsIgnoreCase(nghia)) {
                            nghiaSai = k;
                            break;
                        }
                    }
                    // Nếu vẫn chưa tìm được nghĩa sai, lấy từ kho bổ sung
                    if (nghiaSai.equalsIgnoreCase(nghia) && !khoTiengVietBoSung.isEmpty()) {
                        List<String> khoTronBS = new ArrayList<>(khoTiengVietBoSung);
                        Collections.shuffle(khoTronBS, random);
                        for (String k : khoTronBS) {
                            if (!k.equalsIgnoreCase(nghia)) {
                                nghiaSai = k;
                                break;
                            }
                        }
                    }
                    dto.setLuaChon(List.of(nghiaSai)); // Hiện nghĩa sai
                }
            } else {
                // Mặc định: Nhìn từ tiếng Anh chọn nghĩa tiếng Việt
                dto.setLoaiCauHoi("TU_SANG_NGHIA");
                dto.setDapAnDung(nghia);
                dto.setLuaChon(tao4LuaChon(nghia, khoTiengVietUuTien, khoTiengVietBoSung, random, true));
            }

            danhSachCauHoi.add(dto);
        }

        return ResponseEntity.ok(danhSachCauHoi);
    }

    private List<String> tao4LuaChon(String dapAnDung, List<String> khoUuTien, List<String> khoBoSung, Random random, boolean isVietnamese) {
        Set<String> set = new LinkedHashSet<>();
        set.add(dapAnDung);

        // 1. Ưu tiên cao nhất: Lấy từ chính nhóm các từ đang học
        if (khoUuTien != null) {
            List<String> khoTronUuTien = new ArrayList<>(khoUuTien);
            Collections.shuffle(khoTronUuTien, random);
            for (String item : khoTronUuTien) {
                if (!item.equalsIgnoreCase(dapAnDung)) {
                    set.add(item);
                }
                if (set.size() == 4) break;
            }
        }

        // 2. Nếu nhóm đang học ít hơn 4 từ, mới lấy bù từ kho từ bổ sung
        if (set.size() < 4 && khoBoSung != null) {
            List<String> khoTronBoSung = new ArrayList<>(khoBoSung);
            Collections.shuffle(khoTronBoSung, random);
            for (String item : khoTronBoSung) {
                if (!item.equalsIgnoreCase(dapAnDung)) {
                    set.add(item);
                }
                if (set.size() == 4) break;
            }
        }

        // 3. Dự phòng tối thiểu nếu kho dữ liệu của user chưa có đủ 4 từ
        String[] duPhongEng = {"Apple", "Book", "Computer", "Student", "Travel", "Work", "Music", "Home"};
        String[] duPhongVie = {"Quả táo", "Quyển sách", "Máy tính", "Học sinh", "Du lịch", "Công việc", "Âm nhạc", "Ngôi nhà"};
        String[] tuDuPhong = isVietnamese ? duPhongVie : duPhongEng;
        int dpIdx = 0;
        while (set.size() < 4) {
            String dp = tuDuPhong[(dpIdx++) % tuDuPhong.length];
            if (!dp.equalsIgnoreCase(dapAnDung)) {
                set.add(dp);
            }
        }

        List<String> ketQua = new ArrayList<>(set);
        Collections.shuffle(ketQua, random);
        return ketQua;
    }

    // =========================================================
    // 3. API GHI NHẬN KẾT QUẢ PHẢN XẠ (CẬP NHẬT ĐỘ CHUẨN)
    // =========================================================
    @PostMapping("/api/luyen-phan-xa/ghi-nhan")
    @ResponseBody
    public ResponseEntity<?> ghiNhanKetQua(
            @RequestParam("tuId") Long tuId,
            @RequestParam("chinhXac") boolean chinhXac,
            HttpSession session
    ) {
        TaiKhoan taiKhoan = (TaiKhoan) session.getAttribute("taiKhoan");
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        Long taiKhoanId = taiKhoan.getId();
        if (chinhXac) {
            hocService.giamSoLanSai(tuId, taiKhoanId);
        } else {
            hocService.tangSoLanSai(tuId, taiKhoanId);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
