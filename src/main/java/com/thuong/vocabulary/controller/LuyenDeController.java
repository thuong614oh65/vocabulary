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
import java.util.*;

@Controller
public class LuyenDeController {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    // Danh sách 15 đề mẫu có sẵn từ tài liệu đề thi.docx
    private static final List<Map<String, Object>> DS_DE_MAU = new ArrayList<>();

    static {
        // 3 ĐỀ THI MẪU TRỌNG TÂM (TEXT 1, TEXT 2, TEXT 3 TỪ BÀI HỌC TOEIC SPEAKING Q7-9)
        themDeMau(16, "[Text 1] Drilling Site Tour Schedule", "Lịch trình Tham quan Khu công trường", "/images/de-thi/image16.png");
        themDeMau(17, "[Text 2] International Writers Conference", "Hội nghị Nhà văn Quốc tế", "/images/de-thi/image17.png");
        themDeMau(18, "[Text 3] Southeast Delegation Tour Itinerary", "Lịch trình Đoàn đại biểu Đông Nam", "/images/de-thi/image18.png");

        // 15 ĐỀ THI MẪU TỪ TÀI LIỆU đề thi.docx
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
        themDeMau(13, "High Elevation Rock Festival Tours", "Tour Lễ hội Âm nhạc Rock High Elevation", "/images/de-thi/image13.png");
        themDeMau(14, "Vista City Annual Festival", "Lễ hội Thường niên Thành phố Vista", "/images/de-thi/image14.png");
        themDeMau(15, "Palm Island's New Employee Orientation", "Định hướng Nhân viên Khu nghỉ dưỡng Palm Island", "/images/de-thi/image15.png");
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

        if (id < 1 || id > 18) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã đề mẫu không hợp lệ (1-18)"));
        }

