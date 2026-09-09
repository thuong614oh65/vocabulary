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
            @RequestParam(value = "cheDo", required = false, defaultValue = "TOAN_DIEN") String cheDo,
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
            List<TuVung> dsSai = hocService.layTuSai(taiKhoanId);
            if (dsSai != null) {
                // Giới hạn tối đa 25 từ hay sai nhất để vừa sức luyện tập
                if (dsSai.size() > 25) {
                    dsSai = new ArrayList<>(dsSai.subList(0, 25));
                }
                dsTuGoc = dsSai;
            }
        } else if ("NGAU_NHIEN".equalsIgnoreCase(kieuHoc)) {
            List<TuVung> dsNgauNhien = hocService.layNgauNhien(taiKhoanId);
            if (dsNgauNhien != null) {
                // Giới hạn tối đa 25 từ ngẫu nhiên
                if (dsNgauNhien.size() > 25) {
                    dsNgauNhien = new ArrayList<>(dsNgauNhien.subList(0, 25));
                }
                dsTuGoc = dsNgauNhien;
            }
        } else if (boId != null) {
            dsTuGoc = hocService.layTheoBo(boId, taiKhoanId);
        }

        // Fallback nếu danh sách rỗng
        if (dsTuGoc.isEmpty()) {
            List<TuVung> tatCa = hocService.layTatCa(taiKhoanId);
            if (tatCa.size() > 25) {
                Collections.shuffle(tatCa);
                tatCa = new ArrayList<>(tatCa.subList(0, 25));
            }
            dsTuGoc = tatCa;
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

        // Xử lý các chế độ chơi
        if ("TOAN_DIEN".equalsIgnoreCase(cheDo)) {
            // Luyện toàn diện: Xếp theo mức độ khó dần
            // Cấp 1: Đúng / Sai (Từ tiếng Anh ➔ Nghĩa tiếng Việt)
            List<TuVung> c1 = new ArrayList<>(dsTuGoc);
            Collections.shuffle(c1, random);
            for (TuVung tv : c1) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiDungSaiAnhViet(tv, khoTiengVietUuTien, khoTiengVietBoSung, random));
                }
            }

            // Cấp 2: Đúng / Sai (Nghĩa tiếng Việt ➔ Từ tiếng Anh)
            List<TuVung> c2 = new ArrayList<>(dsTuGoc);
            Collections.shuffle(c2, random);
            for (TuVung tv : c2) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiDungSaiVietAnh(tv, khoTiengAnhUuTien, khoTiengAnhBoSung, random));
                }
            }

            // Cấp 3: Nhìn từ tiếng Anh ➔ Chọn 4 nghĩa tiếng Việt
            List<TuVung> c3 = new ArrayList<>(dsTuGoc);
            Collections.shuffle(c3, random);
            for (TuVung tv : c3) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiTuSangNghia(tv, khoTiengVietUuTien, khoTiengVietBoSung, random));
                }
            }

            // Cấp 4: Nhìn nghĩa tiếng Việt ➔ Chọn 4 từ tiếng Anh
            List<TuVung> c4 = new ArrayList<>(dsTuGoc);
            Collections.shuffle(c4, random);
            for (TuVung tv : c4) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiNghiaSangTu(tv, khoTiengAnhUuTien, khoTiengAnhBoSung, random));
                }
            }

            // Cấp 5: Nghe âm thanh (khi số từ <= 10 để vòng luyện vừa sức)
            if (dsTuGoc.size() <= 10) {
                List<TuVung> c5 = new ArrayList<>(dsTuGoc);
                Collections.shuffle(c5, random);
                for (TuVung tv : c5) {
                    if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                        danhSachCauHoi.add(taoCauHoiNghe(tv, khoTiengVietUuTien, khoTiengVietBoSung, random));
                    }
                }
            }
        } else if ("DUNG_SAI_ANH_VIET".equalsIgnoreCase(cheDo) || "DUNG_SAI".equalsIgnoreCase(cheDo)) {
            List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
            Collections.shuffle(dsTron, random);
            for (TuVung tv : dsTron) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiDungSaiAnhViet(tv, khoTiengVietUuTien, khoTiengVietBoSung, random));
                }
            }
        } else if ("DUNG_SAI_VIET_ANH".equalsIgnoreCase(cheDo)) {
            List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
            Collections.shuffle(dsTron, random);
            for (TuVung tv : dsTron) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiDungSaiVietAnh(tv, khoTiengAnhUuTien, khoTiengAnhBoSung, random));
                }
            }
        } else if ("NGHIA_SANG_TU".equalsIgnoreCase(cheDo)) {
            List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
            Collections.shuffle(dsTron, random);
            for (TuVung tv : dsTron) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiNghiaSangTu(tv, khoTiengAnhUuTien, khoTiengAnhBoSung, random));
                }
            }
        } else if ("NGHE".equalsIgnoreCase(cheDo)) {
            List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
            Collections.shuffle(dsTron, random);
            for (TuVung tv : dsTron) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiNghe(tv, khoTiengVietUuTien, khoTiengVietBoSung, random));
                }
            }
        } else if ("HINH_ANH".equalsIgnoreCase(cheDo)) {
            List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
            Collections.shuffle(dsTron, random);
            for (TuVung tv : dsTron) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiHinhAnh(tv, khoTiengAnhUuTien, khoTiengAnhBoSung, random));
                }
            }
        } else {
            // Mặc định: NHIN_TU (Từ tiếng Anh ➔ 4 nghĩa tiếng Việt)
            List<TuVung> dsTron = new ArrayList<>(dsTuGoc);
            Collections.shuffle(dsTron, random);
            for (TuVung tv : dsTron) {
                if (tv.getTiengAnh() != null && !tv.getTiengAnh().isBlank()) {
                    danhSachCauHoi.add(taoCauHoiTuSangNghia(tv, khoTiengVietUuTien, khoTiengVietBoSung, random));
                }
            }
        }

        return ResponseEntity.ok(danhSachCauHoi);
    }

    private PhanXaCauHoiDTO taoCauHoiDungSaiAnhViet(TuVung tv, List<String> kU, List<String> kB, Random r) {
        PhanXaCauHoiDTO dto = baseDTO(tv);
        dto.setLoaiCauHoi("DUNG_SAI_ANH_VIET");
        dto.setCapDo("Cấp 1: Đúng / Sai (Anh ➔ Việt)");
        dto.setTfHienThiTrai(tv.getTiengAnh());
        boolean laDung = r.nextBoolean();
        dto.setCauDungSaiLaDung(laDung);
        if (laDung) {
            dto.setDapAnDung("DUNG");
            dto.setTfHienThiPhai(tv.getTiengViet());
            dto.setLuaChon(List.of(tv.getTiengViet()));
        } else {
            dto.setDapAnDung("SAI");
            String sai = laySai(tv.getTiengViet(), kU, kB, r, true);
            dto.setTfHienThiPhai(sai);
            dto.setLuaChon(List.of(sai));
        }
        return dto;
    }

    private PhanXaCauHoiDTO taoCauHoiDungSaiVietAnh(TuVung tv, List<String> kU, List<String> kB, Random r) {
        PhanXaCauHoiDTO dto = baseDTO(tv);
        dto.setLoaiCauHoi("DUNG_SAI_VIET_ANH");
        dto.setCapDo("Cấp 2: Đúng / Sai (Việt ➔ Anh)");
        dto.setTfHienThiTrai(tv.getTiengViet());
        boolean laDung = r.nextBoolean();
        dto.setCauDungSaiLaDung(laDung);
        if (laDung) {
            dto.setDapAnDung("DUNG");
            dto.setTfHienThiPhai(tv.getTiengAnh());
            dto.setLuaChon(List.of(tv.getTiengAnh()));
        } else {
            dto.setDapAnDung("SAI");
            String sai = laySai(tv.getTiengAnh(), kU, kB, r, false);
            dto.setTfHienThiPhai(sai);
            dto.setLuaChon(List.of(sai));
        }
        return dto;
    }

    private PhanXaCauHoiDTO taoCauHoiTuSangNghia(TuVung tv, List<String> kU, List<String> kB, Random r) {
        PhanXaCauHoiDTO dto = baseDTO(tv);
        dto.setLoaiCauHoi("TU_SANG_NGHIA");
        dto.setCapDo("Cấp 3: Nhìn từ ➔ 4 Nghĩa Việt");
        dto.setDapAnDung(tv.getTiengViet());
        dto.setLuaChon(tao4LuaChon(tv.getTiengViet(), kU, kB, r, true));
        return dto;
    }

    private PhanXaCauHoiDTO taoCauHoiNghiaSangTu(TuVung tv, List<String> kU, List<String> kB, Random r) {
        PhanXaCauHoiDTO dto = baseDTO(tv);
        dto.setLoaiCauHoi("NGHIA_SANG_TU");
        dto.setCapDo("Cấp 4: Nhìn nghĩa ➔ 4 Từ Anh");
        dto.setDapAnDung(tv.getTiengAnh());
        dto.setLuaChon(tao4LuaChon(tv.getTiengAnh(), kU, kB, r, false));
        return dto;
    }

    private PhanXaCauHoiDTO taoCauHoiNghe(TuVung tv, List<String> kU, List<String> kB, Random r) {
        PhanXaCauHoiDTO dto = baseDTO(tv);
        dto.setLoaiCauHoi("NGHE_SANG_NGHIA");
        dto.setCapDo("Cấp 5: Nghe âm thanh ➔ 4 Nghĩa Việt");
        dto.setDapAnDung(tv.getTiengViet());
        dto.setLuaChon(tao4LuaChon(tv.getTiengViet(), kU, kB, r, true));
        return dto;
    }

    private PhanXaCauHoiDTO taoCauHoiHinhAnh(TuVung tv, List<String> kU, List<String> kB, Random r) {
        PhanXaCauHoiDTO dto = baseDTO(tv);
        dto.setLoaiCauHoi("HINH_ANH_SANG_TU");
        dto.setCapDo("Nhìn ảnh ➔ 4 Từ Anh");
        dto.setDapAnDung(tv.getTiengAnh());
        dto.setLuaChon(tao4LuaChon(tv.getTiengAnh(), kU, kB, r, false));
        return dto;
    }

    private PhanXaCauHoiDTO baseDTO(TuVung tv) {
        PhanXaCauHoiDTO dto = new PhanXaCauHoiDTO();
        dto.setId(tv.getId());
        dto.setTiengAnh(tv.getTiengAnh());
        dto.setTiengViet(tv.getTiengViet());
        dto.setPhienAm(tv.getPhienAm());
        dto.setAudioUrl("/audio/tu-vung/" + URLEncoder.encode(tv.getTiengAnh().toLowerCase().replace(" ", "-") + ".mp3", StandardCharsets.UTF_8));
        return dto;
    }

    private String laySai(String dung, List<String> kU, List<String> kB, Random r, boolean isVietnamese) {
        List<String> pool = new ArrayList<>();
        if (kU != null) pool.addAll(kU);
        if (kB != null) pool.addAll(kB);
        Collections.shuffle(pool, r);
        for (String s : pool) {
            if (s != null && !s.equalsIgnoreCase(dung)) return s;
        }
        String[] fallbackVie = {"Quyển sách", "Máy tính", "Học sinh", "Du lịch", "Công việc", "Sức khỏe"};
        String[] fallbackEng = {"Book", "Computer", "Student", "Travel", "Work", "Health"};
        String[] fallback = isVietnamese ? fallbackVie : fallbackEng;
        for (String f : fallback) {
            if (!f.equalsIgnoreCase(dung)) return f;
        }
        return isVietnamese ? "Khác" : "Other";
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
