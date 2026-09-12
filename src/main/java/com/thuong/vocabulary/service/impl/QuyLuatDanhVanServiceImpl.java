package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.QuyLuatDanhVanDTO;
import com.thuong.vocabulary.dto.TuMinhHoaDTO;
import com.thuong.vocabulary.service.GeminiService;
import com.thuong.vocabulary.service.QuyLuatDanhVanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuyLuatDanhVanServiceImpl implements QuyLuatDanhVanService {

    private final Map<String, QuyLuatDanhVanDTO> thuVienQuyLuat = new LinkedHashMap<>();
    private final Map<String, QuyLuatDanhVanDTO> aiCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiService geminiService;

    @Autowired
    public QuyLuatDanhVanServiceImpl(@Autowired(required = false) GeminiService geminiService) {
        this.geminiService = geminiService;
        khoiTaoThuVienQuyLuat();
    }

    @Override
    public List<QuyLuatDanhVanDTO> layTatCaQuyLuat() {
        return new ArrayList<>(thuVienQuyLuat.values());
    }

    @Override
    public QuyLuatDanhVanDTO layTheoId(String id) {
        if (id == null) return null;
        String key = id.trim().toLowerCase();
        if (thuVienQuyLuat.containsKey(key)) {
            return thuVienQuyLuat.get(key);
        }
        if (aiCache.containsKey(key)) {
            return aiCache.get(key);
        }
        return null;
    }

    @Override
    public List<QuyLuatDanhVanDTO> timKiemQuyLuat(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return layTatCaQuyLuat();
        }
        String q = tuKhoa.trim().toLowerCase();
        List<QuyLuatDanhVanDTO> ketQua = new ArrayList<>();

        for (QuyLuatDanhVanDTO ql : thuVienQuyLuat.values()) {
            boolean match = false;
            if (ql.getId().toLowerCase().contains(q) ||
                ql.getCumChu().toLowerCase().contains(q) ||
                ql.getDocLaIpa().toLowerCase().contains(q) ||
                ql.getTieuDeQuyTac().toLowerCase().contains(q)) {
                match = true;
            } else {
                for (TuMinhHoaDTO tm : ql.getDanhSachTu()) {
                    if (tm.getTu().toLowerCase().contains(q) || tm.getNghia().toLowerCase().contains(q)) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                ketQua.add(ql);
            }
        }
        return ketQua;
    }

    @Override
    public QuyLuatDanhVanDTO taoSoDoTuAI(String tuHoacAm) {
        if (tuHoacAm == null || tuHoacAm.isBlank()) {
            return layTheoId("w_or");
        }
        String key = tuHoacAm.trim().toLowerCase();

        // 1. Kiểm tra kho có sẵn
        for (QuyLuatDanhVanDTO ql : thuVienQuyLuat.values()) {
            if (ql.getId().equalsIgnoreCase(key) || ql.getCumChu().equalsIgnoreCase(key)) {
                return ql;
            }
            for (TuMinhHoaDTO tm : ql.getDanhSachTu()) {
                if (tm.getTu().equalsIgnoreCase(key)) {
                    return ql;
                }
            }
        }

        // 2. Kiểm tra cache AI
        if (aiCache.containsKey(key)) {
            return aiCache.get(key);
        }

        // 3. Gọi Gemini sinh sơ đồ tư duy
        if (geminiService != null) {
            try {
                String prompt = String.format("""
                        Bạn là chuyên gia ngữ âm và quy luật đánh vần tiếng Anh chuẩn Cambridge / Oxford.
                        Hãy tạo một sơ đồ tư duy (Mindmap) quy luật đánh vần cho âm hoặc từ sau: "%s".
                        
                        Yêu cầu trả về DUY NHẤT một chuỗi JSON thuần túy (không kèm markdown ```json):
                        {
                          "id": "chuoi_id_khong_dau",
                          "cumChu": "Cụm chữ cái quy tắc trung tâm (ví dụ: 'or', 'ise', 'tion')",
                          "docLaIpa": "Phiên âm IPA chuẩn của cụm đó (ví dụ: '/ɜː/', '/aɪz/')",
                          "amDoc": "Văn bản phát âm riêng cho cụm này để TTS đọc",
                          "tieuDeQuyTac": "Tiêu đề ngắn gọn quy tắc (ví dụ: Từ có 'w + or' thì 'or' đọc là /ɜː/)",
                          "moTaQuyTac": "Giải thích chi tiết quy tắc đánh vần trong 1-2 câu",
                          "phanLoai": "NGUYEN_AM_DAC_BIET (hoặc DUOI_TU_HAU_TO, NGUYEN_AM_DOI, PHU_AM_KEP_CAM, BIEN_AM_C_G)",
                          "tenPhanLoai": "Tên nhóm hiển thị tiếng Việt",
                          "icon": "1 icon đại diện sinh động (ví dụ: ⚡, 🌟, 🎯)",
                          "huongDanPhatAm": "Hướng dẫn khẩu hình môi lưỡi của âm trung tâm",
                          "danhSachTu": [
                            {
                              "tu": "từ tiếng Anh (ví dụ: word)",
                              "phanHighlight": "phần chữ theo quy tắc (ví dụ: or hoặc wor)",
                              "phienAm": "phiên âm IPA cả từ (ví dụ: /wɜːd/)",
                              "phanIpaHighlight": "âm IPA theo quy tắc (ví dụ: ɜː)",
                              "nghia": "nghĩa tiếng Việt ngắn gọn",
                              "icon": "1 icon minh họa cho từ",
                              "audioUrl": "/audio/tts?text=word"
                            }
                          ]
                        }
                        Tạo từ 6 đến 8 từ vựng thông dụng minh họa cho quy tắc này.
                        """, tuHoacAm);

                String jsonResponse = geminiService.generateText(prompt);
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    String cleanJson = jsonResponse.trim();
                    if (cleanJson.startsWith("```")) {
                        cleanJson = cleanJson.replaceAll("^```[a-zA-Z]*\\s*", "").replaceAll("\\s*```$", "").trim();
                    }
                    QuyLuatDanhVanDTO dto = objectMapper.readValue(cleanJson, QuyLuatDanhVanDTO.class);
                    if (dto != null && dto.getDanhSachTu() != null && !dto.getDanhSachTu().isEmpty()) {
                        for (TuMinhHoaDTO tm : dto.getDanhSachTu()) {
                            if (tm.getAudioUrl() == null || tm.getAudioUrl().isBlank()) {
                                tm.setAudioUrl("/audio/tts?text=" + tm.getTu());
                            }
                        }
                        aiCache.put(key, dto);
                        return dto;
                    }
                }
            } catch (Exception e) {
                System.err.println("[QuyLuatDanhVanService] Lỗi gọi Gemini AI tạo sơ đồ: " + e.getMessage());
            }
        }

        // Fallback mặc định
        return layTheoId("w_or");
    }

    // =========================================================
    // KHỞI TẠO BỘ THƯ VIỆN HƠN 36+ QUY TẮC ĐÁNH VẦN ĐẦY ĐỦ CÁC ÂM
    // =========================================================
    private void khoiTaoThuVienQuyLuat() {

        // -------------------------------------------------------------
        // NHÓM 1: NGUYÊN ÂM ĐẶC BIỆT & BIẾN ÂM R (R-CONTROLLED VOWELS)
        // -------------------------------------------------------------
        themQuyLuat(new QuyLuatDanhVanDTO(
                "w_or", "or", "/ɜː/", "er",
                "Từ có \"w + or\" thì \"or\" đọc là /ɜː/",
                "Thông thường chữ \"or\" đọc là /ɔː/ (như fork, short). Nhưng khi đứng ngay sau \"w\", cụm \"or\" luôn bị biến âm đọc thành /ɜː/.",
                "NGUYEN_AM_DAC_BIET", "🌟 Nguyên âm đặc biệt", "⚡",
                "Khẩu hình âm /ɜː/: Mở miệng tự nhiên, thả lỏng môi và quai hàm, lưỡi đặt ở vị trí trung tâm khoang miệng, ngân dài hơi từ cổ họng.",
                List.of(
                        new TuMinhHoaDTO("word", "wor", "/wɜːd/", "ɜː", "từ, lời nói", "📝", "/audio/tts?text=word"),
                        new TuMinhHoaDTO("world", "wor", "/wɜːld/", "ɜː", "thế giới", "🌍", "/audio/tts?text=world"),
                        new TuMinhHoaDTO("work", "wor", "/wɜːk/", "ɜː", "làm việc", "💼", "/audio/tts?text=work"),
                        new TuMinhHoaDTO("worship", "wor", "/ˈwɜː.ʃɪp/", "ɜː", "thờ phụng, tôn sùng", "🛐", "/audio/tts?text=worship"),
                        new TuMinhHoaDTO("worthy", "wor", "/ˈwɜː.ði/", "ɜː", "xứng đáng", "🏆", "/audio/tts?text=worthy"),
                        new TuMinhHoaDTO("worse", "wor", "/wɜːs/", "ɜː", "tệ hơn, xấu hơn", "📉", "/audio/tts?text=worse"),
                        new TuMinhHoaDTO("worm", "wor", "/wɜːm/", "ɜː", "con giun, sâu", "🐛", "/audio/tts?text=worm"),
                        new TuMinhHoaDTO("workplace", "wor", "/ˈwɜːk.pleɪs/", "ɜː", "nơi làm việc", "🏢", "/audio/tts?text=workplace")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ar", "ar", "/ɑː/", "ah",
                "Chữ \"AR\" thường đọc là /ɑː/",
                "Khi chữ 'a' đi liền với 'r' trong từ một âm tiết hoặc âm tiết có trọng âm, 'ar' hầu như luôn được phát âm là nguyên âm dài /ɑː/.",
                "NGUYEN_AM_DAC_BIET", "🌟 Nguyên âm đặc biệt", "🚗",
                "Khẩu hình âm /ɑː/: Hạ quai hàm xuống sâu, mở rộng vòm miệng, lưỡi đặt thấp và kéo nhẹ về phía sau, ngân dài hơi ấm áp.",
                List.of(
                        new TuMinhHoaDTO("car", "ar", "/kɑːr/", "ɑː", "xe hơi, ô tô", "🚗", "/audio/tts?text=car"),
                        new TuMinhHoaDTO("park", "ar", "/pɑːk/", "ɑː", "công viên, đỗ xe", "🌳", "/audio/tts?text=park"),
                        new TuMinhHoaDTO("star", "ar", "/stɑːr/", "ɑː", "ngôi sao", "⭐", "/audio/tts?text=star"),
                        new TuMinhHoaDTO("dark", "ar", "/dɑːk/", "ɑː", "tối tăm, bóng tối", "🌑", "/audio/tts?text=dark"),
                        new TuMinhHoaDTO("farm", "ar", "/fɑːm/", "ɑː", "nông trại, trang trại", "🚜", "/audio/tts?text=farm"),
                        new TuMinhHoaDTO("hard", "ar", "/hɑːd/", "ɑː", "chăm chỉ, khó khăn", "💪", "/audio/tts?text=hard"),
                        new TuMinhHoaDTO("card", "ar", "/kɑːd/", "ɑː", "thẻ, thiệp, quân bài", "💳", "/audio/tts?text=card"),
                        new TuMinhHoaDTO("smart", "ar", "/smɑːt/", "ɑː", "thông minh, nhanh nhạy", "🧠", "/audio/tts?text=smart")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "or_normal", "or", "/ɔː/", "or",
                "Chữ \"OR\" thông thường đọc là /ɔː/",
                "Trong các từ thông thường (không đi sau chữ 'w'), nhóm chữ 'or' được phát âm là nguyên âm dài tròn môi /ɔː/.",
                "NGUYEN_AM_DAC_BIET", "🌟 Nguyên âm đặc biệt", "🐎",
                "Khẩu hình âm /ɔː/: Môi hơi tròn nhô ra phía trước, quai hàm hạ vừa phải, nâng phần sau của lưỡi lên cao một chút.",
                List.of(
                        new TuMinhHoaDTO("horse", "or", "/hɔːs/", "ɔː", "con ngựa", "🐎", "/audio/tts?text=horse"),
                        new TuMinhHoaDTO("fork", "or", "/fɔːk/", "ɔː", "cái nĩa, cái dĩa", "🍴", "/audio/tts?text=fork"),
                        new TuMinhHoaDTO("storm", "or", "/stɔːm/", "ɔː", "cơn bão lớn", "⛈️", "/audio/tts?text=storm"),
                        new TuMinhHoaDTO("born", "or", "/bɔːn/", "ɔː", "sinh ra, chào đời", "👶", "/audio/tts?text=born"),
                        new TuMinhHoaDTO("short", "or", "/ʃɔːt/", "ɔː", "ngắn, thấp", "📏", "/audio/tts?text=short"),
                        new TuMinhHoaDTO("north", "or", "/nɔːθ/", "ɔː", "hướng bắc", "🧭", "/audio/tts?text=north"),
                        new TuMinhHoaDTO("port", "or", "/pɔːt/", "ɔː", "cảng biển, cổng", "⚓", "/audio/tts?text=port"),
                        new TuMinhHoaDTO("corner", "or", "/ˈkɔː.nər/", "ɔː", "góc phố, góc phòng", "📐", "/audio/tts?text=corner")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "er_ir_ur", "er / ir / ur", "/ɜː/", "er",
                "\"ER, IR, UR\" đều đọc là /ɜː/",
                "Bộ ba nguyên âm kết hợp với 'r' gồm er, ir, ur khi đứng trong âm tiết có trọng âm đều cùng phát âm thành nguyên âm dài /ɜː/.",
                "NGUYEN_AM_DAC_BIET", "🌟 Nguyên âm đặc biệt", "🐦",
                "Khẩu hình âm /ɜː/: Mở miệng vừa phải, thả lỏng môi tự nhiên, đặt lưỡi ở giữa miệng và phát âm ngân dài.",
                List.of(
                        new TuMinhHoaDTO("her", "er", "/hɜːr/", "ɜː", "cô ấy, của cô ấy", "👩", "/audio/tts?text=her"),
                        new TuMinhHoaDTO("bird", "ir", "/bɜːd/", "ɜː", "con chim", "🐦", "/audio/tts?text=bird"),
                        new TuMinhHoaDTO("girl", "ir", "/ɡɜːl/", "ɜː", "cô bé, bạn gái", "👧", "/audio/tts?text=girl"),
                        new TuMinhHoaDTO("shirt", "ir", "/ʃɜːt/", "ɜː", "áo sơ mi", "👔", "/audio/tts?text=shirt"),
                        new TuMinhHoaDTO("turn", "ur", "/tɜːn/", "ɜː", "quay, xoay vòng", "🔄", "/audio/tts?text=turn"),
                        new TuMinhHoaDTO("burn", "ur", "/bɜːn/", "ɜː", "đốt cháy, ngọn lửa", "🔥", "/audio/tts?text=burn"),
                        new TuMinhHoaDTO("hurt", "ur", "/hɜːt/", "ɜː", "làm đau, tổn thương", "🩹", "/audio/tts?text=hurt"),
                        new TuMinhHoaDTO("nurse", "ur", "/nɜːs/", "ɜː", "y tá chăm sóc", "🩺", "/audio/tts?text=nurse")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "all_al", "all / al", "/ɔːl/", "all",
                "\"ALL / AL\" đọc là /ɔːl/",
                "Chữ cái 'a' khi đứng trước hai chữ 'll' hoặc trước phụ âm 'l' thường bị biến âm đọc thành /ɔː/ kết hợp âm uốn lưỡi /l/ thành /ɔːl/.",
                "NGUYEN_AM_DAC_BIET", "🌟 Nguyên âm đặc biệt", "⚽",
                "Khẩu hình âm /ɔːl/: Chu môi tròn phát âm /ɔː/ sau đó nâng đầu lưỡi chạm vào nướu răng hàm trên tạo âm /l/ (Dark L).",
                List.of(
                        new TuMinhHoaDTO("ball", "all", "/bɔːl/", "ɔːl", "quả bóng, trái banh", "⚽", "/audio/tts?text=ball"),
                        new TuMinhHoaDTO("call", "all", "/kɔːl/", "ɔːl", "gọi điện thoại", "📞", "/audio/tts?text=call"),
                        new TuMinhHoaDTO("tall", "all", "/tɔːl/", "ɔːl", "cao lớn", "🦒", "/audio/tts?text=tall"),
                        new TuMinhHoaDTO("fall", "all", "/fɔːl/", "ɔːl", "mùa thu, rơi xuống", "🍂", "/audio/tts?text=fall"),
                        new TuMinhHoaDTO("wall", "all", "/wɔːl/", "ɔːl", "bức tường thành", "🧱", "/audio/tts?text=wall"),
                        new TuMinhHoaDTO("small", "all", "/smɔːl/", "ɔːl", "nhỏ bé", "🤏", "/audio/tts?text=small"),
                        new TuMinhHoaDTO("salt", "al", "/sɔːlt/", "ɔːl", "muối ăn", "🧂", "/audio/tts?text=salt"),
                        new TuMinhHoaDTO("talk", "al", "/tɔːk/", "ɔː", "nói chuyện, trò chuyện", "🗣️", "/audio/tts?text=talk")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "wa_war", "wa / war", "/wɒ/ & /wɔː/", "wah",
                "\"WA\" đọc là /wɒ/, \"WAR\" đọc là /wɔː/",
                "Chữ 'a' đi sau 'w' thường bị biến đổi: đọc là /ɒ/ ngắn (như watch, wash) hoặc đọc là /ɔː/ dài khi đi cùng r (như war, warm).",
                "NGUYEN_AM_DAC_BIET", "🌟 Nguyên âm đặc biệt", "🌊",
                "Khẩu hình: Tròn môi phát âm /w/, sau đó mở vòm họng phát âm tròn /ɒ/ hoặc /ɔː/.",
                List.of(
                        new TuMinhHoaDTO("water", "wa", "/ˈwɔː.tər/", "wɔː", "nước uống", "💧", "/audio/tts?text=water"),
                        new TuMinhHoaDTO("wash", "wa", "/wɒʃ/", "wɒ", "giặt giũ, rửa sạch", "🧼", "/audio/tts?text=wash"),
                        new TuMinhHoaDTO("watch", "wa", "/wɒtʃ/", "wɒ", "đồng hồ đeo tay, xem", "⌚", "/audio/tts?text=watch"),
                        new TuMinhHoaDTO("want", "wa", "/wɒnt/", "wɒ", "mong muốn, cần", "💭", "/audio/tts?text=want"),
                        new TuMinhHoaDTO("war", "war", "/wɔːr/", "wɔː", "chiến tranh, xung đột", "⚔️", "/audio/tts?text=war"),
                        new TuMinhHoaDTO("warm", "war", "/wɔːm/", "wɔː", "ấm áp, nồng hậu", "☀️", "/audio/tts?text=warm"),
                        new TuMinhHoaDTO("warn", "war", "/wɔːn/", "wɔː", "cảnh báo, nhắc nhở", "⚠️", "/audio/tts?text=warn"),
                        new TuMinhHoaDTO("wardrobe", "war", "/ˈwɔː.drəʊb/", "wɔː", "tủ quần áo", "🚪", "/audio/tts?text=wardrobe")
                )
        ));

        // -------------------------------------------------------------
        // NHÓM 2: ĐUÔI TỪ & HẬU TỐ (SUFFIXES & ENDINGS)
        // -------------------------------------------------------------
        themQuyLuat(new QuyLuatDanhVanDTO(
                "ise", "ISE", "/aɪz/", "eyes",
                "\"ISE\" đọc là /aɪz/",
                "Đuôi \"ise\" ở cuối các từ phổ biến thường được phát âm là nguyên âm đôi kết hợp phụ âm rung /aɪz/.",
                "DUOI_TU_HAU_TO", "🔖 Đuôi từ & Hậu tố", "✨",
                "Khẩu hình âm /aɪz/: Bắt đầu bằng mở rộng miệng phát âm /a/, sau đó thu nhẹ môi về âm /ɪ/, khép hai hàm răng nhẹ và rung dây thanh tạo âm /z/.",
                List.of(
                        new TuMinhHoaDTO("surprise", "ise", "/səˈpraɪz/", "aɪz", "ngạc nhiên, bất ngờ", "😲", "/audio/tts?text=surprise"),
                        new TuMinhHoaDTO("rise", "ise", "/raɪz/", "aɪz", "dâng lên, tăng lên", "📈", "/audio/tts?text=rise"),
                        new TuMinhHoaDTO("arise", "ise", "/əˈraɪz/", "aɪz", "phát sinh, nảy sinh", "⛰️", "/audio/tts?text=arise"),
                        new TuMinhHoaDTO("exercise", "ise", "/ˈek.sə.saɪz/", "aɪz", "tập thể dục, rèn luyện", "🏋️", "/audio/tts?text=exercise"),
                        new TuMinhHoaDTO("wise", "ise", "/waɪz/", "aɪz", "thông thái, uyên bác", "👴", "/audio/tts?text=wise"),
                        new TuMinhHoaDTO("advise", "ise", "/ədˈvaɪz/", "aɪz", "khuyên bảo, tư vấn", "🧑‍💼", "/audio/tts?text=advise"),
                        new TuMinhHoaDTO("realize", "ize", "/ˈrɪə.laɪz/", "aɪz", "nhận ra, thấu hiểu", "💡", "/audio/tts?text=realize"),
                        new TuMinhHoaDTO("organize", "ize", "/ˈɔː.ɡən.aɪz/", "aɪz", "tổ chức, sắp xếp", "📋", "/audio/tts?text=organize")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "tion_sion", "tion / sion", "/ʃn/ & /ʒn/", "shun",
                "Đuôi \"TION\" đọc là /ʃn/, \"SION\" đọc là /ʒn/",
                "Hậu tố danh từ -tion hầu như luôn đọc là /ʃn/, trong khi -sion sau nguyên âm thường đọc là âm rung /ʒn/.",
                "DUOI_TU_HAU_TO", "🔖 Đuôi từ & Hậu tố", "🎬",
                "Khẩu hình: Chu môi tròn hơi cong ra ngoài, đẩy luồng hơi êm qua khe hẹp tạo âm /ʃ/ hoặc rung họng tạo âm /ʒ/, rồi ngậm môi phát âm /n/.",
                List.of(
                        new TuMinhHoaDTO("action", "tion", "/ˈæk.ʃn/", "ʃn", "hành động, hoạt động", "🎬", "/audio/tts?text=action"),
                        new TuMinhHoaDTO("nation", "tion", "/ˈneɪ.ʃn/", "ʃn", "quốc gia, dân tộc", "🚩", "/audio/tts?text=nation"),
                        new TuMinhHoaDTO("station", "tion", "/ˈsteɪ.ʃn/", "ʃn", "nhà ga, trạm tàu", "🚉", "/audio/tts?text=station"),
                        new TuMinhHoaDTO("option", "tion", "/ˈɒp.ʃn/", "ʃn", "lựa chọn, phương án", "🔘", "/audio/tts?text=option"),
                        new TuMinhHoaDTO("vision", "sion", "/ˈvɪʒ.n/", "ʒn", "tầm nhìn, thị giác", "👁️", "/audio/tts?text=vision"),
                        new TuMinhHoaDTO("decision", "sion", "/dɪˈsɪʒ.n/", "ʒn", "quyết định quan trọng", "⚖️", "/audio/tts?text=decision"),
                        new TuMinhHoaDTO("television", "sion", "/ˈtel.ɪ.vɪʒ.n/", "ʒn", "ti vi, truyền hình", "📺", "/audio/tts?text=television"),
                        new TuMinhHoaDTO("conclusion", "sion", "/kənˈkluː.ʒn/", "ʒn", "kết luận, tổng kết", "🏁", "/audio/tts?text=conclusion")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ture", "ture", "/tʃə/", "cher",
                "Đuôi \"TURE\" đọc là /tʃə/",
                "Trong các từ đa âm tiết, nhóm hậu tố -ture thường không nhấn trọng âm và luôn được phát âm là /tʃə/.",
                "DUOI_TU_HAU_TO", "🔖 Đuôi từ & Hậu tố", "🖼️",
                "Khẩu hình: Bắt đầu đặt đầu lưỡi chạm ngạc cứng chặn hơi rồi bật nhanh âm /tʃ/ (như 'ch' tiếng Việt), sau đó thả lỏng môi về âm ơ ngắn /ə/.",
                List.of(
                        new TuMinhHoaDTO("nature", "ture", "/ˈneɪ.tʃər/", "tʃə", "tự nhiên, thiên nhiên", "🌿", "/audio/tts?text=nature"),
                        new TuMinhHoaDTO("picture", "ture", "/ˈpɪk.tʃər/", "tʃə", "bức tranh, tấm hình", "🖼️", "/audio/tts?text=picture"),
                        new TuMinhHoaDTO("future", "ture", "/ˈfjuː.tʃər/", "tʃə", "tương lai phía trước", "🚀", "/audio/tts?text=future"),
                        new TuMinhHoaDTO("culture", "ture", "/ˈkʌl.tʃər/", "tʃə", "văn hóa truyền thống", "🎭", "/audio/tts?text=culture"),
                        new TuMinhHoaDTO("adventure", "ture", "/ədˈven.tʃər/", "tʃə", "chuyến phiêu lưu", "🏕️", "/audio/tts?text=adventure"),
                        new TuMinhHoaDTO("feature", "ture", "/ˈfiː.tʃər/", "tʃə", "tính năng, đặc điểm", "⭐", "/audio/tts?text=feature"),
                        new TuMinhHoaDTO("furniture", "ture", "/ˈfɜː.nɪ.tʃər/", "tʃə", "đồ đạc nội thất", "🛋️", "/audio/tts?text=furniture"),
                        new TuMinhHoaDTO("creature", "ture", "/ˈkriː.tʃər/", "tʃə", "sinh vật, con vật", "🦄", "/audio/tts?text=creature")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ous", "ous", "/əs/", "us",
                "Đuôi \"OUS\" đọc là /əs/",
                "Hậu tố tính từ -ous khi đứng ở cuối từ không mang trọng âm và luôn được phát âm rất nhẹ là /əs/.",
                "DUOI_TU_HAU_TO", "🔖 Đuôi từ & Hậu tố", "🌟",
                "Khẩu hình: Thả lỏng vòm miệng tạo âm ơ cực ngắn /ə/, sau đó khép răng thổi luồng hơi xì sắc nét tạo âm /s/.",
                List.of(
                        new TuMinhHoaDTO("famous", "ous", "/ˈfeɪ.məs/", "əs", "nổi tiếng lừng danh", "🌟", "/audio/tts?text=famous"),
                        new TuMinhHoaDTO("dangerous", "ous", "/ˈdeɪn.dʒər.əs/", "əs", "nguy hiểm, rủi ro", "⚠️", "/audio/tts?text=dangerous"),
                        new TuMinhHoaDTO("delicious", "ous", "/dɪˈlɪʃ.əs/", "əs", "thơm ngon hảo hạng", "😋", "/audio/tts?text=delicious"),
                        new TuMinhHoaDTO("nervous", "ous", "/ˈnɜː.vəs/", "əs", "hồi hộp, lo lắng", "😰", "/audio/tts?text=nervous"),
                        new TuMinhHoaDTO("continuous", "ous", "/kənˈtɪn.ju.əs/", "əs", "liên tục, không ngừng", "🔁", "/audio/tts?text=continuous"),
                        new TuMinhHoaDTO("generous", "ous", "/ˈdʒen.ər.əs/", "əs", "hào phóng, rộng lượng", "🎁", "/audio/tts?text=generous"),
                        new TuMinhHoaDTO("serious", "ous", "/ˈsɪə.ri.əs/", "əs", "nghiêm túc, trầm trọng", "🧐", "/audio/tts?text=serious"),
                        new TuMinhHoaDTO("obvious", "ous", "/ˈɒb.vi.əs/", "əs", "rõ ràng, hiển nhiên", "👀", "/audio/tts?text=obvious")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "age_ending", "age", "/ɪdʒ/", "idge",
                "Đuôi \"AGE\" trong từ đa âm đọc là /ɪdʒ/",
                "Khi đuôi -age nằm ở âm tiết không nhấn trọng âm của từ có từ 2 âm tiết trở lên, nó luôn phát âm là /ɪdʒ/ thay vì /eɪdʒ/.",
                "DUOI_TU_HAU_TO", "🔖 Đuôi từ & Hậu tố", "📦",
                "Khẩu hình: Miệng mở hé phát âm /ɪ/ ngắn rồi khép răng bật âm /dʒ/ có rung thanh quản (như âm gi).",
                List.of(
                        new TuMinhHoaDTO("village", "age", "/ˈvɪl.ɪdʒ/", "ɪdʒ", "ngôi làng quê thanh bình", "🏡", "/audio/tts?text=village"),
                        new TuMinhHoaDTO("message", "age", "/ˈmes.ɪdʒ/", "ɪdʒ", "tin nhắn, thông điệp", "💬", "/audio/tts?text=message"),
                        new TuMinhHoaDTO("package", "age", "/ˈpæk.ɪdʒ/", "ɪdʒ", "kiện hàng, bưu phẩm", "📦", "/audio/tts?text=package"),
                        new TuMinhHoaDTO("luggage", "age", "/ˈlʌɡ.ɪdʒ/", "ɪdʒ", "hành lý du lịch", "🧳", "/audio/tts?text=luggage"),
                        new TuMinhHoaDTO("damage", "age", "/ˈdæm.ɪdʒ/", "ɪdʒ", "thiệt hại, hư hỏng", "💥", "/audio/tts?text=damage"),
                        new TuMinhHoaDTO("storage", "age", "/ˈstɔː.rɪdʒ/", "ɪdʒ", "kho lưu trữ, lưu trữ", "🗄️", "/audio/tts?text=storage"),
                        new TuMinhHoaDTO("cottage", "age", "/ˈkɒt.ɪdʒ/", "ɪdʒ", "ngôi nhà tranh nhỏ", "🛖", "/audio/tts?text=cottage"),
                        new TuMinhHoaDTO("courage", "age", "/ˈkʌr.ɪdʒ/", "ɪdʒ", "sự can đảm, lòng dũng cảm", "🦁", "/audio/tts?text=courage")
                )
        ));

        // -------------------------------------------------------------
        // NHÓM 3: NGUYÊN ÂM ĐÔI & NHÓM CHỮ CÁI (VOWEL DIGRAPHS)
        // -------------------------------------------------------------
        themQuyLuat(new QuyLuatDanhVanDTO(
                "ea", "EA", "/iː/", "ee",
                "Nhóm \"EA\" thường đọc là /iː/",
                "Trong phần lớn các từ tiếng Anh thông dụng, hai chữ cái 'ea' đi liền nhau được phát âm là nguyên âm dài /iː/ (cười mỉm).",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "🍵",
                "Khẩu hình âm /iː/: Kéo căng khóe môi sang hai bên giống như đang mỉm cười, nâng cao đầu lưỡi và phát âm chữ i ngân dài.",
                List.of(
                        new TuMinhHoaDTO("tea", "ea", "/tiː/", "iː", "tách trà nóng", "🍵", "/audio/tts?text=tea"),
                        new TuMinhHoaDTO("sea", "ea", "/siː/", "iː", "biển cả bao la", "🌊", "/audio/tts?text=sea"),
                        new TuMinhHoaDTO("meat", "ea", "/miːt/", "iː", "thịt động vật", "🥩", "/audio/tts?text=meat"),
                        new TuMinhHoaDTO("read", "ea", "/riːd/", "iː", "đọc sách báo", "📖", "/audio/tts?text=read"),
                        new TuMinhHoaDTO("peach", "ea", "/piːtʃ/", "iː", "quả đào ngọt", "🍑", "/audio/tts?text=peach"),
                        new TuMinhHoaDTO("beach", "ea", "/biːtʃ/", "iː", "bãi biển cát trắng", "🏖️", "/audio/tts?text=beach"),
                        new TuMinhHoaDTO("clean", "ea", "/kliːn/", "iː", "sạch sẽ, lau dọn", "✨", "/audio/tts?text=clean"),
                        new TuMinhHoaDTO("leader", "ea", "/ˈliː.dər/", "iː", "người dẫn đầu, thủ lĩnh", "👑", "/audio/tts?text=leader")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ee", "EE", "/iː/", "ee",
                "Nhóm \"EE\" luôn đọc là /iː/",
                "Hai chữ cái 'ee' đứng liền nhau trong tiếng Anh luôn giữ nguyên tắc phát âm 100% thành nguyên âm dài /iː/.",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "🌳",
                "Khẩu hình âm /iː/: Mỉm cười dẹt môi sang 2 bên, lưỡi đưa cao lên gần vòm miệng trên, ngân dài hơi.",
                List.of(
                        new TuMinhHoaDTO("see", "ee", "/siː/", "iː", "nhìn thấy, quan sát", "👀", "/audio/tts?text=see"),
                        new TuMinhHoaDTO("tree", "ee", "/triː/", "iː", "cây xanh", "🌳", "/audio/tts?text=tree"),
                        new TuMinhHoaDTO("bee", "ee", "/biː/", "iː", "con ong chăm chỉ", "🐝", "/audio/tts?text=bee"),
                        new TuMinhHoaDTO("meet", "ee", "/miːt/", "iː", "gặp gỡ bạn bè", "🤝", "/audio/tts?text=meet"),
                        new TuMinhHoaDTO("feel", "ee", "/fiːl/", "iː", "cảm nhận, cảm xúc", "❤️", "/audio/tts?text=feel"),
                        new TuMinhHoaDTO("green", "ee", "/ɡriːn/", "iː", "màu xanh lá cây", "🟢", "/audio/tts?text=green"),
                        new TuMinhHoaDTO("sleep", "ee", "/sliːp/", "iː", "giấc ngủ say", "😴", "/audio/tts?text=sleep"),
                        new TuMinhHoaDTO("deep", "ee", "/diːp/", "iː", "sâu thẳm", "🕳️", "/audio/tts?text=deep")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "oo_long", "OO (dài)", "/uː/", "oo",
                "Nhóm \"OO\" đọc là âm dài /uː/",
                "Trong phần lớn các từ, chữ 'oo' được đọc là âm dài /uː/ chu môi sâu (như moon, food, spoon).",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "🌕",
                "Khẩu hình âm /uː/: Chu tròn môi thật chặt nhô ra phía trước như đang huýt sáo, nâng cao cuống lưỡi và phát âm u ngân dài.",
                List.of(
                        new TuMinhHoaDTO("moon", "oo", "/muːn/", "uː", "mặt trăng sáng", "🌕", "/audio/tts?text=moon"),
                        new TuMinhHoaDTO("food", "oo", "/fuːd/", "uː", "thức ăn, món ăn", "🍲", "/audio/tts?text=food"),
                        new TuMinhHoaDTO("spoon", "oo", "/spuːn/", "uː", "cái thìa, cái muỗng", "🥄", "/audio/tts?text=spoon"),
                        new TuMinhHoaDTO("pool", "oo", "/puːl/", "uː", "hồ bơi xanh mát", "🏊", "/audio/tts?text=pool"),
                        new TuMinhHoaDTO("cool", "oo", "/kuːl/", "uː", "mát mẻ, cực ngầu", "😎", "/audio/tts?text=cool"),
                        new TuMinhHoaDTO("zoo", "oo", "/zuː/", "uː", "vườn bách thú", "🦁", "/audio/tts?text=zoo"),
                        new TuMinhHoaDTO("root", "oo", "/ruːt/", "uː", "rễ cây, nguồn cội", "🌱", "/audio/tts?text=root"),
                        new TuMinhHoaDTO("school", "oo", "/skuːl/", "uː", "trường học", "🏫", "/audio/tts?text=school")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "oo_short", "OO (ngắn)", "/ʊ/", "uh",
                "Nhóm \"OO\" đọc là âm ngắn /ʊ/ trước k, d",
                "Khi 'oo' đứng trước chữ 'k' (book, look) hoặc một số từ với 'd', 't' (good, foot, wood), nó được phát âm là âm ngắn dứt khoát /ʊ/.",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "📚",
                "Khẩu hình âm /ʊ/: Môi hơi tròn nhưng thả lỏng tự nhiên (không chu nhọn như /uː/), phát âm chữ u thật nhanh và dứt khoát.",
                List.of(
                        new TuMinhHoaDTO("book", "oo", "/bʊk/", "ʊ", "cuốn sách học", "📚", "/audio/tts?text=book"),
                        new TuMinhHoaDTO("look", "oo", "/lʊk/", "ʊ", "nhìn ngắm, quan sát", "👀", "/audio/tts?text=look"),
                        new TuMinhHoaDTO("cook", "oo", "/kʊk/", "ʊ", "nấu nướng món ăn", "👨‍🍳", "/audio/tts?text=cook"),
                        new TuMinhHoaDTO("good", "oo", "/ɡʊd/", "ʊ", "tốt lành, giỏi giang", "👍", "/audio/tts?text=good"),
                        new TuMinhHoaDTO("foot", "oo", "/fʊt/", "ʊ", "bàn chân", "🦶", "/audio/tts?text=foot"),
                        new TuMinhHoaDTO("wood", "oo", "/wʊd/", "ʊ", "gỗ, rừng cây", "🪵", "/audio/tts?text=wood"),
                        new TuMinhHoaDTO("took", "oo", "/tʊk/", "ʊ", "đã cầm, đã lấy", "🤲", "/audio/tts?text=took"),
                        new TuMinhHoaDTO("wool", "oo", "/wʊl/", "ʊ", "len sợi dệt", "🧶", "/audio/tts?text=wool")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "igh", "IGH", "/aɪ/", "eye",
                "Nhóm \"IGH\" đọc là /aɪ/ (gh câm)",
                "Cụm chữ cái 'igh' luôn được phát âm là nguyên âm đôi /aɪ/, trong đó hai chữ 'gh' hoàn toàn câm không phát ra tiếng.",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "💡",
                "Khẩu hình: Mở to miệng tạo âm /a/ rồi khép nhẹ về âm /ɪ/ thanh mảnh.",
                List.of(
                        new TuMinhHoaDTO("high", "igh", "/haɪ/", "aɪ", "trên cao, đỉnh cao", "🏔️", "/audio/tts?text=high"),
                        new TuMinhHoaDTO("night", "igh", "/naɪt/", "aɪ", "màn đêm yên tĩnh", "🌙", "/audio/tts?text=night"),
                        new TuMinhHoaDTO("light", "igh", "/laɪt/", "aɪ", "ánh sáng mặt trời, đèn", "💡", "/audio/tts?text=light"),
                        new TuMinhHoaDTO("bright", "igh", "/braɪt/", "aɪ", "sáng ngời, rực rỡ", "✨", "/audio/tts?text=bright"),
                        new TuMinhHoaDTO("fight", "igh", "/faɪt/", "aɪ", "chiến đấu, đấu tranh", "🥊", "/audio/tts?text=fight"),
                        new TuMinhHoaDTO("flight", "igh", "/flaɪt/", "aɪ", "chuyến bay trên trời", "✈️", "/audio/tts?text=flight"),
                        new TuMinhHoaDTO("right", "igh", "/raɪt/", "aɪ", "bên phải, chính xác", "➡️", "/audio/tts?text=right"),
                        new TuMinhHoaDTO("sight", "igh", "/saɪt/", "aɪ", "thị giác, cảnh đẹp", "🔭", "/audio/tts?text=sight")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "oa", "OA", "/oʊ/", "oh",
                "Nhóm \"OA\" đọc là /oʊ/",
                "Khi hai chữ cái 'oa' đi liền nhau, chữ 'a' câm và chữ 'o' phát âm thành nguyên âm đôi /oʊ/ (như boat, road, soap).",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "⛵",
                "Khẩu hình: Mở miệng tròn chữ /o/ sau đó thu nhỏ đầu môi về chữ /ʊ/.",
                List.of(
                        new TuMinhHoaDTO("boat", "oa", "/bəʊt/", "oʊ", "chiếc thuyền buồm", "⛵", "/audio/tts?text=boat"),
                        new TuMinhHoaDTO("coat", "oa", "/kəʊt/", "oʊ", "áo khoác ấm", "🧥", "/audio/tts?text=coat"),
                        new TuMinhHoaDTO("road", "oa", "/rəʊd/", "oʊ", "con đường đi", "🛣️", "/audio/tts?text=road"),
                        new TuMinhHoaDTO("soap", "oa", "/səʊp/", "oʊ", "bánh xà phòng thơm", "🧼", "/audio/tts?text=soap"),
                        new TuMinhHoaDTO("goal", "oa", "/ɡəʊl/", "oʊ", "bàn thắng, mục tiêu", "⚽", "/audio/tts?text=goal"),
                        new TuMinhHoaDTO("toast", "oa", "/təʊst/", "oʊ", "bánh mì nướng", "🍞", "/audio/tts?text=toast"),
                        new TuMinhHoaDTO("float", "oa", "/fləʊt/", "oʊ", "nổi trên mặt nước", "🛟", "/audio/tts?text=float"),
                        new TuMinhHoaDTO("coach", "oa", "/kəʊtʃ/", "oʊ", "huấn luyện viên, xe khách", "🚌", "/audio/tts?text=coach")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ai_ay", "AI / AY", "/eɪ/", "ay",
                "\"AI / AY\" đọc là /eɪ/",
                "Cụm 'ai' (thường ở giữa từ) và 'ay' (ở cuối từ) luôn phát âm là nguyên âm đôi /eɪ/ giống chữ 'ê-i' tiếng Việt nói liền.",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "🌧️",
                "Khẩu hình: Miệng mở vừa âm /e/ rồi kéo dẹt môi sang hai bên kết thúc ở /ɪ/.",
                List.of(
                        new TuMinhHoaDTO("rain", "ai", "/reɪn/", "eɪ", "cơn mưa rào", "🌧️", "/audio/tts?text=rain"),
                        new TuMinhHoaDTO("train", "ai", "/treɪn/", "eɪ", "chuyến tàu hỏa", "🚆", "/audio/tts?text=train"),
                        new TuMinhHoaDTO("wait", "ai", "/weɪt/", "eɪ", "chờ đợi, kiên nhẫn", "⏳", "/audio/tts?text=wait"),
                        new TuMinhHoaDTO("pain", "ai", "/peɪn/", "eɪ", "nỗi đau đớn", "🩹", "/audio/tts?text=pain"),
                        new TuMinhHoaDTO("day", "ay", "/deɪ/", "eɪ", "ngày, ban ngày", "☀️", "/audio/tts?text=day"),
                        new TuMinhHoaDTO("play", "ay", "/pleɪ/", "eɪ", "vui chơi giải trí", "🎮", "/audio/tts?text=play"),
                        new TuMinhHoaDTO("say", "ay", "/seɪ/", "eɪ", "nói rằng, phát biểu", "🗣️", "/audio/tts?text=say"),
                        new TuMinhHoaDTO("stay", "ay", "/steɪ/", "eɪ", "ở lại, lưu trú", "🏨", "/audio/tts?text=stay")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "oi_oy", "OI / OY", "/ɔɪ/", "oy",
                "\"OI / OY\" đọc là /ɔɪ/",
                "Cụm 'oi' (trong thân từ) và 'oy' (cuối từ) luôn phát âm là nguyên âm đôi /ɔɪ/ (như coin, boy, enjoy).",
                "NGUYEN_AM_DOI", "🎶 Nguyên âm đôi", "🪙",
                "Khẩu hình: Bắt đầu tròn môi /ɔː/ sau đó trượt môi mỉm cười nhẹ về âm /ɪ/.",
                List.of(
                        new TuMinhHoaDTO("coin", "oi", "/kɔɪn/", "ɔɪ", "đồng tiền xu", "🪙", "/audio/tts?text=coin"),
                        new TuMinhHoaDTO("boil", "oi", "/bɔɪl/", "ɔɪ", "đun sôi nước", "🫖", "/audio/tts?text=boil"),
                        new TuMinhHoaDTO("join", "oi", "/dʒɔɪn/", "ɔɪ", "tham gia cùng nhau", "🤝", "/audio/tts?text=join"),
                        new TuMinhHoaDTO("voice", "oi", "/vɔɪs/", "ɔɪ", "giọng nói ngọt ngào", "🎙️", "/audio/tts?text=voice"),
                        new TuMinhHoaDTO("point", "oi", "/pɔɪnt/", "ɔɪ", "điểm số, chỉ tay", "👉", "/audio/tts?text=point"),
                        new TuMinhHoaDTO("boy", "oy", "/bɔɪ/", "ɔɪ", "cậu bé trai", "👦", "/audio/tts?text=boy"),
                        new TuMinhHoaDTO("toy", "oy", "/tɔɪ/", "ɔɪ", "đồ chơi trẻ em", "🧸", "/audio/tts?text=toy"),
                        new TuMinhHoaDTO("enjoy", "oy", "/ɪnˈdʒɔɪ/", "ɔɪ", "thưởng thức, vui thích", "🎉", "/audio/tts?text=enjoy")
                )
        ));

        // -------------------------------------------------------------
        // NHÓM 4: PHỤ ÂM KÉP & ÂM CÂM (CONSONANT DIGRAPHS & SILENT LETTERS)
        // -------------------------------------------------------------
        themQuyLuat(new QuyLuatDanhVanDTO(
                "ch_sound", "CH", "/tʃ/", "ch",
                "Nhóm \"CH\" thường đọc là /tʃ/",
                "Cụm chữ cái 'ch' trong tiếng Anh nguyên bản thường phát âm bật hơi mạnh /tʃ/ (tương tự âm 'ch' nhưng bật hơi mạnh mẽ hơn).",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "🪑",
                "Khẩu hình: Chạm đầu lưỡi lên chân răng hàm trên nén luồng hơi, sau đó bật mạnh đẩy luồng hơi ra đồng thời mở môi tròn.",
                List.of(
                        new TuMinhHoaDTO("chair", "ch", "/tʃeər/", "tʃ", "chiếc ghế tựa", "🪑", "/audio/tts?text=chair"),
                        new TuMinhHoaDTO("cheese", "ch", "/tʃiːz/", "tʃ", "miếng phô mai béo", "🧀", "/audio/tts?text=cheese"),
                        new TuMinhHoaDTO("church", "ch", "/tʃɜːtʃ/", "tʃ", "nhà thờ tôn nghiêm", "⛪", "/audio/tts?text=church"),
                        new TuMinhHoaDTO("child", "ch", "/tʃaɪld/", "tʃ", "đứa trẻ nhỏ", "🧒", "/audio/tts?text=child"),
                        new TuMinhHoaDTO("check", "ch", "/tʃek/", "tʃ", "kiểm tra, đánh dấu", "✅", "/audio/tts?text=check"),
                        new TuMinhHoaDTO("rich", "ch", "/rɪtʃ/", "tʃ", "giàu có, thịnh vượng", "💰", "/audio/tts?text=rich"),
                        new TuMinhHoaDTO("beach", "ch", "/biːtʃ/", "tʃ", "bờ biển đẹp", "🏖️", "/audio/tts?text=beach"),
                        new TuMinhHoaDTO("catch", "tch", "/kætʃ/", "tʃ", "bắt lấy, chộp lấy", "🧤", "/audio/tts?text=catch")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ch_greek", "CH (gốc Hy Lạp)", "/k/", "k",
                "\"CH\" gốc Hy Lạp đọc là /k/",
                "Trong các từ có nguồn gốc Hy Lạp (thường là từ vựng khoa học, trường học, hóa học), 'ch' được phát âm là /k/.",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "🧪",
                "Khẩu hình: Nâng phần sau của lưỡi chạm vào ngạc mềm (vòm miệng gà) chặn hơi rồi bật nhanh luồng hơi tạo âm /k/.",
                List.of(
                        new TuMinhHoaDTO("school", "ch", "/skuːl/", "k", "trường học thân yêu", "🏫", "/audio/tts?text=school"),
                        new TuMinhHoaDTO("chemist", "ch", "/ˈkem.ɪst/", "k", "nhà hóa học, dược sĩ", "🧪", "/audio/tts?text=chemist"),
                        new TuMinhHoaDTO("character", "ch", "/ˈkær.ək.tər/", "k", "nhân vật, tính cách", "🎭", "/audio/tts?text=character"),
                        new TuMinhHoaDTO("stomach", "ch", "/ˈstʌm.ək/", "k", "dạ dày, bụng", "🤰", "/audio/tts?text=stomach"),
                        new TuMinhHoaDTO("ache", "ch", "/eɪk/", "k", "cơn đau nhức", "🤕", "/audio/tts?text=ache"),
                        new TuMinhHoaDTO("echo", "ch", "/ˈek.əʊ/", "k", "tiếng vọng âm vang", "📢", "/audio/tts?text=echo"),
                        new TuMinhHoaDTO("technology", "ch", "/tekˈnɒl.ə.dʒi/", "k", "công nghệ hiện đại", "💻", "/audio/tts?text=technology"),
                        new TuMinhHoaDTO("chorus", "ch", "/ˈkɔː.rəs/", "k", "dàn đồng ca, điệp khúc", "🎶", "/audio/tts?text=chorus")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "sh_sound", "SH", "/ʃ/", "sh",
                "Nhóm \"SH\" luôn đọc là /ʃ/",
                "Cụm chữ cái 'sh' luôn được phát âm là âm vô thanh xì dài êm dịu /ʃ/ (như động tác 'suỵt' giữ im lặng).",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "🚢",
                "Khẩu hình âm /ʃ/: Chu môi hơi tròn, mặt lưỡi nâng nhẹ lên vòm ngạc, thổi luồng hơi dài êm qua kẽ răng.",
                List.of(
                        new TuMinhHoaDTO("ship", "sh", "/ʃɪp/", "ʃ", "tàu thủy lớn", "🚢", "/audio/tts?text=ship"),
                        new TuMinhHoaDTO("shop", "sh", "/ʃɒp/", "ʃ", "cửa hàng mua sắm", "🛍️", "/audio/tts?text=shop"),
                        new TuMinhHoaDTO("fish", "sh", "/fɪʃ/", "ʃ", "con cá tung tăng", "🐟", "/audio/tts?text=fish"),
                        new TuMinhHoaDTO("wish", "sh", "/wɪʃ/", "ʃ", "điều ước mong manh", "🌠", "/audio/tts?text=wish"),
                        new TuMinhHoaDTO("shell", "sh", "/ʃel/", "ʃ", "vỏ sò, vỏ ốc", "🐚", "/audio/tts?text=shell"),
                        new TuMinhHoaDTO("shine", "sh", "/ʃaɪn/", "ʃ", "tỏa sáng rực rỡ", "✨", "/audio/tts?text=shine"),
                        new TuMinhHoaDTO("shoe", "sh", "/ʃuː/", "ʃ", "chiếc giày da", "👞", "/audio/tts?text=shoe"),
                        new TuMinhHoaDTO("brush", "sh", "/brʌʃ/", "ʃ", "bàn chải, chải tóc", "🪥", "/audio/tts?text=brush")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "th_unvoiced", "TH (vô thanh)", "/θ/", "th",
                "\"TH\" vô thanh đọc là /θ/ (thổi hơi)",
                "Nhóm chữ 'th' đứng đầu các từ danh từ/động từ/tính từ thường đọc là âm vô thanh kẹp lưỡi thổi hơi /θ/.",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "💭",
                "Khẩu hình âm /θ/: Đặt nhẹ đầu lưỡi ở giữa hai hàm răng cửa, không cắn chặt, thổi luồng hơi êm qua kẽ lưỡi mà không rung cổ họng.",
                List.of(
                        new TuMinhHoaDTO("think", "th", "/θɪŋk/", "θ", "suy nghĩ, tư duy", "💭", "/audio/tts?text=think"),
                        new TuMinhHoaDTO("thank", "th", "/θæŋk/", "θ", "cảm ơn chân thành", "🙏", "/audio/tts?text=thank"),
                        new TuMinhHoaDTO("thin", "th", "/θɪn/", "θ", "mảnh mai, gầy gò", "📏", "/audio/tts?text=thin"),
                        new TuMinhHoaDTO("tooth", "th", "/tuːθ/", "θ", "răng cửa", "🦷", "/audio/tts?text=tooth"),
                        new TuMinhHoaDTO("math", "th", "/mæθ/", "θ", "môn toán học", "📐", "/audio/tts?text=math"),
                        new TuMinhHoaDTO("birth", "th", "/bɜːθ/", "θ", "sự ra đời, ngày sinh", "🎂", "/audio/tts?text=birth"),
                        new TuMinhHoaDTO("earth", "th", "/ɜːθ/", "θ", "trái đất tươi đẹp", "🌍", "/audio/tts?text=earth"),
                        new TuMinhHoaDTO("month", "th", "/mʌnθ/", "θ", "tháng trong năm", "📅", "/audio/tts?text=month")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "th_voiced", "TH (hữu thanh)", "/ð/", "th",
                "\"TH\" hữu thanh đọc là /ð/ (rung lưỡi)",
                "Nhóm chữ 'th' trong các đại từ, mạo từ (this, that, the, they) hoặc đứng giữa hai nguyên âm (father, brother) luôn đọc rung cổ họng /ð/.",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "👨‍👩‍👧",
                "Khẩu hình âm /ð/: Đặt nhẹ đầu lưỡi giữa hai hàm răng giống âm /θ/, nhưng kết hợp làm rung dây thanh quản tạo tiếng vo ve.",
                List.of(
                        new TuMinhHoaDTO("this", "th", "/ðɪs/", "ð", "cái này, người này", "👇", "/audio/tts?text=this"),
                        new TuMinhHoaDTO("that", "th", "/ðæt/", "ð", "cái kia, điều đó", "👉", "/audio/tts?text=that"),
                        new TuMinhHoaDTO("these", "th", "/ðiːz/", "ð", "những cái này", "👐", "/audio/tts?text=these"),
                        new TuMinhHoaDTO("those", "th", "/ðəʊz/", "ð", "những cái kia", "🙌", "/audio/tts?text=those"),
                        new TuMinhHoaDTO("father", "th", "/ˈfɑː.ðər/", "ð", "người cha kính yêu", "👨", "/audio/tts?text=father"),
                        new TuMinhHoaDTO("mother", "th", "/ˈmʌð.ər/", "ð", "người mẹ dịu hiền", "👩", "/audio/tts?text=mother"),
                        new TuMinhHoaDTO("brother", "th", "/ˈbrʌð.ər/", "ð", "anh em trai", "👦", "/audio/tts?text=brother"),
                        new TuMinhHoaDTO("breathe", "th", "/briːð/", "ð", "hít thở không khí", "🫁", "/audio/tts?text=breathe")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "ph_sound", "PH", "/f/", "f",
                "Nhóm \"PH\" luôn đọc là /f/",
                "Hai chữ cái 'ph' khi đi liền nhau luôn luôn được phát âm thành phụ âm /f/ (như phone, photo, elephant).",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "📱",
                "Khẩu hình âm /f/: Chạm nhẹ răng cửa hàm trên vào môi dưới, đẩy luồng hơi êm qua khe hở giữa răng và môi.",
                List.of(
                        new TuMinhHoaDTO("phone", "ph", "/fəʊn/", "f", "điện thoại di động", "📱", "/audio/tts?text=phone"),
                        new TuMinhHoaDTO("photo", "ph", "/ˈfəʊ.təʊ/", "f", "bức ảnh chụp", "📷", "/audio/tts?text=photo"),
                        new TuMinhHoaDTO("phantom", "ph", "/ˈfæn.təm/", "f", "bóng ma, ảo ảnh", "👻", "/audio/tts?text=phantom"),
                        new TuMinhHoaDTO("elephant", "ph", "/ˈel.ɪ.fənt/", "f", "chú voi to lớn", "🐘", "/audio/tts?text=elephant"),
                        new TuMinhHoaDTO("dolphin", "ph", "/ˈdɒl.fɪn/", "f", "chú cá heo thông minh", "🐬", "/audio/tts?text=dolphin"),
                        new TuMinhHoaDTO("paragraph", "ph", "/ˈpær.ə.ɡrɑːf/", "f", "đoạn văn bản", "📄", "/audio/tts?text=paragraph"),
                        new TuMinhHoaDTO("alphabet", "ph", "/ˈæl.fə.bet/", "f", "bảng chữ cái", "🔤", "/audio/tts?text=alphabet"),
                        new TuMinhHoaDTO("phrase", "ph", "/freɪz/", "f", "cụm từ ngữ", "💬", "/audio/tts?text=phrase")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "kn_silent", "KN", "/n/", "n",
                "\"KN\" đầu từ: K câm, chỉ đọc là /n/",
                "Khi chữ 'k' đứng trước 'n' ở đầu một từ hoặc gốc từ, chữ 'k' là âm câm 100%, chỉ đọc từ phụ âm /n/.",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "🔪",
                "Khẩu hình: Bỏ qua chữ k, đặt đầu lưỡi chạm vào nướu răng hàm trên tạo luồng hơi qua mũi phát âm /n/.",
                List.of(
                        new TuMinhHoaDTO("know", "kn", "/nəʊ/", "n", "biết, hiểu rõ", "🧠", "/audio/tts?text=know"),
                        new TuMinhHoaDTO("knife", "kn", "/naɪf/", "n", "con dao sắc bén", "🔪", "/audio/tts?text=knife"),
                        new TuMinhHoaDTO("knee", "kn", "/niː/", "n", "đầu gối chân", "🦵", "/audio/tts?text=knee"),
                        new TuMinhHoaDTO("knock", "kn", "/nɒk/", "n", "gõ cửa cốc cốc", "🚪", "/audio/tts?text=knock"),
                        new TuMinhHoaDTO("knight", "kn", "/naɪt/", "n", "hiệp sĩ dũng cảm", "🛡️", "/audio/tts?text=knight"),
                        new TuMinhHoaDTO("knot", "kn", "/nɒt/", "n", "nút thắt dây", "🪢", "/audio/tts?text=knot"),
                        new TuMinhHoaDTO("knit", "kn", "/nɪt/", "n", "đan len sợi", "🧶", "/audio/tts?text=knit"),
                        new TuMinhHoaDTO("kneel", "kn", "/niːl/", "n", "quỳ gối xuống", "🧎", "/audio/tts?text=kneel")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "wr_silent", "WR", "/r/", "r",
                "\"WR\" đầu từ: W câm, chỉ đọc là /r/",
                "Khi chữ 'w' đứng trước 'r' ở đầu từ, chữ 'w' luôn luôn câm, người học chỉ cần phát âm từ chữ /r/.",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "✍️",
                "Khẩu hình: Bỏ qua chữ w, hơi chu nhẹ môi và cuộn đầu lưỡi về sau phát âm /r/ tròn trịa.",
                List.of(
                        new TuMinhHoaDTO("write", "wr", "/raɪt/", "r", "viết bài, viết thư", "✍️", "/audio/tts?text=write"),
                        new TuMinhHoaDTO("wrong", "wr", "/rɒŋ/", "r", "sai lầm, sai trái", "❌", "/audio/tts?text=wrong"),
                        new TuMinhHoaDTO("wrist", "wr", "/rɪst/", "r", "cổ tay", "⌚", "/audio/tts?text=wrist"),
                        new TuMinhHoaDTO("wrap", "wr", "/ræp/", "r", "gói quà, bọc lại", "🎁", "/audio/tts?text=wrap"),
                        new TuMinhHoaDTO("wreck", "wr", "/rek/", "r", "xác tàu đắm, phá hủy", "🚢", "/audio/tts?text=wreck"),
                        new TuMinhHoaDTO("wrinkle", "wr", "/ˈrɪŋ.kl/", "r", "nếp nhăn da", "👴", "/audio/tts?text=wrinkle"),
                        new TuMinhHoaDTO("wreath", "wr", "/riːθ/", "r", "vòng hoa tươi", "💐", "/audio/tts?text=wreath"),
                        new TuMinhHoaDTO("wrestler", "wr", "/ˈres.lər/", "r", "đô vật, võ sĩ", "🤼", "/audio/tts?text=wrestler")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "mb_silent", "MB", "/m/", "m",
                "\"MB\" cuối từ: B câm, chỉ đọc là /m/",
                "Khi cụm 'mb' xuất hiện ở cuối từ, chữ 'b' là âm câm hoàn toàn, bạn chỉ cần ngậm môi phát âm chữ /m/.",
                "PHU_AM_KEP_CAM", "🔇 Phụ âm kép & Âm câm", "🧗",
                "Khẩu hình: Bỏ qua âm b, chỉ cần mím chặt hai môi và đẩy luồng hơi nhẹ qua khoang mũi tạo âm /m/.",
                List.of(
                        new TuMinhHoaDTO("climb", "mb", "/klaɪm/", "m", "leo trèo núi", "🧗", "/audio/tts?text=climb"),
                        new TuMinhHoaDTO("thumb", "mb", "/θʌm/", "m", "ngón tay cái", "👍", "/audio/tts?text=thumb"),
                        new TuMinhHoaDTO("lamb", "mb", "/læm/", "m", "chú cừu non", "🐑", "/audio/tts?text=lamb"),
                        new TuMinhHoaDTO("bomb", "mb", "/bɒm/", "m", "quả bom nổ", "💣", "/audio/tts?text=bomb"),
                        new TuMinhHoaDTO("comb", "mb", "/kəʊm/", "m", "chiếc lược chải tóc", "🪮", "/audio/tts?text=comb"),
                        new TuMinhHoaDTO("tomb", "mb", "/tuːm/", "m", "lăng mộ cổ kính", "⚰️", "/audio/tts?text=tomb"),
                        new TuMinhHoaDTO("limb", "mb", "/lɪm/", "m", "chi, tay chân", "🦾", "/audio/tts?text=limb"),
                        new TuMinhHoaDTO("dumb", "mb", "/dʌm/", "m", "câm, ngốc nghếch", "🤐", "/audio/tts?text=dumb")
                )
        ));

        // -------------------------------------------------------------
        // NHÓM 5: BIẾN ÂM C VÀ G (SOFT & HARD C/G)
        // -------------------------------------------------------------
        themQuyLuat(new QuyLuatDanhVanDTO(
                "soft_c", "C + E / I / Y", "/s/", "s",
                "\"C\" đi trước e, i, y đọc là /s/ (C mềm)",
                "Quy tắc vàng: Chữ cái 'c' khi đứng trước 3 nguyên âm 'e', 'i', 'y' luôn bị làm mềm và phát âm thành /s/.",
                "BIEN_AM_C_G", "🔄 Biến âm C & G", "🏙️",
                "Khẩu hình âm /s/: Khép nhẹ hai hàm răng cửa, đưa đầu lưỡi gần nướu răng trên, thổi luồng hơi xì sắc bén không rung cổ họng.",
                List.of(
                        new TuMinhHoaDTO("city", "c", "/ˈsɪt.i/", "s", "thành phố hoa lệ", "🏙️", "/audio/tts?text=city"),
                        new TuMinhHoaDTO("center", "c", "/ˈsen.tər/", "s", "trung tâm hội nghị", "🎯", "/audio/tts?text=center"),
                        new TuMinhHoaDTO("cinema", "c", "/ˈsɪn.ə.mə/", "s", "rạp chiếu phim", "🍿", "/audio/tts?text=cinema"),
                        new TuMinhHoaDTO("circle", "c", "/ˈsɜː.kl/", "s", "hình tròn xoe", "⭕", "/audio/tts?text=circle"),
                        new TuMinhHoaDTO("cycle", "c", "/ˈsaɪ.kl/", "s", "chu kỳ, đạp xe", "🚲", "/audio/tts?text=cycle"),
                        new TuMinhHoaDTO("face", "ce", "/feɪs/", "s", "khuôn mặt xinh tươi", "😀", "/audio/tts?text=face"),
                        new TuMinhHoaDTO("price", "ce", "/praɪs/", "s", "giá cả món hàng", "🏷️", "/audio/tts?text=price"),
                        new TuMinhHoaDTO("nice", "ce", "/naɪs/", "s", "tốt bụng, dễ thương", "👌", "/audio/tts?text=nice")
                )
        ));

        themQuyLuat(new QuyLuatDanhVanDTO(
                "soft_g", "G + E / I / Y", "/dʒ/", "j",
                "\"G\" đi trước e, i, y thường đọc là /dʒ/ (G mềm)",
                "Chữ cái 'g' khi đứng trước 'e', 'i', 'y' đa số biến thành âm G mềm phát âm là /dʒ/ (như gem, gym, giant, cage).",
                "BIEN_AM_C_G", "🔄 Biến âm C & G", "💎",
                "Khẩu hình âm /dʒ/: Chu tròn môi nhẹ, áp đầu lưỡi lên chân răng hàm trên chặn hơi rồi bật nhanh âm có rung thanh quản mạnh.",
                List.of(
                        new TuMinhHoaDTO("gem", "g", "/dʒem/", "dʒ", "viên ngọc quý", "💎", "/audio/tts?text=gem"),
                        new TuMinhHoaDTO("ginger", "g", "/ˈdʒɪn.dʒər/", "dʒ", "củ gừng cay nồng", "🫚", "/audio/tts?text=ginger"),
                        new TuMinhHoaDTO("gym", "g", "/dʒɪm/", "dʒ", "phòng tập thể hình", "🏋️", "/audio/tts?text=gym"),
                        new TuMinhHoaDTO("cage", "ge", "/keɪdʒ/", "dʒ", "chiếc lồng chim", "🦜", "/audio/tts?text=cage"),
                        new TuMinhHoaDTO("giant", "g", "/ˈdʒaɪ.ənt/", "dʒ", "người khổng lồ", "🧌", "/audio/tts?text=giant"),
                        new TuMinhHoaDTO("magic", "g", "/ˈmædʒ.ɪk/", "dʒ", "phép thuật kỳ diệu", "🪄", "/audio/tts?text=magic"),
                        new TuMinhHoaDTO("gentle", "g", "/ˈdʒen.tl/", "dʒ", "dịu dàng, hiền từ", "🕊️", "/audio/tts?text=gentle"),
                        new TuMinhHoaDTO("danger", "g", "/ˈdeɪn.dʒər/", "dʒ", "mối nguy hiểm", "⚠️", "/audio/tts?text=danger")
                )
        ));
    }

    private void themQuyLuat(QuyLuatDanhVanDTO ql) {
        thuVienQuyLuat.put(ql.getId().toLowerCase(), ql);
    }
}