        try {
            String imgRelativePath = "/images/de-thi/image" + id + ".png";

            // XỬ LÝ ĐẶC BIỆT CHO 3 ĐỀ BÀI HỌC (TEXT 1, TEXT 2, TEXT 3)
            if (id == 16) {
                DeThiQ79DTO dto = DeThiQ79DTO.builder()
                        .tieuDe("[Text 1] Drilling Site Tour Schedule")
                        .loaiNoiDung("IMAGE")
                        .anhUrl(imgRelativePath)
                        .tomTatNoiDung("Drilling Site Tour Schedule: Daily at 11:00 a.m. No tours on weekends. Safety equipment required for all participants! 10:45 a.m. Meet at tunnel entrance; 11:00-12:00 Walking tour of finished tunnel with guide; 12:00-1:00 Lunch in underground break room; 1:00-1:30 Talk about drill site safety; 1:30-2:30 Open viewing of drill area (guide available); 3:00 Return to base camp.")
                        .tinhHuong("Hello, this is Mark. I'm calling to ask for some details about the drilling site tour scheduled for tomorrow. Could you please answer a few questions for me?")
                        .cauHoi1("What time do we need to meet for the tour?")
                        .thoiGianCau1(15)
                        .goiYCau1("You need to meet at 10:45 a.m. at the tunnel entrance before the tour begins at 11:00 a.m.")
                        .cauHoi2("I heard that tours are also available on weekends. Is that correct?")
                        .thoiGianCau2(15)
                        .goiYCau2("No, I'm sorry, but that's not correct. There are no tours on weekends; tours are only conducted daily from Monday through Friday.")
                        .cauHoi3("Could you please tell me how long the tour lasts and what we will see during the tour?")
                        .thoiGianCau3(30)
                        .goiYCau3("Certainly. The tour lasts about 4 hours, from 10:45 a.m. until 3:00 p.m. First, from 11:00 to 12:00, you will take a walking tour of the finished part of the tunnel with a guide. Then, after lunch, from 1:30 to 2:30, you will have an open viewing of the drill area with a guide available before returning to base camp at 3:00 p.m.")
                        .nguonGoc("DE_MAU")
                        .deMauId(16)
                        .build();
                return ResponseEntity.ok(dto);
            }

            if (id == 17) {
                DeThiQ79DTO dto = DeThiQ79DTO.builder()
                        .tieuDe("[Text 2] International Writers Conference")
                        .loaiNoiDung("IMAGE")
                        .anhUrl(imgRelativePath)
                        .tomTatNoiDung("International Writers Conference - Tuesday, March 15, 10:00 a.m. to 6:00 p.m. at Carver Hall, Thorpe Center, West University Campus. Guest Speakers: Jenny Hill (President, Freelance Writers League, 10:00 a.m., Room 17), Marlon Thomson (Publishing Manager, Horton Publishing, 1:00 p.m., Room 21), Angela Moeller (CEO, Editorial Advisory Group, 3:00 p.m., Room 12). Publisher exhibits: 10:00 a.m. to 6:00 p.m., Carver Reception Hall. Open forum: 4:00 p.m. to 6:00 p.m., Room 2. Registration Cost: $26 per person by March 13.")
                        .tinhHuong("Hi, I'm planning to attend the International Writers Conference on March 15th, and I have a couple of questions about the schedule.")
                        .cauHoi1("Can you please tell me who Angela Moeller is and where she will be speaking?")
                        .thoiGianCau1(15)
                        .goiYCau1("Angela Moeller is the CEO of the Editorial Advisory Group, and she will be speaking at 3:00 p.m. in Room 12.")
                        .cauHoi2("What can you do at the publisher exhibits?")
                        .thoiGianCau2(15)
                        .goiYCau2("At the publisher exhibits, you can browse through booths offering valuable information on how to get published, learn what's new in the field, and find out where to send your work.")
                        .cauHoi3("Could you tell me about all the guest speakers and when they will be speaking?")
                        .thoiGianCau3(30)
                        .goiYCau3("Sure, there are three guest speakers scheduled. First, Jenny Hill, President of Freelance Writers League, will speak at 10:00 a.m. in Room 17. Second, Marlon Thomson, Publishing Manager at Horton Publishing, will speak at 1:00 p.m. in Room 21. Finally, Angela Moeller, CEO of Editorial Advisory Group, will speak at 3:00 p.m. in Room 12.")
                        .nguonGoc("DE_MAU")
                        .deMauId(17)
                        .build();
                return ResponseEntity.ok(dto);
            }

            if (id == 18) {
                DeThiQ79DTO dto = DeThiQ79DTO.builder()
                        .tieuDe("[Text 3] Southeast Delegation Tour Itinerary")
                        .loaiNoiDung("IMAGE")
                        .anhUrl(imgRelativePath)
                        .tomTatNoiDung("Itinerary for Southeast Delegation: 7:00 a.m. Arrive at New York La Guardia Airport on Flight 681, pick-up by Secretary Sullivan; 8:15 a.m. Hotel Compton check-in, 1 hour free; 9:15 a.m. Leave for Government Center, arrive 9:45 a.m.; 10:00 a.m. Meet and greet, Webber Room; 10:30 a.m. Presentation: Global Environmental Issues; 11:30 a.m. Taxi to North Surfside restaurant; 12:00 p.m. Lunch with CEO, HCG Inc.; 1:45 p.m. Taxi to Government Center; 2:30 p.m. Presentation: Solar Energy; 4:00 p.m. Depart for airport with Secretary Sullivan; 7:00 p.m. Flight 682 for Los Angeles.")
                        .tinhHuong("Hello, this is Mr. Gibson. I'm checking on the Southeast delegation's schedule in New York today, and I have a few questions.")
                        .cauHoi1("This is Mr. Gibson. Who's taking the Southeast delegation to the airport, and what time is the flight?")
                        .thoiGianCau1(15)
                        .goiYCau1("Secretary Sullivan is taking the delegation to the airport at 4:00 p.m., and their flight departs at 7:00 p.m. on Flight 682 for Los Angeles.")
                        .cauHoi2("What is the delegation doing between presentations?")
                        .thoiGianCau2(15)
                        .goiYCau2("Between presentations, from 11:30 a.m. to 1:45 p.m., the delegation will take a taxi to North Surfside restaurant to have lunch with the CEO of HCG Inc., and then return by taxi to Government Center.")
                        .cauHoi3("Could you please tell me the complete schedule of the delegation for the morning from arrival until their first presentation?")
                        .thoiGianCau3(30)
                        .goiYCau3("Certainly. First, they arrive at New York La Guardia Airport at 7:00 a.m. and are picked up by Secretary Sullivan. Then, they arrive at Hotel Compton at 8:15 a.m. for check-in with one hour of free time. Next, they leave for Government Center at 9:15 a.m., arriving at 9:45 a.m. Finally, they have a meet and greet at 10:00 a.m. in the Webber Room before their first presentation at 10:30 a.m.")
                        .nguonGoc("DE_MAU")
                        .deMauId(18)
                        .build();
                return ResponseEntity.ok(dto);
            }

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
