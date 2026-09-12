package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.HuongDanDocDTO;
import com.thuong.vocabulary.service.GeminiService;
import com.thuong.vocabulary.service.HuongDanDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HuongDanDocServiceImpl implements HuongDanDocService {

    private final Map<String, HuongDanDocDTO> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiService geminiService;

    @Autowired
    public HuongDanDocServiceImpl(@Autowired(required = false) GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public HuongDanDocDTO layHuongDanDoc(String tu, String phienAm, String nghia) {
        if (tu == null || tu.isBlank()) {
            return layMauCoSan("hello", "/həˈloʊ/", "xin chào");
        }

        String tuChuanHoa = tu.trim().toLowerCase();
        if (cache.containsKey(tuChuanHoa)) {
            return cache.get(tuChuanHoa);
        }

        String phienAmChuan = chuanHoaIpaDauVao(phienAm);

        // 1. Kiểm tra từ điển chuẩn hóa đã được biên soạn kỹ lưỡng trước
        if (TU_DIEN_BOI_CHUAN.containsKey(tuChuanHoa)) {
            HuongDanDocDTO kq = layMauCoSan(tuChuanHoa, phienAmChuan, nghia);
            cache.put(tuChuanHoa, kq);
            return kq;
        }

        HuongDanDocDTO ketQua = null;

        // 2. Thử gọi Gemini AI nếu GeminiService khả dụng
        if (geminiService != null) {
            try {
                ketQua = goiGeminiPhanTich(tu.trim(), phienAmChuan, nghia);
                if (ketQua != null) {
                    ketQua = lamSachKetQua(ketQua);
                }
            } catch (Exception e) {
                System.err.println("[HuongDanDocService] Gọi Gemini không thành công: " + e.getMessage() + " -> Dùng bộ quy tắc ngữ âm dự phòng.");
            }
        }

        // 3. Nếu không có AI hoặc AI lỗi -> Dùng thuật toán phân tích ngữ âm thông minh dựa trên IPA & mặt chữ
        if (ketQua == null) {
            ketQua = taoHuongDanDocThongMinh(tu.trim(), phienAmChuan, nghia);
            ketQua = lamSachKetQua(ketQua);
        }

        cache.put(tuChuanHoa, ketQua);
        return ketQua;
    }

    private String chuanHoaIpaDauVao(String ipa) {
        if (ipa == null || ipa.isBlank()) return "";
        return ipa.replace("ʌɫ", "əl")
                .replace("əɫ", "əl")
                .replace("ɫ", "l")
                .replace("'", "ˈ")
                .replace("g", "ɡ")
                .replace("ɝ", "ɚ")
                .trim();
    }

    private HuongDanDocDTO lamSachKetQua(HuongDanDocDTO dto) {
        if (dto == null) return null;

        // 1. Chuẩn hóa IPA
        if (dto.getPhienAm() != null) {
            dto.setPhienAm(chuanHoaIpaDauVao(dto.getPhienAm()));
        }

        // 2. Chuẩn hóa chuỗi bồi tiếng Việt
        if (dto.getPhienAmTiengViet() != null) {
            String vn = dto.getPhienAmTiengViet()
                    .replace("uồl", "ờl")
                    .replace("UỒL", "ỜL")
                    .replace("uôl", "ờl")
                    .replace("UÔL", "ỜL");
            dto.setPhienAmTiengViet(vn);
        }

        // 3. Chuẩn hóa amTietBoi
        if (dto.getAmTietBoi() != null) {
            List<String> cleanedBoi = new ArrayList<>();
            for (String b : dto.getAmTietBoi()) {
                if (b != null) {
                    cleanedBoi.add(b.replace("uồl", "ờl")
                            .replace("UỒL", "ỜL")
                            .replace("uôl", "ờl")
                            .replace("UÔL", "ỜL"));
                }
            }
            dto.setAmTietBoi(cleanedBoi);
        }

        // 4. Chuẩn hóa amTietIpa
        if (dto.getAmTietIpa() != null) {
            List<String> cleanedIpa = new ArrayList<>();
            for (String p : dto.getAmTietIpa()) {
                cleanedIpa.add(p != null ? chuanHoaIpaDauVao(p).replaceAll("[/|\\[\\]]", "") : "");
            }
            dto.setAmTietIpa(cleanedIpa);
        }

        // 5. Chuẩn hóa amTiet (loại bỏ ngoặc đơn nếu có)
        if (dto.getAmTiet() != null) {
            List<String> cleanedEn = new ArrayList<>();
            for (String en : dto.getAmTiet()) {
                cleanedEn.add(en != null ? en.replaceAll("\\s*\\([^)]*\\)", "").trim() : "");
            }
            dto.setAmTiet(cleanedEn);
        }

        return dto;
    }

    private HuongDanDocDTO goiGeminiPhanTich(String tu, String phienAm, String nghia) throws Exception {
        String prompt = """
                Bạn là chuyên gia ngữ âm tiếng Anh hàng đầu cho người Việt Nam (giáo trình Cambridge / Oxford).
                Hãy phân tích chi tiết cách phát âm của từ tiếng Anh sau:
                - Từ: "%s"
                - Phiên âm IPA: "%s"
                - Nghĩa: "%s"

                YÊU CẦU BẮT BUỘC VỀ TÁCH ÂM TIẾT VÀ PHIÊN ÂM TIẾNG VIỆT (CHUẨN SƯ PHẠM BẢN XỨ):
                1. 'amTiet': Mảng các âm tiết tiếng Anh thực tế trong từ theo âm vị học (chỉ chứa chữ tiếng Anh sạch, KHÔNG kèm mở ngoặc), ví dụ:
                   - "annual" -> ["an", "nu", "al"]
                   - "usage" -> ["u", "sage"]
                   - "comfortable" -> ["com", "for", "ta", "ble"]
                   - "apple" -> ["ap", "ple"]
                   - "cabbage" -> ["cab", "bage"]
                2. 'amTietDoc': Mảng từ phát âm tiếng Anh để Web Speech API / TTS đọc đúng ngữ âm của âm đó khi đứng riêng lẻ (ví dụ:
                   - cho "annual": ["an", "you", "ull"] (vì nếu để "al" TTS sẽ đọc thành tên người 'Al', để "nu" TTS sẽ đọc là chữ cái 'N-U')
                   - cho "usage": ["you", "sidge"]
                   - cho "cabbage": ["cab", "bidge"]
                   - cho "lettuce": ["let", "tiss"]
                   - cho "apple": ["app", "pull"]
                3. 'amTietBoi': Mảng phiên âm bồi tiếng Việt tương ứng từng âm, ví dụ:
                   - "annual" -> ["AN", "niu", "ờl"]
                   - "usage" -> ["DIU", "sịch"]
                   - "comfortable" -> ["KĂM", "phơ", "tờ", "bồ"]
                   - "apple" -> ["ÉP", "pồ"]
                4. QUY TẮC PHIÊN ÂM TIẾNG VIỆT THÔNG MINH TRỰC QUAN (KẾT HỢP ÂM ĐUÔI RÕ RÀNG):
                   - CỰC KỲ QUAN TRỌNG: Các âm đuôi tiếng Anh (ending sounds) mà người Việt hay bỏ quên PHẢI được thể hiện rõ bằng dấu gạch nối '-' kèm ký hiệu âm đuôi chuẩn:
                     + Đuôi /s/: thêm '-x' (ví dụ: "face" -> "phây-x", "nice" -> "nái-x", "bus" -> "bót-x", "class" -> "clát-x", "price" -> "prái-x")
                     + Đuôi /z/: thêm '-z' (ví dụ: "nose" -> "nâu-z", "rose" -> "râu-z", "please" -> "plii-z", "use" -> "diu-z", "boys" -> "bôi-z", "is" -> "ít-z")
                     + Đuôi /θ/ hoặc /ð/: thêm '-th' (ví dụ: "mouth" -> "mao-th", "tooth" -> "tu-th", "teeth" -> "ti-th", "bath" -> "bát-th", "with" -> "wít-th")
                     + Đuôi /d/: thêm '-đ' (ví dụ: "hand" -> "hen-đ", "good" -> "gút-đ", "red" -> "rét-đ", "bad" -> "bét-đ", "friend" -> "phren-đ", "word" -> "quớt-đ")
                     + Đuôi /t/: dùng 't' hoặc '-t' (ví dụ: "head" -> "hét", "foot" -> "phút", "cat" -> "cát-t", "meet" -> "mít-t")
                     + Đuôi /k/ hoặc /g/: dùng 'c' hoặc '-k' (ví dụ: "leg" -> "léc", "back" -> "béc", "book" -> "búc-k", "like" -> "lái-k")
                     + Đuôi /tʃ/: thêm '-ch' (ví dụ: "watch" -> "oát-ch", "catch" -> "két-ch", "much" -> "mất-ch")
                     + Đuôi /dʒ/: bồi là "ịch" / "bích" hoặc thêm '-dzh' (ví dụ: "usage" -> "DIU - sịch", "cabbage" -> "KÉP - bích", "page" -> "phây-dzh", "orange" -> "o-rần-dzh")
                     + Đuôi /p/: dùng 'p' hoặc '-p' (ví dụ: "top" -> "tháp-p", "stop" -> "sờ-tóp")
                     + Đuôi "-al", "-el", "-le" và âm /əl/: Bồi là "ờl" hoặc "ồ" (ví dụ: "annual" -> "AN - niu - ờl", "apple" -> "ÉP - pồ", "table" -> "TÂY - bồ", "special" -> "SPÉ - sờl"). TUYỆT ĐỐI KHÔNG bồi là "uồl" hay "oát"!
                     + Vần "-ual", "-ua-", âm /ju/, /nju/: Bồi là "niu" hoặc "diu" (ví dụ: "annual" -> "AN - niu - ờl", "continue" -> "kần - TÍN - niu").
                     + Đuôi "-tion", "-sion", âm /ʃən/: Bồi là "sần".
                     + Đuôi "-ture", âm /tʃɚ/: Bồi là "chờ".
                     + Đuôi "-ous", âm /əs/: Bồi là "ợt-s" hoặc "ờ-s".
                     + Đuôi "-ble", âm /bəl/: Bồi là "bồ".
                   - Các từ nhiều âm tiết: nối các âm bằng dấu '-' (ví dụ: "finger" -> "phinh-gờ", "hello" -> "hê-lô", "water" -> "OÁ-tờ", "teacher" -> "TÍ-chờ").
                   - Âm tiết mang trọng âm chính: PHẢI VIẾT IN HOA trong chuỗi nhiều âm (ví dụ: "PHINH - gờ", "AN - niu - ờl", "DIU - sịch", "KĂM - phơ - tờ - bồ", "ÉP - pồ"). Với từ 1 âm tiết, viết chữ thường có dấu trực quan (ví dụ: "phây-x", "nâu-z", "mao-th", "tu-th", "hen-đ", "hét", "léc", "phút", "béc").
                5. 'amTietIpa': Mảng phiên âm IPA từng âm tiết chuẩn quốc tế Oxford/Cambridge (KHÔNG dùng ký hiệu Arpabet như ɫ), ví dụ cho "annual": ["ˈæn", "ju", "əl"].
                6. 'phienAmTiengViet': Chuỗi nối các âm tiết bồi bằng dấu gạch ngang '-', ví dụ: "AN - niu - ờl", "DIU - sịch", "KĂM - phơ - tờ - bồ", "ÉP - pồ".
                7. 'amNhanIndex': Chỉ số âm tiết mang trọng âm chính (0-indexed).
                8. 'trongAm': Lời giải thích ngắn gọn âm nào cần nhấn mạnh.
                9. 'khauHinh': Hướng dẫn mở miệng, đặt lưỡi.
                10. 'amDuoi': Nhắc nhở âm đuôi quan trọng (như /dʒ/, /s/, /t/, /d/, /k/...).
                11. 'loiThuongGap': Lỗi người Việt thường phát âm sai ở từ này.
                12. 'meoGhiNho': Mẹo nhớ vui và trực quan.

                CHỈ TRẢ VỀ DUY NHẤT 1 ĐỐI TƯỢNG JSON (KHÔNG KÈM MARKDOWN, KHÔNG GIẢI THÍCH):
                {
                  "tu": "%s",
                  "phienAm": "%s",
                  "nghia": "%s",
                  "amTiet": ["an", "nu", "al"],
                  "amTietDoc": ["an", "you", "ull"],
                  "amTietBoi": ["AN", "niu", "ờl"],
                  "amTietIpa": ["ˈæn", "ju", "əl"],
                  "amNhanIndex": 0,
                  "phienAmTiengViet": "AN - niu - ờl",
                  "trongAm": "Nhấn mạnh vào âm tiết thứ nhất (AN)...",
                  "khauHinh": "...",
                  "amDuoi": "...",
                  "loiThuongGap": "...",
                  "meoGhiNho": "..."
                }
                """.formatted(tu, phienAm != null ? phienAm : "", nghia != null ? nghia : "",
                tu, phienAm != null ? phienAm : "", nghia != null ? nghia : "");

        String jsonResponse = geminiService.taoPhanTichHuongDanDoc(prompt);
        if (jsonResponse != null && !jsonResponse.isBlank()) {
            String cleanJson = jsonResponse.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleanJson, HuongDanDocDTO.class);
        }
        return null;
    }

    private HuongDanDocDTO layMauCoSan(String tu, String phienAm, String nghia) {
        HuongDanDocDTO mau = TU_DIEN_BOI_CHUAN.get(tu.toLowerCase().trim());
        if (mau == null) return null;
        return new HuongDanDocDTO(
                tu,
                phienAm != null && !phienAm.isBlank() ? phienAm : mau.getPhienAm(),
                nghia != null && !nghia.isBlank() ? nghia : mau.getNghia(),
                mau.getAmTiet(),
                mau.getAmTietIpa(),
                mau.getAmTietBoi(),
                mau.getAmTietDoc(),
                mau.getAmNhanIndex(),
                mau.getPhienAmTiengViet(),
                mau.getTrongAm(),
                mau.getKhauHinh(),
                mau.getAmDuoi(),
                mau.getLoiThuongGap(),
                mau.getMeoGhiNho()
        );
    }

    // =========================================================================
    // TỪ ĐIỂN NGỮ ÂM CHUẨN XÁC DÀNH CHO CÁC TỪ THƯỜNG GẶP
    // =========================================================================
    private static final Map<String, HuongDanDocDTO> TU_DIEN_BOI_CHUAN = new HashMap<>();

    static {
        // --- USAGE ---
        TU_DIEN_BOI_CHUAN.put("usage", new HuongDanDocDTO(
                "usage", "/ˈjuː.sɪdʒ/", "cách sử dụng, sự dùng",
                List.of("u", "sage"),
                List.of("juː", "sɪdʒ"),
                List.of("DIU", "sịch"),
                List.of("you", "sidge"),
                0,
                "DIU - sịch",
                "Trọng âm rơi vào âm 1 (DIU) -> đọc to, cao, kéo dài: DIU-sịch.",
                "Khép môi nhẹ đẩy hơi đọc âm 'd' kết hợp 'iu' (/juː/), sau đó chu tròn môi bật nhẹ âm cuối /dʒ/ (sịch).",
                "⚠️ Âm đuôi /dʒ/ (cuối từ): chu môi bật dứt khoát như 'ch' có rung thanh quản (xịt-ch / sịch).",
                "Người Việt hay đọc nhầm theo mặt chữ thành 'u-sa-ge' hoặc 'diu-sếch'. Cách đọc chuẩn là DIU-sịch!",
                "Mẹo nhớ: Hướng dẫn sử dụng đồ công nghệ thì phải 'DIU' (Dịu) dàng kẻo hỏng 'SỊCH'!"
        ));

        TU_DIEN_BOI_CHUAN.put("use", new HuongDanDocDTO(
                "use", "/juːz/", "sử dụng, dùng",
                List.of("use"),
                List.of("juːz"),
                List.of("DIU-z"),
                List.of("use"),
                0,
                "DIU - z",
                "Từ có 1 âm tiết, phát âm /juː/ kéo dài rồi kết thúc bằng rung âm /z/.",
                "Đẩy hơi /j/ kết hợp tròn môi /uː/, đầu lưỡi gần chân răng rung thanh quản âm /z/.",
                "⚠️ Đuôi /z/ (khi là động từ) phải rung nhẹ ở cổ họng.",
                "Tránh đọc cụt thành 'dút' hay 'u-sờ'.",
                "Mẹo nhớ: 'DIU' dàng sử dụng đồ dùng."
        ));

        TU_DIEN_BOI_CHUAN.put("user", new HuongDanDocDTO(
                "user", "/ˈjuː.zɚ/", "người dùng",
                List.of("u", "ser"),
                List.of("juː", "zɚ"),
                List.of("DIU", "zờ"),
                List.of("you", "zer"),
                0,
                "DIU - zờ",
                "Trọng âm rơi vào âm 1 (DIU).",
                "Phát âm /juː/ nhấn mạnh, âm sau /zɚ/ lướt nhẹ cong lưỡi.",
                "Âm /z/ ở giữa từ rung nhẹ.",
                "Tránh đọc thành 'u-xơ' hay 'út-xơ'.",
                "Mẹo nhớ: Người dùng 'DIU' (dịu) dàng 'ZỜ' (sờ) vào màn hình."
        ));

        TU_DIEN_BOI_CHUAN.put("useful", new HuongDanDocDTO(
                "useful", "/ˈjuːs.fəl/", "hữu ích, bổ ích",
                List.of("use", "ful"),
                List.of("juːs", "fəl"),
                List.of("DIU-s", "phun"),
                List.of("yoos", "full"),
                0,
                "DIU-s - phun",
                "Trọng âm rơi vào âm 1 (DIU-s).",
                "Đọc /juː/ rồi xì nhẹ /s/, sau đó răng trên chạm môi dưới đọc /fəl/.",
                "⚠️ Xì nhẹ âm /s/ ở giữa từ: 'DIU-s... phun'.",
                "Tránh bỏ quên âm /s/ ở giữa thành 'diu-phun'.",
                "Mẹo nhớ: Từ điển này 'DIU-S' (dịu sắc) và 'PHUN' (full) điều hữu ích!"
        ));

        // --- HELLO / HI ---
        TU_DIEN_BOI_CHUAN.put("hello", new HuongDanDocDTO(
                "hello", "/həˈloʊ/", "xin chào",
                List.of("hel", "lo"),
                List.of("hə", "loʊ"),
                List.of("hê", "LÔ"),
                List.of("heh", "low"),
                1,
                "Hê - LÔ",
                "Trọng âm rơi vào âm thứ hai LÔ -> đọc âm LÔ to, cao và ngân dài hơn (hoặc hơ-LÔU).",
                "Bật hơi nhẹ âm /h/, kết thúc chu tròn môi âm /oʊ/.",
                "Âm đuôi /oʊ/ cần chu môi tròn nhẹ ở cuối âm.",
                "Tránh đọc phẳng 'hê-lô' không ngữ điệu.",
                "Mẹo nhớ: Chào bạn 'Hê' ơi, cùng chơi 'LÔ' tô nào!"
        ));

        TU_DIEN_BOI_CHUAN.put("hi", new HuongDanDocDTO(
                "hi", "/haɪ/", "xin chào",
                List.of("hi"),
                List.of("haɪ"),
                List.of("HAI"),
                List.of("hi"),
                0,
                "HAI",
                "Từ 1 âm tiết, đọc rõ nguyên âm đôi /aɪ/ kéo từ 'a' sang 'i'.",
                "Đẩy hơi /h/ nhẹ, mở miệng 'a' rồi thu về 'i'.",
                "Không có phụ âm đuôi.",
                "Tránh nuốt âm /h/ thành 'ai'.",
                "Mẹo nhớ: Giơ 2 ngón tay chào 'HAI'."
        ));

        // --- APPLE / APPLES ---
        TU_DIEN_BOI_CHUAN.put("apple", new HuongDanDocDTO(
                "apple", "/ˈæp.əl/", "quả táo",
                List.of("ap", "ple"),
                List.of("æp", "əl"),
                List.of("ÉP", "pồ"),
                List.of("app", "pull"),
                0,
                "ÉP - pồ",
                "Trọng âm rơi vào âm đầu (ÉP).",
                "Mở to miệng đọc /æ/ (lai e và a), ngậm môi bật /p/, đầu lưỡi chạm chân răng đọc âm /l/ nhẹ.",
                "Âm cuối uốn nhẹ lưỡi /l/.",
                "Tránh đọc thô cứng thành 'áp-pồ', hãy đọc âm /æ/ mềm: 'ÉP-pồ'.",
                "Mẹo nhớ: 'ÉP' quả táo lấy nước mời bạn 'PỒ'."
        ));

        TU_DIEN_BOI_CHUAN.put("apples", new HuongDanDocDTO(
                "apples", "/ˈæp.əlz/", "những quả táo",
                List.of("ap", "ples"),
                List.of("æp", "əlz"),
                List.of("ÉP", "pồ-z"),
                List.of("app", "pulls"),
                0,
                "ÉP - pồ - z",
                "Trọng âm rơi vào âm 1 (ÉP).",
                "Mở miệng đọc /æp/, uốn lưỡi /əl/ và rung nhẹ âm /z/ ở cuối.",
                "⚠️ Đuôi /z/ rung nhẹ ở thanh quản vì sau âm /l/.",
                "Hay quên âm đuôi /z/ số nhiều.",
                "Mẹo nhớ: 'ÉP' nhiều quả táo cho 'PỒ' cười 'Z'ui vẻ!"
        ));

        // --- VEGETABLES ---
        TU_DIEN_BOI_CHUAN.put("cabbage", new HuongDanDocDTO(
                "cabbage", "/ˈkæb.ɪdʒ/", "bắp cải",
                List.of("cab", "bage"),
                List.of("kæb", "ɪdʒ"),
                List.of("KÉP", "bích"),
                List.of("cab", "bidge"),
                0,
                "KÉP - bích",
                "Trọng âm rơi vào âm 1 (KÉP).",
                "Bật hơi /k/ ở họng, mở miệng /æ/, ngậm môi /b/ rồi chu môi bật âm /dʒ/ (bích / bít-ch).",
                "⚠️ Âm đuôi /dʒ/ chu nhẹ môi và bật hơi dứt khoát.",
                "Hay bị đọc sai theo chữ viết thành 'cáp-ba-ge'.",
                "Mẹo nhớ: Bắp cải to quá phải 'KÉP' lại cho vào 'BÍCH' (bịch) mang về."
        ));

        TU_DIEN_BOI_CHUAN.put("garlic", new HuongDanDocDTO(
                "garlic", "/ˈɡɑːr.lɪk/", "củ tỏi",
                List.of("gar", "lic"),
                List.of("ɡɑːr", "lɪk"),
                List.of("GAA", "lịk"),
                List.of("gar", "lick"),
                0,
                "GAA - lịk",
                "Trọng âm rơi vào âm 1 (GAA) -> đọc to, trầm và ngân dài.",
                "Mở vòm họng tròn sâu phát âm /ɑː/, cong nhẹ lưỡi, chuyển sang âm /lɪk/ kết thúc bằng ngắt hơi /k/.",
                "⚠️ Âm đuôi /k/ ngắt hơi sắc ở cuống họng.",
                "Tránh đọc thành 'gát-lích' phẳng giọng.",
                "Mẹo nhớ: Ăn củ tỏi cay quá kêu 'GAA' lên, mắt 'LỊK' đi vì cay!"
        ));

        TU_DIEN_BOI_CHUAN.put("lemon", new HuongDanDocDTO(
                "lemon", "/ˈlem.ən/", "quả chanh vàng",
                List.of("le", "mon"),
                List.of("lem", "ən"),
                List.of("LE", "mần"),
                List.of("lem", "uhn"),
                0,
                "LE - mần",
                "Trọng âm rơi vào âm 1 (LE).",
                "Môi bẹt sang hai bên phát âm /e/, khép môi /m/ rồi thả lỏng lưỡi đọc /ən/.",
                "Âm kết thúc bằng /n/ nhẹ ở vòm miệng.",
                "Người Việt hay đọc nhầm thành 'le-mon' (âm o) thay vì âm /ə/ (mần).",
                "Mẹo nhớ: Quả chanh vắt vào 'LE' lưỡi vì chua, ăn 'MẦN' chi nữa!"
        ));

        TU_DIEN_BOI_CHUAN.put("lettuce", new HuongDanDocDTO(
                "lettuce", "/ˈlet̬.ɪs/", "rau xà lách",
                List.of("let", "tuce"),
                List.of("let", "ɪs"),
                List.of("LE", "tịt-s"),
                List.of("let", "tiss"),
                0,
                "LE - tịt - s",
                "Trọng âm rơi vào âm 1 (LE).",
                "Đầu lưỡi chạm nướu trên bật âm /t/, sau đó xì nhẹ hơi /s/ ở cuối.",
                "⚠️ Bắt buộc phải xì nhẹ âm /s/ ở cuối từ (tịt-s).",
                "Hay bị đọc sai thành 'lét-tu-xê' hoặc quên âm xì /s/ cuối.",
                "Mẹo nhớ: Ăn rau xà lách 'LE' lưỡi vì 'TỊT' ngòi!"
        ));

        TU_DIEN_BOI_CHUAN.put("beans", new HuongDanDocDTO(
                "beans", "/biːnz/", "các loại hạt đậu",
                List.of("beans"),
                List.of("biːnz"),
                List.of("BIIN-z"),
                List.of("beans"),
                0,
                "BIIN - z",
                "Từ có 1 âm tiết, nguyên âm /iː/ kéo dài hơn bình thường.",
                "Khép hai môi bật /b/, kéo căng khóe miệng cười để đọc /iː/, kết thúc rung nhẹ âm /z/.",
                "⚠️ Đuôi /z/ rung nhẹ, không được bỏ quên.",
                "Tránh đọc cộc lốc thành 'bin', phải đọc kéo dài 'biiin-z'.",
                "Mẹo nhớ: Hạt đậu thần của bác 'BIIN' (Bean) cười 'Z'ui vẻ!"
        ));

        TU_DIEN_BOI_CHUAN.put("bean", new HuongDanDocDTO(
                "bean", "/biːn/", "hạt đậu",
                List.of("bean"),
                List.of("biːn"),
                List.of("BIIN"),
                List.of("bean"),
                0,
                "BIIN",
                "Nguyên âm /iː/ dài, đọc kéo dài giọng 'biiin'.",
                "Khép môi bật /b/, căng khóe miệng đọc /iː/ rồi kết thúc bằng /n/.",
                "Âm đuôi /n/ ngân nhẹ ở khoang mũi.",
                "Tránh đọc cụt ngủn thành 'bin'.",
                "Mẹo nhớ: Hạt đậu của Mr. 'BIIN'."
        ));

        TU_DIEN_BOI_CHUAN.put("comfortable", new HuongDanDocDTO(
                "comfortable", "/ˈkʌm.fɚ.t̬ə.bəl/", "thoải mái, tiện nghi",
                List.of("com", "for", "ta", "ble"),
                List.of("kʌm", "fɚ", "t̬ə", "bəl"),
                List.of("KĂM", "phơ", "tờ", "bồ"),
                List.of("come", "fer", "tuh", "bull"),
                0,
                "KĂM - phơ - tờ - bồ",
                "Trọng âm rơi vào âm 1 (KĂM), các âm sau đọc lướt nhanh và nhẹ.",
                "Bật hơi /k/ ở họng, răng trên chạm môi dưới phát âm /f/, kết thúc bằng âm /bəl/ nhẹ.",
                "Âm kết thúc /bəl/ phát âm lướt nhẹ, không nhấn.",
                "Lỗi rất phổ biến: Đọc 4 âm riêng 'côm-pho-tây-bồ' thay vì chuẩn 'KĂM-phơ-tờ-bồ' (hoặc KĂM-tờ-bồ).",
                "Mẹo nhớ: 'KĂM' túi nước xả 'PHƠ' (Comfort) nằm thật thoải mái 'TỜ - BỒ'!"
        ));

        TU_DIEN_BOI_CHUAN.put("beautiful", new HuongDanDocDTO(
                "beautiful", "/ˈbjuː.t̬ə.fəl/", "xinh đẹp",
                List.of("beau", "ti", "ful"),
                List.of("bjuː", "t̬ə", "fəl"),
                List.of("BIU", "ti", "phun"),
                List.of("byoo", "tee", "full"),
                0,
                "BIU - ti - phun",
                "Trọng âm rơi vào âm đầu tiên (BIU).",
                "Khép môi bật /b/, chu môi đọc âm dài /juː/, âm /t/ đọc nhẹ, đuôi /fəl/ đọc như 'phun'.",
                "Đuôi /l/ uốn nhẹ đầu lưỡi chạm chân răng trên.",
                "Tránh đọc 'beo-ti-phun' theo chữ viết.",
                "Mẹo nhớ: 'BIU' (View) cảnh biển này 'TI'ệt đẹp, quá 'PHUN' (Full) sắc màu!"
        ));

        TU_DIEN_BOI_CHUAN.put("important", new HuongDanDocDTO(
                "important", "/ɪmˈpɔːr.tənt/", "quan trọng",
                List.of("im", "por", "tant"),
                List.of("ɪm", "pɔːr", "tənt"),
                List.of("im", "PO", "tần-t"),
                List.of("im", "por", "tent"),
                1,
                "im - PO - tần - t",
                "Trọng âm rơi vào âm 2 (PO) -> đọc to, cao và tròn môi.",
                "Đọc lướt /ɪm/, bật mạnh /pɔːr/ rồi kết thúc dứt khoát bằng /tənt/.",
                "⚠️ Đuôi /t/ cuối từ bật hơi dứt khoát.",
                "Hay bị đọc phẳng không nhấn vào âm 'PO'.",
                "Mẹo nhớ: Việc quan trọng là ăn 'PO' (Phở) cho 'TẦN' (tận) tụy."
        ));

        TU_DIEN_BOI_CHUAN.put("information", new HuongDanDocDTO(
                "information", "/ˌɪn.fɚˈmeɪ.ʃən/", "thông tin",
                List.of("in", "for", "ma", "tion"),
                List.of("ɪn", "fɚ", "meɪ", "ʃən"),
                List.of("in", "phơ", "MÂY", "sần"),
                List.of("in", "fer", "may", "shun"),
                2,
                "in - phơ - MÂY - sần",
                "Trọng âm chính rơi vào âm 3 (MÂY) -> đọc cao nhất: in-phơ-MÂY-sần.",
                "Đọc /meɪ/ kéo từ e sang i, âm đuôi /ʃən/ chu môi xì hơi dứt khoát.",
                "⚠️ Đuôi /ʃən/ chu môi tạo âm /ʃ/ dày hơi.",
                "Người Việt hay đọc thành 'in-pho-mây-sơn'.",
                "Mẹo nhớ: Tìm thông tin trên tầng 'MÂY' (Cloud) thật 'SẦN' sùi."
        ));

        // --- ANNUAL / ANNUALLY ---
        TU_DIEN_BOI_CHUAN.put("annual", new HuongDanDocDTO(
                "annual", "/ˈæn.ju.əl/", "hàng năm, thường niên",
                List.of("an", "nu", "al"),
                List.of("ˈæn", "ju", "əl"),
                List.of("AN", "niu", "ờl"),
                List.of("an", "you", "ull"),
                0,
                "AN - niu - ờl",
                "Trọng âm chính rơi vào âm 1 (AN) -> đọc to, cao và rõ: AN-niu-ờl.",
                "Mở rộng khóe miệng đọc /æn/, chuyển mượt sang /ju/ (niu), kết thúc thả lỏng môi uốn nhẹ đầu lưỡi chạm chân răng trên /əl/ (ờl).",
                "⚠️ Âm đuôi /əl/: kết thúc bằng uốn nhẹ đầu lưỡi chạm nướu trên (ờ-l / ờl), không nuốt âm.",
                "Người Việt hay đọc theo mặt chữ thành 'an-nu-an' hoặc 'uồl'. Cách đọc chuẩn xác Cambridge là: AN-niu-ờl!",
                "Mẹo nhớ: Sự kiện hàng năm 'AN' tâm tổ chức, 'NIU' (nhiều) người đợi 'ỜL' (chờ)!"
        ));

        TU_DIEN_BOI_CHUAN.put("annually", new HuongDanDocDTO(
                "annually", "/ˈæn.ju.ə.li/", "hàng năm, mỗi năm một lần",
                List.of("an", "nu", "al", "ly"),
                List.of("ˈæn", "ju", "əl", "li"),
                List.of("AN", "niu", "ờ", "li"),
                List.of("an", "you", "uh", "lee"),
                0,
                "AN - niu - ờ - li",
                "Trọng âm chính rơi vào âm 1 (AN).",
                "Phát âm /æn/ dứt khoát, các âm sau lướt nhẹ mềm mại.",
                "Đuôi /li/ đọc nhẹ nhàng, tươi sáng.",
                "Tránh đọc phẳng hoặc nhấn sai trọng âm.",
                "Mẹo nhớ: 'AN' tâm báo cáo hàng năm 'NIU' việc 'Ờ' 'LI' liềm!"
        ));

        // --- SCHEDULE ---
        TU_DIEN_BOI_CHUAN.put("schedule", new HuongDanDocDTO(
                "schedule", "/ˈskedʒ.uːl/", "lịch trình, thời gian biểu",
                List.of("sched", "ule"),
                List.of("ˈskedʒ", "uːl"),
                List.of("SKÉ", "diun"),
                List.of("skedj", "oole"),
                0,
                "SKÉ - diun",
                "Trọng âm rơi vào âm 1 (SKÉ).",
                "Xì nhẹ /s/ rồi bật /k/ và /e/, âm /dʒ/ nối sang /uːl/ thành 'diun' (hoặc Anh-Anh: 'sê-điu').",
                "⚠️ Đuôi /l/ uốn nhẹ lưỡi ở cuối âm.",
                "Tránh đọc nhầm theo chữ viết thành 's-che-đu-le'.",
                "Mẹo nhớ: Lên lịch trình thì 'SKÉ' (khe) thời gian cho 'DIUN' (dễ) làm việc!"
        ));

        // --- EXPERIENCE ---
        TU_DIEN_BOI_CHUAN.put("experience", new HuongDanDocDTO(
                "experience", "/ɪkˈspɪr.i.əns/", "kinh nghiệm, trải nghiệm",
                List.of("ex", "pe", "ri", "ence"),
                List.of("ɪk", "ˈspɪr", "i", "əns"),
                List.of("ik", "SPÍ", "ri", "ần-s"),
                List.of("ik", "speer", "ee", "ens"),
                1,
                "ik - SPÍ - ri - ần-s",
                "Trọng âm chính rơi vào âm 2 (SPÍ) -> đọc cao giọng và nhấn rõ.",
                "Đọc lướt /ɪk/, bật mạnh /spɪr/ rồi xì nhẹ /s/ ở đuôi /əns/.",
                "⚠️ Đuôi /s/ cuối từ bắt buộc phải xì hơi nhẹ qua kẽ răng.",
                "Người Việt hay quên âm xì /s/ cuối hoặc đọc sai thành 'ech-pe-ri-en'.",
                "Mẹo nhớ: Muốn có kinh nghiệm thì 'IK' (ít) nhất phải 'SPÍ' (phi) vào thực tế!"
        ));

        // --- OPPORTUNITY ---
        TU_DIEN_BOI_CHUAN.put("opportunity", new HuongDanDocDTO(
                "opportunity", "/ˌɑː.pɚˈtuː.nə.t̬i/", "cơ hội",
                List.of("op", "por", "tu", "ni", "ty"),
                List.of("ˌɑː", "pɚ", "ˈtuː", "nə", "t̬i"),
                List.of("o", "pơ", "TIU", "nơ", "ti"),
                List.of("op", "per", "too", "nuh", "tee"),
                2,
                "o - pơ - TIU - nơ - ti",
                "Trọng âm chính rơi vào âm 3 (TIU / TU) -> ngân cao và rõ nhất.",
                "Mở miệng đọc /ɑː/, lướt nhẹ /pɚ/, nhấn mạnh /tuː/ và lướt hai âm cuối.",
                "Đuôi /ti/ đọc nhẹ, dứt khoát.",
                "Tránh đọc bằng phẳng cả 5 âm tiết.",
                "Mẹo nhớ: Nắm bắt cơ hội để bay lên 'TIU' (tít) trên cao!"
        ));

        // --- CANDIDATE ---
        TU_DIEN_BOI_CHUAN.put("candidate", new HuongDanDocDTO(
                "candidate", "/ˈkæn.dɪ.dət/", "ứng viên, thí sinh",
                List.of("can", "di", "date"),
                List.of("ˈkæn", "dɪ", "dət"),
                List.of("KÉN", "đi", "đợt"),
                List.of("can", "dih", "duht"),
                0,
                "KÉN - đi - đợt",
                "Trọng âm rơi vào âm 1 (KÉN).",
                "Bật hơi /k/ mở miệng /æ/, âm sau đọc nhẹ lướt /dət/.",
                "⚠️ Đuôi /t/ bật nhẹ ở đầu lưỡi.",
                "Tránh đọc 'can-đi-đết' theo mặt chữ viết.",
                "Mẹo nhớ: Tuyển chọn ứng viên rất 'KÉN' chọn từng 'ĐỢT'!"
        ));

        // --- CONFERENCE ---
        TU_DIEN_BOI_CHUAN.put("conference", new HuongDanDocDTO(
                "conference", "/ˈkɑːn.fɚ.əns/", "hội nghị",
                List.of("con", "fer", "ence"),
                List.of("ˈkɑːn", "fɚ", "əns"),
                List.of("KOON", "phơ", "rần-s"),
                List.of("con", "fer", "ens"),
                0,
                "KOON - phơ - rần-s",
                "Trọng âm rơi vào âm 1 (KOON).",
                "Tròn miệng mở âm /ɑː/, răng trên chạm môi dưới /f/, xì nhẹ /s/ ở đuôi.",
                "⚠️ Đuôi /s/ xì nhẹ luồng hơi qua kẽ răng.",
                "Tránh đọc thành 'côn-phơ-ren' thiếu âm gió.",
                "Mẹo nhớ: Đi hội nghị 'KOON' (khôn) ngoan học hỏi nhiều điều!"
        ));

        // --- EQUIPMENT ---
        TU_DIEN_BOI_CHUAN.put("equipment", new HuongDanDocDTO(
                "equipment", "/ɪˈkwɪp.mənt/", "trang thiết bị, dụng cụ",
                List.of("e", "quip", "ment"),
                List.of("ɪ", "ˈkwɪp", "mənt"),
                List.of("i", "QUÍP", "mừn-t"),
                List.of("ih", "quip", "muhnt"),
                1,
                "i - QUÍP - mừn-t",
                "Trọng âm chính rơi vào âm 2 (QUÍP) -> nhấn mạnh rõ.",
                "Lướt /ɪ/, tròn môi bật /kw/ sang /ɪp/, kết thúc chặn lưỡi /nt/.",
                "⚠️ Đuôi /t/ ngắt dứt khoát.",
                "Tránh đọc 'e-quíp-mơn' không trọng âm.",
                "Mẹo nhớ: Trang bị thiết bị hiện đại để 'QUÍP' (kịp) tiến độ!"
        ));

        // --- REGISTER ---
        TU_DIEN_BOI_CHUAN.put("register", new HuongDanDocDTO(
                "register", "/ˈredʒ.ə.stɚ/", "đăng ký, ghi danh",
                List.of("reg", "is", "ter"),
                List.of("ˈredʒ", "ə", "stɚ"),
                List.of("RÉ", "dơ", "stơ"),
                List.of("redj", "uh", "ster"),
                0,
                "RÉ - dơ - stơ",
                "Trọng âm rơi vào âm 1 (RÉ).",
                "Bật âm /dʒ/ chu môi nhẹ rồi nối sang /stɚ/ cong nhẹ lưỡi.",
                "⚠️ Chú ý âm /st/ ở âm tiết cuối.",
                "Tránh đọc 're-gít-tơ' theo mặt chữ.",
                "Mẹo nhớ: Đăng ký tài khoản 'RÉ' (réo) gọi mọi người cùng vào!"
        ));

        // --- CÁC TỪ VỰNG BỘ PHẬN CƠ THỂ (CHUẨN THEO ẢNH MẪU TRỰC QUAN) ---
        TU_DIEN_BOI_CHUAN.put("head", new HuongDanDocDTO(
                "head", "/hed/", "đầu, cái đầu",
                List.of("head"),
                List.of("hed"),
                List.of("hét"),
                List.of("head"),
                0,
                "hét",
                "Từ 1 âm tiết, phát âm /e/ ngắn dứt khoát rồi chặn hơi ở nướu trên /d/ thành 'hét'.",
                "Mở miệng vừa phải phát âm /e/, đầu lưỡi nâng lên chạm nướu trên chặn hơi âm /d/ dứt khoát.",
                "⚠️ Đuôi /d/ chặn hơi ở nướu răng trên, phát âm dứt khoát như 'hét'.",
                "Tránh đọc kéo dài hay đọc thành 'héc'.",
                "Mẹo nhớ: Suy nghĩ nhiều đau 'HÉT' cả đầu!"
        ));

        TU_DIEN_BOI_CHUAN.put("hair", new HuongDanDocDTO(
                "hair", "/heər/", "tóc, mái tóc",
                List.of("hair"),
                List.of("heə"),
                List.of("he"),
                List.of("hair"),
                0,
                "he",
                "Từ 1 âm tiết, phát âm /e/ rồi thu họng cong nhẹ lưỡi /ə(r)/, đọc tự nhiên là 'he' hoặc 'he-ờ'.",
                "Mở miệng phát âm /e/, sau đó hơi thu họng lại cong nhẹ đầu lưỡi.",
                "Âm đuôi /r/ uốn nhẹ đầu lưỡi ở cuối âm.",
                "Tránh đọc thành 'hai-ơ' theo mặt chữ viết.",
                "Mẹo nhớ: Mái tóc bồng bềnh đón 'HE' (hè) sang!"
        ));

        TU_DIEN_BOI_CHUAN.put("face", new HuongDanDocDTO(
                "face", "/feɪs/", "khuôn mặt, mặt",
                List.of("face"),
                List.of("feɪs"),
                List.of("phây-x"),
                List.of("face"),
                0,
                "phây-x",
                "Từ 1 âm tiết, trượt nguyên âm đôi /eɪ/ rồi BẮT BUỘC xì hơi gió /s/ (-x) ở cuối.",
                "Răng trên chạm nhẹ môi dưới thổi /f/, trượt sang /eɪ/ (phây), hai hàm răng khép hờ xì hơi gió /s/ (-x).",
                "⚠️ BẮT BUỘC xì hơi gió /s/ (-x) ở cuối từ: 'phây-x', tuyệt đối không nuốt âm!",
                "Người Việt rất hay bỏ quên âm gió /s/ cuối thành 'phây' (như lướt 'phây'). Phải đọc rõ: 'phây-x'!",
                "Mẹo nhớ: Khuôn mặt 'PHÂY-X' (phây phây) trắng hồng xinh xắn!"
        ));

        TU_DIEN_BOI_CHUAN.put("eye", new HuongDanDocDTO(
                "eye", "/aɪ/", "mắt, con mắt",
                List.of("eye"),
                List.of("aɪ"),
                List.of("ai"),
                List.of("eye"),
                0,
                "ai",
                "Từ 1 âm tiết, nguyên âm đôi /aɪ/ đọc liền mạch trượt từ 'a' sang 'i' (ai).",
                "Mở miệng rộng phát âm /a/ rồi thu hẹp khóe miệng trượt về /i/.",
                "Không có phụ âm đuôi, ngân nhẹ âm /i/.",
                "Tránh đọc cộc lốc hoặc gắt giọng.",
                "Mẹo nhớ: Đôi mắt sáng nhìn 'AI' cũng thấy mến!"
        ));

        TU_DIEN_BOI_CHUAN.put("ear", new HuongDanDocDTO(
                "ear", "/ɪər/", "tai, lỗ tai",
                List.of("ear"),
                List.of("ɪə"),
                List.of("ia"),
                List.of("ear"),
                0,
                "ia",
                "Từ 1 âm tiết, trượt từ /ɪ/ sang /ə/ rồi cong nhẹ đầu lưỡi (đọc như 'ia' hoặc 'i-ờ').",
                "Môi bẹt đọc /ɪ/ rồi thả lỏng trượt về /ə/, uốn nhẹ đầu lưỡi.",
                "Âm /r/ cuối từ uốn nhẹ đầu lưỡi.",
                "Tránh đọc thành 'e-a' theo mặt chữ.",
                "Mẹo nhớ: Vểnh 'TAI' lắng nghe tiếng 'IA' (kia) vọng lại!"
        ));

        TU_DIEN_BOI_CHUAN.put("nose", new HuongDanDocDTO(
                "nose", "/noʊz/", "mũi, cái mũi",
                List.of("nose"),
                List.of("noʊz"),
                List.of("nâu-z"),
                List.of("nose"),
                0,
                "nâu-z",
                "Từ 1 âm tiết, đọc /noʊ/ (nâu) kết thúc bằng âm rung thanh quản /z/ (-z).",
                "Đầu lưỡi chạm nướu trên đọc /n/, chu môi đọc /oʊ/ (nâu), hai răng khép hờ rung thanh quản âm /z/ (-z).",
                "⚠️ Đuôi /z/ (-z) là âm rung thanh quản, khác với âm gió /s/. Đặt tay lên cổ họng sẽ cảm nhận độ rung!",
                "Hay đọc nhầm thành 'nốt' hoặc nuốt âm thành 'nâu'. Phải đọc chuẩn: 'nâu-z'!",
                "Mẹo nhớ: Chiếc mũi có màu 'NÂU', thở ra kêu 'Z'zz!"
        ));

        TU_DIEN_BOI_CHUAN.put("mouth", new HuongDanDocDTO(
                "mouth", "/maʊθ/", "miệng, cái miệng",
                List.of("mouth"),
                List.of("maʊθ"),
                List.of("mao-th"),
                List.of("mouth"),
                0,
                "mao-th",
                "Từ 1 âm tiết, đọc /maʊ/ (mao) rồi đưa đầu lưỡi ra giữa 2 hàm răng thổi hơi /θ/ (-th).",
                "Hai môi mở phát âm /maʊ/ (mao), sau đó đưa đầu lưỡi kẹp nhẹ giữa 2 hàm răng, thổi luồng hơi nhẹ (-th).",
                "⚠️ Âm đuôi /θ/ (-th): Đầu lưỡi kẹp nhẹ giữa răng trên và răng dưới rồi thổi luồng hơi gió.",
                "Rất nhiều người đọc sai thành 'mao-s' hoặc 'mát'. Hãy thè nhẹ đầu lưỡi thổi hơi: 'mao-th'!",
                "Mẹo nhớ: Cái miệng 'MAO' (mau) mồm mau 'TH' (thì) nói nhiều!"
        ));

        TU_DIEN_BOI_CHUAN.put("tooth", new HuongDanDocDTO(
                "tooth", "/tuːθ/", "răng, một chiếc răng",
                List.of("tooth"),
                List.of("tuːθ"),
                List.of("tu-th"),
                List.of("tooth"),
                0,
                "tu-th",
                "Từ 1 âm tiết, chu tròn môi đọc âm dài /tuː/ (tu) rồi thè nhẹ đầu lưỡi thổi hơi /θ/ (-th).",
                "Đầu lưỡi chạm nướu bật /t/, chu môi kéo dài /uː/, đưa đầu lưỡi ra giữa 2 hàm răng thổi hơi /θ/ (-th).",
                "⚠️ Âm đuôi /θ/ (-th): Đặt đầu lưỡi giữa răng cửa trên và dưới, thổi luồng hơi nhẹ ra.",
                "Không đọc thành 'tút' hay 'tút-s', đuôi phải là âm thè lưỡi /θ/ (-th).",
                "Mẹo nhớ: Đau 'TU' (tút) chiếc răng số 'TH' (tám)!"
        ));

        TU_DIEN_BOI_CHUAN.put("teeth", new HuongDanDocDTO(
                "teeth", "/tiːθ/", "những chiếc răng (số nhiều)",
                List.of("teeth"),
                List.of("tiːθ"),
                List.of("ti-th"),
                List.of("teeth"),
                0,
                "ti-th",
                "Căng khóe miệng cười đọc âm dài /tiː/ (ti), sau đó kẹp nhẹ đầu lưỡi giữa 2 răng thổi hơi /θ/ (-th).",
                "Cười căng khóe miệng đọc /tiː/, đưa đầu lưỡi ra giữa hai hàm răng thổi hơi gió /θ/ (-th).",
                "⚠️ Âm đuôi /θ/ (-th) thè nhẹ lưỡi thổi luồng hơi gió.",
                "Tránh đọc thành 'tít'.",
                "Mẹo nhớ: Đánh răng sạch 'TI' (tinh) tươm 'TH' (thơm) tho!"
        ));

        TU_DIEN_BOI_CHUAN.put("hand", new HuongDanDocDTO(
                "hand", "/hænd/", "bàn tay, tay",
                List.of("hand"),
                List.of("hænd"),
                List.of("hen-đ"),
                List.of("hand"),
                0,
                "hen-đ",
                "Từ 1 âm tiết, phát âm /hæ/ (lai e và a thành 'hen'), chặn âm /n/ rồi bật nhẹ âm /d/ (-đ).",
                "Mở rộng miệng phát âm /hæ/, hạ ngạc mềm đọc /n/ rồi bật nhẹ đầu lưỡi ở nướu trên âm /d/ (-đ).",
                "⚠️ Âm đuôi /d/ (-đ): Bật nhẹ đầu lưỡi ở nướu răng trên: 'hen-đ', không nuốt mất âm /d/.",
                "Người Việt hay đọc thành 'hen' hoặc 'han' mà quên bật âm /d/ cuối.",
                "Mẹo nhớ: 'HEN-Đ' (hẹn) nắm bàn tay ai đó thật ấm áp!"
        ));

        TU_DIEN_BOI_CHUAN.put("finger", new HuongDanDocDTO(
                "finger", "/ˈfɪŋ.ɡɚ/", "ngón tay",
                List.of("fin", "ger"),
                List.of("ˈfɪŋ", "ɡɚ"),
                List.of("PHINH", "gờ"),
                List.of("fing", "ger"),
                0,
                "phinh-gờ",
                "Trọng âm rơi vào âm 1 (PHINH) -> đọc to rõ, âm 'gờ' lướt nhẹ mềm mại: phinh-gờ.",
                "Răng trên chạm môi dưới phát âm /f/ kết hợp ngạc mềm /ŋ/ (phinh), âm sau bật /ɡ/ cuống họng cong lưỡi nhẹ /ɚ/ (gờ).",
                "Đuôi /ɚ/ hơi cong nhẹ đầu lưỡi vào trong vòm miệng.",
                "Tránh đọc bằng phẳng 'phin-gơ' không có trọng âm.",
                "Mẹo nhớ: Ngón tay đeo nhẫn vàng 'PHINH' (phổng) mũi 'GỜ' (gớm)!"
        ));

        TU_DIEN_BOI_CHUAN.put("arm", new HuongDanDocDTO(
                "arm", "/ɑːrm/", "cánh tay",
                List.of("arm"),
                List.of("ɑːrm"),
                List.of("am"),
                List.of("arm"),
                0,
                "am",
                "Từ 1 âm tiết, mở vòm họng đọc âm dài sâu /ɑː/, cong nhẹ lưỡi /r/ rồi ngậm môi phát âm /m/ (am).",
                "Mở rộng khoang miệng và hạ thấp lưỡi đọc /ɑː/, ngậm 2 môi ở cuối từ tạo âm /m/.",
                "Âm đuôi /m/ khép nhẹ hai môi.",
                "Tránh đọc cụt lủn hoặc quên ngậm môi.",
                "Mẹo nhớ: Cánh tay khoẻ mạnh ôm trọn 'AM' (ấm) áp!"
        ));

        TU_DIEN_BOI_CHUAN.put("leg", new HuongDanDocDTO(
                "leg", "/leɡ/", "chân, cẳng chân",
                List.of("leg"),
                List.of("leɡ"),
                List.of("léc"),
                List.of("leg"),
                0,
                "léc",
                "Từ 1 âm tiết, mở miệng phát âm /e/ rồi cuống lưỡi nâng chặn hơi dứt khoát /ɡ/ (đọc như 'léc').",
                "Đầu lưỡi chạm nướu trên đọc /l/, mở miệng phát âm /e/, cuống lưỡi nâng lên ngạc mềm chặn âm /ɡ/ (léc).",
                "Âm /ɡ/ ngắt dứt khoát ở cuống họng.",
                "Tránh đọc thành 'lếch' hay kéo dài giọng.",
                "Mẹo nhớ: Đôi chân nhanh nhẹn 'LÉC' (lách) qua đám đông!"
        ));

        TU_DIEN_BOI_CHUAN.put("foot", new HuongDanDocDTO(
                "foot", "/fʊt/", "bàn chân",
                List.of("foot"),
                List.of("fʊt"),
                List.of("phút"),
                List.of("foot"),
                0,
                "phút",
                "Từ 1 âm tiết, phát âm /ʊ/ ngắn dứt khoát rồi chặn nhẹ đầu lưỡi âm /t/ (đọc như 'phút').",
                "Răng trên chạm môi dưới thổi /f/, môi hơi chu thả lỏng /ʊ/, đầu lưỡi nâng chạm nướu chặn âm /t/ (phút).",
                "⚠️ Đuôi /t/ ngắt hơi dứt khoát ở đầu lưỡi.",
                "Tránh đọc âm /uː/ dài như 'phu-t', đây là âm ngắn dứt khoát: 'phút'.",
                "Mẹo nhớ: Bàn chân chạy bộ vài 'PHÚT' mỗi ngày rất khỏe!"
        ));

        TU_DIEN_BOI_CHUAN.put("feet", new HuongDanDocDTO(
                "feet", "/fiːt/", "hai bàn chân (số nhiều)",
                List.of("feet"),
                List.of("fiːt"),
                List.of("phít"),
                List.of("feet"),
                0,
                "phít",
                "Căng khóe miệng cười phát âm /iː/ kéo dài rồi chặn nhẹ đầu lưỡi âm /t/ (phít).",
                "Cười mở khóe miệng phát âm /fiː/, đầu lưỡi chạm nướu trên chặn âm /t/ (phít).",
                "Âm /t/ chặn dứt khoát.",
                "Phân biệt với foot (phút - âm u ngắn), feet có âm 'i' dài (phít).",
                "Mẹo nhớ: Hai bàn chân đi đôi giày vừa 'PHÍT' (khít)!"
        ));

        TU_DIEN_BOI_CHUAN.put("back", new HuongDanDocDTO(
                "back", "/bæk/", "lưng, phía sau",
                List.of("back"),
                List.of("bæk"),
                List.of("béc"),
                List.of("back"),
                0,
                "béc",
                "Từ 1 âm tiết, mở rộng miệng đọc /æ/ (lai a và e), ngắt hơi dứt khoát ở cuống họng /k/ (đọc như 'béc').",
                "Ngậm 2 môi bật /b/, mở rộng khẩu hình phát âm /æ/, cuống lưỡi nâng chặn hơi dứt khoát âm /k/ (béc).",
                "⚠️ Âm đuôi /k/ ngắt hơi sắc ở cuống họng.",
                "Tránh đọc thành 'bắc' hay 'bách'.",
                "Mẹo nhớ: Cái lưng mỏi nằm nghỉ trên chiếc ghế 'BÉC' (bành)!"
        ));
    }

    // =========================================================================
    // THUẬT TOÁN PHÂN TÍCH NGỮ ÂM THÔNG MINH DỰA TRÊN IPA VÀ NGUYÊN TẮC ÂM TIẾT
    // =========================================================================
    private HuongDanDocDTO taoHuongDanDocThongMinh(String tu, String phienAmRaw, String nghia) {
        String tuLower = tu.toLowerCase().trim();

        // 1. Phân tích ngữ âm từ IPA nếu có
        String ipa = (phienAmRaw != null && !phienAmRaw.isBlank()) ? phienAmRaw.trim() : "";
        String ipaClean = ipa.replaceAll("[/|\\[\\]]", "").trim();

        List<PhoneticSyllable> syllables = phanTichAmTietTuIpaVaTu(tuLower, ipaClean);

        List<String> amTiet = new ArrayList<>();
        List<String> amTietIpa = new ArrayList<>();
        List<String> amTietBoi = new ArrayList<>();
        List<String> amTietDoc = new ArrayList<>();
        List<String> boiDisplay = new ArrayList<>();

        int amNhan = 0;

        for (int i = 0; i < syllables.size(); i++) {
            PhoneticSyllable ps = syllables.get(i);
            if (ps.isStressed) {
                amNhan = i;
            }
            amTiet.add(ps.englishPart);
            amTietIpa.add(ps.ipaPart);
            String boi = (syllables.size() > 1 && ps.isStressed) ? ps.vietnameseBoi.toUpperCase() : ps.vietnameseBoi;
            amTietBoi.add(boi);
            boiDisplay.add(boi);
            amTietDoc.add(ps.ttsSpeakText);
        }

        if (syllables.isEmpty()) {
            amTiet.add(tuLower);
            amTietIpa.add(ipaClean.isEmpty() ? tuLower : ipaClean);
            amTietBoi.add(tuLower.toUpperCase());
            amTietDoc.add(tuLower);
            boiDisplay.add(tuLower.toUpperCase());
        }

        String phienAmViet = String.join(" - ", boiDisplay);
        String ipaHienThi = !ipa.isEmpty() ? (ipa.startsWith("/") ? ipa : "/" + ipa + "/") : "/" + tuLower + "/";
        String nghiaHienThi = (nghia != null && !nghia.isBlank()) ? nghia : "";

        // Xác định âm đuôi
        String chuYAmDuoi = "Đọc dứt khoát, giữ hơi đều giữa các âm.";
        if (tuLower.endsWith("ge") || tuLower.endsWith("dge") || ipaClean.endsWith("dʒ")) {
            chuYAmDuoi = "⚠️ Âm đuôi /dʒ/: Chu tròn môi và bật hơi dứt khoát (như 'ch' có rung thanh quản).";
        } else if (tuLower.endsWith("s") || tuLower.endsWith("ce") || tuLower.endsWith("se") || ipaClean.endsWith("s")) {
            chuYAmDuoi = "⚠️ Âm đuôi /s/: Xì nhẹ luồng hơi qua kẽ răng, không được nuốt âm cuối.";
        } else if (tuLower.endsWith("t") || ipaClean.endsWith("t")) {
            chuYAmDuoi = "⚠️ Âm đuôi /t/: Đầu lưỡi chạm nướu trên và bật hơi dứt khoát.";
        } else if (tuLower.endsWith("d") || ipaClean.endsWith("d")) {
            chuYAmDuoi = "⚠️ Âm đuôi /d/: Chặn nhẹ đầu lưỡi và rung nhẹ dây thanh quản.";
        } else if (tuLower.endsWith("k") || tuLower.endsWith("c") || tuLower.endsWith("ck") || ipaClean.endsWith("k")) {
            chuYAmDuoi = "⚠️ Âm đuôi /k/: Ngắt hơi sắc và dứt khoát ở cuống họng.";
        }

        String amNhanText = boiDisplay.size() > amNhan ? boiDisplay.get(amNhan) : tuLower;

        return new HuongDanDocDTO(
                tu,
                ipaHienThi,
                nghiaHienThi,
                amTiet,
                amTietIpa,
                amTietBoi,
                amTietDoc,
                amNhan,
                phienAmViet,
                "Trọng âm chính rơi vào âm " + (amNhan + 1) + " (" + amNhanText + ") - đọc to, cao và rõ hơn các âm còn lại.",
                "Mở khẩu hình tự nhiên, thả lỏng cơ hàm và lấy hơi từ bụng để giọng phát âm chuẩn và tự nhiên.",
                chuYAmDuoi,
                "Người Việt hay có thói quen đọc bằng phẳng các âm, hãy nhấn mạnh vào âm có trọng âm (" + amNhanText + ")!",
                "Mẹo nhỏ: Nghe chậm 0.6x trước để nắm rõ từng âm, sau đó luyện đọc theo tốc độ 1.0x."
        );
    }

    private static class PhoneticSyllable {
        String englishPart;
        String ipaPart;
        String vietnameseBoi;
        String ttsSpeakText;
        boolean isStressed;

        PhoneticSyllable(String englishPart, String ipaPart, String vietnameseBoi, String ttsSpeakText, boolean isStressed) {
            this.englishPart = englishPart;
            this.ipaPart = ipaPart;
            this.vietnameseBoi = vietnameseBoi;
            this.ttsSpeakText = ttsSpeakText;
            this.isStressed = isStressed;
        }
    }

    private List<PhoneticSyllable> phanTichAmTietTuIpaVaTu(String word, String ipaRaw) {
        List<PhoneticSyllable> result = new ArrayList<>();
        String ipa = chuanHoaIpaDauVao(ipaRaw);

        // Trường hợp đặc biệt: từ bắt đầu bằng "u" phát âm /juː/ như usage, user
        if (word.startsWith("usag") || (ipa.contains("j") && ipa.contains("u") && word.startsWith("u"))) {
            result.add(new PhoneticSyllable("u", "juː", "DIU", "you", true));
            result.add(new PhoneticSyllable(word.substring(1), "sɪdʒ", "sịch", "sidge", false));
            return result;
        }

        // Trường hợp đặc biệt: "annual"
        if (word.equals("annual")) {
            result.add(new PhoneticSyllable("an", "ˈæn", "AN", "an", true));
            result.add(new PhoneticSyllable("nu", "ju", "niu", "you", false));
            result.add(new PhoneticSyllable("al", "əl", "ờl", "ull", false));
            return result;
        }

        // Nếu IPA có chứa dấu chấm phân tách âm tiết (ví dụ Cambridge / Oxford: /ˈæn.ju.əl/, /ˈkʌm.fɚ.t̬ə.bəl/)
        if (ipa.contains(".")) {
            String[] ipaParts = ipa.split("\\.");
            int count = ipaParts.length;
            List<String> enParts = chiaTuTheoSoLuongAmTiet(word, count);

            for (int i = 0; i < count; i++) {
                String ipaSyl = ipaParts[i].replaceAll("[ˈˌ']", "").trim();
                boolean stressed = ipaParts[i].contains("ˈ") || ipaParts[i].contains("'") || (i == 0 && !ipa.contains("ˈ"));
                String enSyl = (i < enParts.size()) ? enParts.get(i) : ipaSyl;
                String boi = chuyenIpaSangBoiTiengViet(ipaSyl, enSyl);
                String tts = taoTuDocChoSpeech(ipaSyl, enSyl);
                result.add(new PhoneticSyllable(enSyl, ipaSyl, boi, tts, stressed));
            }
            return result;
        }

        // Thuật toán tách âm tiết tiếng Anh thực tế
        List<String> syllables = tachAmTietTiengAnhThucTe(word);
        int stressedIndex = 0;
        if (ipa.contains("ˈ") || ipa.contains("'")) {
            // Xác định trọng âm rơi vào âm nào dựa trên vị trí dấu ˈ
            int stressPos = Math.max(ipa.indexOf("ˈ"), ipa.indexOf("'"));
            if (stressPos > 2 && syllables.size() > 1) {
                stressedIndex = Math.min(1, syllables.size() - 1);
            }
        }

        for (int i = 0; i < syllables.size(); i++) {
            String syl = syllables.get(i);
            boolean stressed = (i == stressedIndex);
            String boi = chuyenIpaSangBoiTiengViet(syl, syl);
            String tts = taoTuDocChoSpeech(syl, syl);
            result.add(new PhoneticSyllable(syl, syl, boi, tts, stressed));
        }

        return result;
    }

    private List<String> chiaTuTheoSoLuongAmTiet(String word, int count) {
        List<String> res = new ArrayList<>();
        if (count <= 1 || word.length() <= count) {
            res.add(word);
            return res;
        }
        List<String> natural = tachAmTietTiengAnhThucTe(word);
        if (natural.size() == count) {
            return natural;
        }

        int len = word.length();
        int step = Math.max(1, len / count);
        int start = 0;
        for (int i = 0; i < count - 1; i++) {
            int end = Math.min(len, start + step);
            res.add(word.substring(start, end));
            start = end;
        }
        if (start < len) {
            res.add(word.substring(start));
        }
        return res;
    }

    private List<String> tachAmTietTiengAnhThucTe(String word) {
        List<String> list = new ArrayList<>();
        if (word.length() <= 3) {
            list.add(word);
            return list;
        }

        // 1. Tách các hậu tố phổ biến trước
        if (word.endsWith("tion") && word.length() > 4) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 4)));
            list.add("tion");
            return list;
        }
        if (word.endsWith("ment") && word.length() > 4) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 4)));
            list.add("ment");
            return list;
        }
        if (word.endsWith("ble") && word.length() > 3) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 3)));
            list.add("ble");
            return list;
        }
        if (word.endsWith("ful") && word.length() > 3) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 3)));
            list.add("ful");
            return list;
        }
        if (word.endsWith("ing") && word.length() > 4) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 3)));
            list.add("ing");
            return list;
        }
        if (word.endsWith("ture") && word.length() > 4) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 4)));
            list.add("ture");
            return list;
        }
        if (word.endsWith("ence") && word.length() > 4) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 4)));
            list.add("ence");
            return list;
        }
        if (word.endsWith("ance") && word.length() > 4) {
            list.addAll(tachAmTietTiengAnhThucTe(word.substring(0, word.length() - 4)));
            list.add("ance");
            return list;
        }

        // 2. Tách cụm nguyên âm hiến định (Hiatus) như "-ual" trong annual: an-nu-al
        if (word.endsWith("ual") && word.length() >= 5) {
            String base = word.substring(0, word.length() - 2); // e.g. "annu"
            list.addAll(tachAmTietTiengAnhThucTe(base));
            list.add("al");
            return list;
        }

        // 3. Tách theo quy tắc phụ âm kép giữa 2 nguyên âm (ví dụ: ap-ple, cab-bage, let-tuce, an-nu)
        String doubleConsonant = "(?<=[aeiouy][bcdfghjklmnpqrstvwxz])(?=[bcdfghjklmnpqrstvwxz][aeiouy])";
        String[] parts = word.split(doubleConsonant);
        if (parts.length > 1 && parts.length <= 4) {
            for (String p : parts) if (!p.isBlank()) list.add(p);
            return list;
        }

        // 4. Tách theo nguyên âm - phụ âm
        String vcv = "(?<=[aeiouy])(?=[bcdfghjklmnpqrstvwxz][aeiouy])";
        String[] vcvParts = word.split(vcv);
        if (vcvParts.length > 1 && vcvParts.length <= 4) {
            for (String p : vcvParts) if (!p.isBlank()) list.add(p);
            return list;
        }

        list.add(word);
        return list;
    }

    private String chuyenIpaSangBoiTiengViet(String ipa, String enSyl) {
        String s = ipa.toLowerCase().trim();
        String en = enSyl.toLowerCase().trim();

        // 0. Nhận diện các từ vựng thường gặp từ ảnh mẫu
        if (en.equals("head") || s.equals("hed")) return "hét";
        if (en.equals("hair") || s.startsWith("heə")) return "he";
        if (en.equals("face") || s.equals("feɪs")) return "phây-x";
        if (en.equals("eye") || s.equals("aɪ")) return "ai";
        if (en.equals("ear") || s.startsWith("ɪə")) return "ia";
        if (en.equals("nose") || s.equals("noʊz")) return "nâu-z";
        if (en.equals("mouth") || s.equals("maʊθ")) return "mao-th";
        if (en.equals("tooth") || s.equals("tuːθ")) return "tu-th";
        if (en.equals("teeth") || s.equals("tiːθ")) return "ti-th";
        if (en.equals("hand") || s.equals("hænd")) return "hen-đ";
        if (en.equals("arm") || s.startsWith("ɑːrm")) return "am";
        if (en.equals("leg") || s.equals("leɡ")) return "léc";
        if (en.equals("foot") || s.equals("fʊt")) return "phút";
        if (en.equals("feet") || s.equals("fiːt")) return "phít";
        if (en.equals("back") || s.equals("bæk")) return "béc";
        if (en.equals("fin") || s.equals("fɪŋ")) return "phinh";
        if (en.equals("ger") || s.equals("ɡɚ") || s.equals("ɡər")) return "gờ";

        // 1. Âm đuôi /əl/ (chuẩn xác từng phụ âm kết hợp, tuyệt đối không ra 'uồl')
        if (s.contains("pəl") || en.endsWith("ple")) return "pồ";
        if (s.contains("bəl") || en.endsWith("ble")) return "bồ";
        if (s.contains("təl") || en.endsWith("tle")) return "tồ";
        if (s.contains("dəl") || en.endsWith("dle")) return "đồ";
        if (s.contains("kəl") || en.endsWith("cle")) return "cồ";
        if (s.contains("fəl") || en.endsWith("fle")) return "phồ";
        if (s.contains("səl") || en.endsWith("sle")) return "sồ";
        if (s.contains("əl") || s.contains("ʌɫ") || s.contains("əɫ") || s.contains("ɫ") || en.equals("al") || en.equals("el")) return "ờl";

        // 2. Vần kết hợp u/ju
        if (s.contains("nju") || (en.equals("nu") && s.contains("ju"))) return "niu";
        if (s.contains("bjuː") || s.contains("bju")) return "Biu";
        if (s.contains("fjuː") || s.contains("fju")) return "Phiu";
        if (s.contains("kjuː") || s.contains("kju")) return "Kiu";
        if (s.contains("mjuː") || s.contains("mju")) return "Miu";
        if (s.contains("vjuː") || s.contains("vju")) return "Viu";
        if (s.contains("juː") || s.contains("ju")) return "Diu";

        // 3. Các phụ âm đuôi đặc biệt
        if (s.contains("sɪdʒ") || s.contains("sʌdʒ") || s.contains("sədʒ")) return "sịch";
        if (s.contains("bɪdʒ") || en.equals("bage")) return "bích";
        if (s.contains("ɪdʒ") || en.equals("age")) return "ịch";
        if (s.contains("ʃən") || en.equals("tion") || en.equals("sion")) return "sần";
        if (s.contains("tʃɚ") || s.contains("tʃər") || en.equals("ture")) return "chờ";
        if (s.contains("fəl") || en.equals("ful")) return "phun";
        if (s.contains("mənt") || en.equals("ment")) return "mừn-t";
        if (s.contains("ɪs") || en.equals("tuce")) return "tịt-s";
        if (s.contains("tənt") || en.equals("tant")) return "tần-t";
        if (s.contains("əns") || en.equals("ence") || en.equals("ance")) return "ần-s";

        // 4. Các âm tiết thường gặp
        if (s.contains("æn") || en.equals("an")) return "An";
        if (s.contains("æp") || en.equals("ap") || en.equals("app")) return "Ép";
        if (s.contains("kæb") || en.equals("cab")) return "Kép";
        if (s.contains("kʌm") || en.equals("com")) return "Kăm";
        if (s.contains("ɡɑːr") || en.equals("gar")) return "Gaa";
        if (s.contains("lɪk") || en.equals("lic")) return "lịk";
        if (s.contains("let") || en.equals("let")) return "Le";
        if (s.contains("fɚ") || en.equals("for") || en.equals("fer")) return "phơ";
        if (s.contains("meɪ") || en.equals("ma") || en.equals("may")) return "Mây";
        if (s.contains("pɔːr") || en.equals("por")) return "Po";
        if (s.contains("spɪr") || en.equals("pe")) return "Spí";

        String res = chuyenAmTietSangBoi(enSyl);
        String resLower = res.toLowerCase();
        if (s.endsWith("s") && !resLower.endsWith("-x") && !resLower.endsWith("s") && !resLower.endsWith("x")) {
            res = res + "-x";
        } else if (s.endsWith("z") && !resLower.endsWith("-z") && !resLower.endsWith("z")) {
            res = res + "-z";
        } else if ((s.endsWith("θ") || s.endsWith("ð")) && !resLower.endsWith("-th")) {
            res = res + "-th";
        } else if (s.endsWith("d") && (en.endsWith("nd") || en.endsWith("od") || en.endsWith("ed")) && !resLower.endsWith("-đ") && !resLower.endsWith("t")) {
            res = res + "-đ";
        } else if (s.endsWith("tʃ") && !resLower.endsWith("-ch")) {
            res = res + "-ch";
        }
        return res;
    }

    private String chuyenAmTietSangBoi(String syl) {
        String s = syl.toLowerCase().trim();
        if (s.equals("head")) return "hét";
        if (s.equals("hair")) return "he";
        if (s.equals("face")) return "phây-x";
        if (s.equals("eye")) return "ai";
        if (s.equals("ear")) return "ia";
        if (s.equals("nose")) return "nâu-z";
        if (s.equals("mouth")) return "mao-th";
        if (s.equals("tooth")) return "tu-th";
        if (s.equals("teeth")) return "ti-th";
        if (s.equals("hand")) return "hen-đ";
        if (s.equals("arm")) return "am";
        if (s.equals("leg")) return "léc";
        if (s.equals("foot")) return "phút";
        if (s.equals("feet")) return "phít";
        if (s.equals("back")) return "béc";
        if (s.equals("fin")) return "phinh";
        if (s.equals("ger")) return "gờ";
        if (s.equals("u") || s.equals("you")) return "Diu";
        if (s.equals("nu")) return "niu";
        if (s.equals("an")) return "An";
        if (s.equals("al") || s.equals("el")) return "ờl";
        if (s.equals("sage")) return "sịch";
        if (s.equals("ser")) return "zờ";
        if (s.equals("use")) return "Diu-z";
        if (s.equals("hel")) return "Hê";
        if (s.equals("lo")) return "lô";
        if (s.equals("ap") || s.equals("app")) return "Ép";
        if (s.equals("ple") || s.equals("ples")) return "pồ";
        if (s.equals("cab")) return "Kép";
        if (s.equals("bage")) return "bích";
        if (s.equals("gar")) return "Gaa";
        if (s.equals("lic")) return "lịk";
        if (s.equals("let")) return "Le";
        if (s.equals("tuce")) return "tịt-s";
        if (s.equals("com")) return "Kăm";
        if (s.equals("for")) return "phơ";
        if (s.equals("ta")) return "tờ";
        if (s.equals("ble")) return "bồ";
        if (s.equals("beau")) return "Biu";
        if (s.equals("ful")) return "phun";
        if (s.equals("tion")) return "sần";
        if (s.equals("ment")) return "mừn";
        if (s.equals("ing")) return "ing";
        if (s.equals("ma")) return "Mây";
        if (s.equals("por")) return "Po";
        if (s.equals("tant")) return "tần-t";
        if (s.equals("ence") || s.equals("ance")) return "ần-s";

        // Thay thế ngữ âm thông dụng
        String b = s.replaceAll("tion", "sần")
                .replaceAll("ment", "mừn")
                .replaceAll("ble", "bồ")
                .replaceAll("ful", "phun")
                .replaceAll("ph", "f")
                .replaceAll("ch", "ch")
                .replaceAll("sh", "s")
                .replaceAll("th", "th")
                .replaceAll("ee|ea", "ii")
                .replaceAll("oo", "uu")
                .replaceAll("ou|ow", "ao")
                .replaceAll("ai|ay", "ay")
                .replaceAll("ar", "aa")
                .replaceAll("or", "oo")
                .replaceAll("c(?=[eiy])", "s")
                .replaceAll("c", "k");

        return b.substring(0, 1).toUpperCase() + (b.length() > 1 ? b.substring(1) : "");
    }

    private String taoTuDocChoSpeech(String ipa, String enSyl) {
        String s = enSyl.toLowerCase().trim();
        if (s.equals("al")) return "ull";
        if (s.equals("el")) return "ell";
        if (s.equals("nu")) return "you";
        if (s.equals("an")) return "an";
        if (s.equals("u")) return "you";
        if (s.equals("sage")) return "sidge";
        if (s.equals("ser")) return "zer";
        if (s.equals("ble")) return "bull";
        if (s.equals("ple")) return "pull";
        if (s.equals("tle")) return "tull";
        if (s.equals("dle")) return "dull";
        if (s.equals("cle")) return "cull";
        if (s.equals("fle")) return "full";
        if (s.equals("bage")) return "bidge";
        if (s.equals("tuce")) return "tiss";
        if (s.equals("tion")) return "shun";
        if (s.equals("sion")) return "shun";
        if (s.equals("ture")) return "chur";
        if (s.equals("ful")) return "full";
        if (s.equals("for")) return "fer";
        if (s.equals("ta")) return "tuh";
        if (s.equals("com")) return "come";
        if (s.equals("beau")) return "byoo";
        if (s.equals("ti")) return "tee";
        if (s.equals("lic")) return "lick";
        if (s.equals("ap") || s.equals("app")) return "app";
        if (s.equals("ment")) return "muhnt";
        return enSyl;
    }
}
