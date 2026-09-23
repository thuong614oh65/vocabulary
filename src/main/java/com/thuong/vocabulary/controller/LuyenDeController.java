package com.thuong.vocabulary.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.luyende.ChamDiemQ79Request;
import com.thuong.vocabulary.dto.luyende.DeThiQ79DTO;
import com.thuong.vocabulary.dto.luyende.KetQuaChamQ79DTO;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.GeminiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import com.thuong.vocabulary.service.LuyenDePresetData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class LuyenDeController {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    // Cache động lưu các đề đã phân tích (nếu có đề ngoài danh sách preset)
    private static final Map<Integer, DeThiQ79DTO> DYNAMIC_CACHE = new ConcurrentHashMap<>();

    // Danh sách 15 đề mẫu có sẵn từ tài liệu đề thi.docx
    private static final List<Map<String, Object>> DS_DE_MAU = new ArrayList<>();

    static {
        // BỘ ĐỀ THI TRỌNG TÂM TỪ BÀI HỌC TOEIC SPEAKING Q7-9
        themDeMau(19, "[Sample Exam] Danville City Tours", "Tour Tham quan Danville (Đề mẫu chuẩn)", "/images/de-thi/image19.png");
        themDeMau(16, "[Text 1] Drilling Site Tour Schedule", "Lịch trình Tham quan Khu công trường", "/images/de-thi/image16.png");
        themDeMau(17, "[Text 2] International Writers Conference", "Hội nghị Nhà văn Quốc tế", "/images/de-thi/image17.png");
        themDeMau(18, "[Text 3] Southeast Delegation Tour Itinerary", "Lịch trình Đoàn đại biểu Đông Nam", "/images/de-thi/image18.png");
        themDeMau(15, "Palm Island's New Employee Orientation", "Định hướng Nhân viên Khu nghỉ dưỡng Palm Island", "/images/de-thi/image15.png");
        themDeMau(13, "High Elevation Rock Festival Tours", "Tour Lễ hội Âm nhạc Rock High Elevation", "/images/de-thi/image13.png");

        // 13 ĐỀ THI MẪU THỰC HÀNH TỪ TÀI LIỆU đề thi.docx
        themDeMau(1, "Annual Human Resources Conference", "Hội nghị Nhân sự Thường niên", "/images/de-thi/image1.png");
        themDeMau(2, "Future of Education and Careers Seminar", "Hội thảo Tương lai Giáo dục & Nghề nghiệp", "/images/de-thi/image2.png");
        themDeMau(3, "Resume: Murray O'Brien", "Sơ yếu lý lịch: Kiến trúc sư Cảnh quan", "/images/de-thi/image3.png");
        themDeMau(4, "Anna Vales' Flower Shop Delivery", "Đơn giao hàng Shop hoa Anna Vales", "/images/de-thi/image4.png");
        themDeMau(5, "Sunrise Pharmaceutical Quarterly Meeting", "Họp Quản lý Quý - Dược phẩm Sunrise", "/images/de-thi/image5.png");
        themDeMau(6, "Seminars for You and Your Family", "Chuỗi Hội thảo Gia đình - Union Bank", "/images/de-thi/image6.png");
        themDeMau(7, "Fall International Culture Events", "Chuỗi Sự kiện Văn hóa Quốc tế Mùa Thu", "/images/de-thi/image7.png");
        themDeMau(8, "Henkel Film Festival", "Lễ hội Điện ảnh Henkel", "/images/de-thi/image8.png");
        themDeMau(9, "Bristol Co. Annual Conference Meeting", "Họp Thường niên Công ty Bristol", "/images/de-thi/image9.png");
        themDeMau(10, "New Employee Orientation", "Buổi Định hướng Nhân viên Mới", "/images/de-thi/image10.png");
        themDeMau(11, "Resume: Bruce Geller", "Sơ yếu lý lịch: Quản lý Nhân sự Bruce Geller", "/images/de-thi/image11.png");
        themDeMau(12, "Magnificent Moment Event Planner", "Lịch trình Tổ chức Sự kiện Mandy Cooper", "/images/de-thi/image12.png");
        themDeMau(14, "Vista City Annual Festival", "Lễ hội Thường niên Thành phố Vista", "/images/de-thi/image14.png");
    }

    private static void themDeMau(int id, String tenEn, String tenVi, String anhUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("tenEn", tenEn);
        map.put("tenVi", tenVi);
        map.put("anhUrl", anhUrl);
        DS_DE_MAU.add(map);
    }

    public LuyenDeController(GeminiService geminiService) {
        this.geminiService = geminiService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private TaiKhoan layTaiKhoan(HttpSession session) {
        return (TaiKhoan) session.getAttribute("taiKhoan");
    }

    // =========================================================
    // 1. TRANG GIAO DIỆN CHÍNH LUYỆN ĐỀ
    // =========================================================
    @GetMapping("/luyen-de")
    public String trangLuyenDe(HttpSession session, Model model) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return "redirect:/dangnhap";
        }

        model.addAttribute("dsDeMau", DS_DE_MAU);
        return "luyen-de";
    }

    // =========================================================
    // 2. API TẠO ĐỀ TỪ ẢNH UPLOAD (LỰA CHỌN 1)
    // =========================================================
    @PostMapping(value = "/api/luyen-de/tao-tu-anh", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> taoTuAnh(@RequestParam("file") MultipartFile file, HttpSession session) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn một file ảnh"));
        }

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType();
            String jsonRes = geminiService.taoDeQ79TuHinhAnh(bytes, contentType);

            DeThiQ79DTO dto = objectMapper.readValue(jsonRes, DeThiQ79DTO.class);
            dto.setLoaiNoiDung("IMAGE");
            dto.setNguonGoc("UPLOAD");
            dto.setThoiGianCau1(15);
            dto.setThoiGianCau2(15);
            dto.setThoiGianCau3(30);

            String base64Img = "data:" + (contentType != null ? contentType : "image/jpeg") + ";base64," + Base64.getEncoder().encodeToString(bytes);
            dto.setAnhUrl(base64Img);

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi phân tích ảnh: " + e.getMessage()));
        }
    }

    // =========================================================
    // 3. API TẠO ĐỀ TỪ VĂN BẢN (LỰA CHỌN 2)
    // =========================================================
    @PostMapping(value = "/api/luyen-de/tao-tu-van-ban", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> taoTuVanBan(@RequestBody Map<String, String> requestBody, HttpSession session) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        String vanBan = requestBody.get("vanBan");
        if (vanBan == null || vanBan.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập nội dung bảng/văn bản"));
        }

        try {
            String jsonRes = geminiService.taoDeQ79TuVanBan(vanBan);
            DeThiQ79DTO dto = objectMapper.readValue(jsonRes, DeThiQ79DTO.class);
            dto.setLoaiNoiDung("TEXT");
            dto.setVanBanThongTin(vanBan);
            dto.setNguonGoc("VAN_BAN");
            dto.setThoiGianCau1(15);
            dto.setThoiGianCau2(15);
            dto.setThoiGianCau3(30);

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi tạo đề từ văn bản: " + e.getMessage()));
        }
    }

    // =========================================================
    // 4. API AI TỰ TẠO ĐỀ NGẪU NHIÊN MỚI (LỰA CHỌN 3A)
    // =========================================================
    @PostMapping(value = "/api/luyen-de/tao-tu-dong", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> taoTuDong(HttpSession session) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            String jsonRes = geminiService.taoDeQ79TuDong();
            DeThiQ79DTO dto = objectMapper.readValue(jsonRes, DeThiQ79DTO.class);
            dto.setLoaiNoiDung("TEXT");
            dto.setNguonGoc("AI_TAO");
            dto.setThoiGianCau1(15);
            dto.setThoiGianCau2(15);
            dto.setThoiGianCau3(30);

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi AI sinh đề tự động: " + e.getMessage()));
        }
    }

    // =========================================================
    // 5. API TẢI ĐỀ THI MẪU CÓ SẴN (LỰA CHỌN 3B - 15 ĐỀ TỪ đề thi.docx)
    // =========================================================
    @GetMapping(value = "/api/luyen-de/de-mau/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> layDeMau(@PathVariable("id") int id, HttpSession session) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (id < 1 || id > 19) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã đề mẫu không hợp lệ (1-19)"));
        }

        try {
            // 1. Phản hồi TỨC THÌ (0.001s) từ bộ dữ liệu chuẩn đã cấu hình sẵn cho tất cả 19 đề thi
            DeThiQ79DTO presetDto = LuyenDePresetData.getDeMau(id);
            if (presetDto != null) {
                return ResponseEntity.ok(presetDto);
            }

            // 2. Kiểm tra bộ nhớ cache động
            if (DYNAMIC_CACHE.containsKey(id)) {
                return ResponseEntity.ok(DYNAMIC_CACHE.get(id));
            }

            // 3. Fallback chỉ chạy nếu có đề ngoài danh sách 1-19
            String imgRelativePath = "/images/de-thi/image" + id + ".png";
            String staticImgPath = "src/main/resources/static/images/de-thi/image" + id + ".png";
            File f = new File(staticImgPath);
            if (!f.exists()) {
                f = new File("vocabulary/src/main/resources/static/images/de-thi/image" + id + ".png");
            }
            if (!f.exists()) {
                f = new File("d:/Vocabulary/vocabulary/src/main/resources/static/images/de-thi/image" + id + ".png");
            }

            byte[] bytes = Files.readAllBytes(f.toPath());
            String jsonRes = geminiService.taoDeQ79TuHinhAnh(bytes, "image/png");

            DeThiQ79DTO dto = objectMapper.readValue(jsonRes, DeThiQ79DTO.class);
            dto.setLoaiNoiDung("IMAGE");
            dto.setAnhUrl(imgRelativePath);
            dto.setNguonGoc("DE_MAU");
            dto.setDeMauId(id);
            dto.setThoiGianCau1(15);
            dto.setThoiGianCau2(15);
            dto.setThoiGianCau3(30);

            DYNAMIC_CACHE.put(id, dto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi tải đề mẫu #" + id + ": " + e.getMessage()));
        }
    }

    // =========================================================
    // 6. API CHẤM ĐIỂM BÀI LÀM Q7-9 BẰNG AI
    // =========================================================
    @PostMapping(value = "/api/luyen-de/cham-diem", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> chamDiem(@RequestBody ChamDiemQ79Request req, HttpSession session) {
        TaiKhoan taiKhoan = layTaiKhoan(session);
        if (taiKhoan == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            String a1 = req.getCauTraLoi1() != null ? req.getCauTraLoi1().trim() : "";
            String a2 = req.getCauTraLoi2() != null ? req.getCauTraLoi2().trim() : "";
            String a3 = req.getCauTraLoi3() != null ? req.getCauTraLoi3().trim() : "";

            if (a1.isEmpty() && a2.isEmpty() && a3.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập ít nhất một câu trả lời để chấm điểm."));
            }

            String jsonKetQua = geminiService.chamDiemDeQ79(
                    req.getTieuDe(),
                    req.getThongTinDeBai(),
                    req.getTinhHuong(),
                    req.getCauHoi1(), a1,
                    req.getCauHoi2(), a2,
                    req.getCauHoi3(), a3
            );

            if (jsonKetQua == null || jsonKetQua.isBlank()) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Gemini AI đang bận, vui lòng thử lại sau giây lát."));
            }

            KetQuaChamQ79DTO ketQua = objectMapper.readValue(jsonKetQua, KetQuaChamQ79DTO.class);
            return ResponseEntity.ok(ketQua);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi chấm điểm: " + e.getMessage()));
        }
    }
}
