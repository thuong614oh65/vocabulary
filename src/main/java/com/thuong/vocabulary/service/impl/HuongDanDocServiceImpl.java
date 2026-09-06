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

        // 1. Kiểm tra từ điển chuẩn hóa đã được biên soạn kỹ lưỡng trước
        if (TU_DIEN_BOI_CHUAN.containsKey(tuChuanHoa)) {
            HuongDanDocDTO kq = layMauCoSan(tuChuanHoa, phienAm, nghia);
            cache.put(tuChuanHoa, kq);
            return kq;
        }

        HuongDanDocDTO ketQua = null;

        // 2. Thử gọi Gemini AI nếu GeminiService khả dụng
        if (geminiService != null) {
            try {
                ketQua = goiGeminiPhanTich(tu.trim(), phienAm, nghia);
            } catch (Exception e) {
                System.err.println("[HuongDanDocService] Gọi Gemini không thành công: " + e.getMessage() + " -> Dùng bộ quy tắc ngữ âm dự phòng.");
            }
        }

        // 3. Nếu không có AI hoặc AI lỗi -> Dùng thuật toán phân tích ngữ âm thông minh dựa trên IPA & mặt chữ
        if (ketQua == null) {
            ketQua = taoHuongDanDocThongMinh(tu.trim(), phienAm, nghia);
        }

        cache.put(tuChuanHoa, ketQua);
        return ketQua;
    }

    private HuongDanDocDTO goiGeminiPhanTich(String tu, String phienAm, String nghia) throws Exception {
        String prompt = """
                Bạn là chuyên gia ngữ âm tiếng Anh hàng đầu cho người Việt Nam.
                Hãy phân tích chi tiết cách phát âm của từ tiếng Anh sau:
                - Từ: "%s"
                - Phiên âm IPA: "%s"
                - Nghĩa: "%s"

                YÊU CẦU BẮT BUỘC VỀ TÁCH ÂM TIẾT VÀ PHIÊN ÂM TIẾNG VIỆT (CHUẨN BẢN XỨ):
                1. 'amTiet': Mảng các âm tiết tiếng Anh thực tế trong từ (chỉ chứa chữ tiếng Anh sạch, KHÔNG kèm mở ngoặc), ví dụ:
                   - "usage" -> ["u", "sage"]
                   - "comfortable" -> ["com", "for", "ta", "ble"]
                   - "apple" -> ["ap", "ple"]
                   - "cabbage" -> ["cab", "bage"]
                2. 'amTietDoc': Mảng từ phát âm tiếng Anh để Web Speech API đọc đúng âm đó trong từ (ví dụ cho "usage": ["you", "sidge"]; cho "cabbage": ["cab", "bidge"]; cho "lettuce": ["let", "tiss"]).
                3. 'amTietBoi': Mảng phiên âm bồi tiếng Việt tương ứng từng âm, ví dụ:
                   - "usage" -> ["DIU", "sịch"]
                   - "comfortable" -> ["KĂM", "phơ", "tờ", "bồ"]
                   - "apple" -> ["ÉP", "pồ"]
                4. 'amTietIpa': Mảng phiên âm IPA từng âm tiết, ví dụ cho "usage": ["juː", "sɪdʒ"].
                5. 'phienAmTiengViet': Chuỗi nối các âm tiết bồi bằng dấu gạch ngang '-', âm trọng âm viết HOA (ví dụ: "DIU - sịch", "KĂM - phơ - tờ - bồ", "ÉP - pồ", "GAA - lịk").
                6. 'amNhanIndex': Chỉ số âm tiết mang trọng âm chính (0-indexed).
                7. 'trongAm': Lời giải thích ngắn gọn âm nào cần nhấn mạnh.
                8. 'khauHinh': Hướng dẫn mở miệng, đặt lưỡi.
                9. 'amDuoi': Nhắc nhở âm đuôi quan trọng (như /dʒ/, /s/, /t/, /d/, /k/...).
                10. 'loiThuongGap': Lỗi người Việt thường phát âm sai ở từ này.
                11. 'meoGhiNho': Mẹo nhớ vui và trực quan.

                CHỈ TRẢ VỀ DUY NHẤT 1 ĐỐI TƯỢNG JSON (KHÔNG KÈM MARKDOWN, KHÔNG GIẢI THÍCH):
                {
                  "tu": "%s",
                  "phienAm": "%s",
                  "nghia": "%s",
                  "amTiet": ["u", "sage"],
                  "amTietDoc": ["you", "sidge"],
                  "amTietBoi": ["DIU", "sịch"],
                  "amTietIpa": ["juː", "sɪdʒ"],
                  "amNhanIndex": 0,
                  "phienAmTiengViet": "DIU - sịch",
                  "trongAm": "Nhấn mạnh vào âm tiết thứ nhất (DIU)...",
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
            String boi = ps.isStressed ? ps.vietnameseBoi.toUpperCase() : ps.vietnameseBoi.toLowerCase();
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

    private List<PhoneticSyllable> phanTichAmTietTuIpaVaTu(String word, String ipa) {
        List<PhoneticSyllable> result = new ArrayList<>();

        // Kiểm tra trường hợp đặc biệt bắt đầu bằng "u" phát âm /juː/ như usage, user, utility, university
        if (word.startsWith("usag") || (ipa.contains("j") && ipa.contains("u") && word.startsWith("u"))) {
            result.add(new PhoneticSyllable("u", "juː", "DIU", "you", true));
            result.add(new PhoneticSyllable(word.substring(1), "sɪdʒ", "sịch", "sidge", false));
            return result;
        }

        // Nếu IPA có chứa dấu chấm phân tách âm tiết (ví dụ Cambridge / Oxford: /ˈæp.əl/, /ˈkʌm.fɚ.t̬ə.bəl/)
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

        // Nếu IPA có chứa dấu trọng âm ' hoặc ˈ phân tách
        if (ipa.contains("ˈ") || ipa.contains("'")) {
            String[] ipaParts = ipa.split("[ˈ']");
            if (ipaParts.length == 2 && !ipaParts[0].isBlank() && !ipaParts[1].isBlank()) {
                List<String> enParts = chiaTuTheoSoLuongAmTiet(word, 2);
                String syl1Ipa = ipaParts[0].trim();
                String syl2Ipa = ipaParts[1].trim();

                result.add(new PhoneticSyllable(enParts.get(0), syl1Ipa, chuyenIpaSangBoiTiengViet(syl1Ipa, enParts.get(0)), taoTuDocChoSpeech(syl1Ipa, enParts.get(0)), false));
                result.add(new PhoneticSyllable(enParts.get(1), syl2Ipa, chuyenIpaSangBoiTiengViet(syl2Ipa, enParts.get(1)), taoTuDocChoSpeech(syl2Ipa, enParts.get(1)), true));
                return result;
            }
        }

        // Thuật toán tách âm tiết tiếng Anh thực tế
        List<String> syllables = tachAmTietTiengAnhThucTe(word);
        for (int i = 0; i < syllables.size(); i++) {
            String syl = syllables.get(i);
            boolean stressed = (i == 0);
            String boi = chuyenAmTietSangBoi(syl);
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

        // Tách các hậu tố phổ biến trước
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

        // Tách theo quy tắc phụ âm kép (ví dụ: ap-ple, cab-bage, let-tuce)
        String doubleConsonant = "(?<=[bcdfghjklmnpqrstvwxz])(?=[bcdfghjklmnpqrstvwxz])";
        String[] parts = word.split(doubleConsonant);
        if (parts.length > 1 && parts.length <= 4) {
            for (String p : parts) if (!p.isBlank()) list.add(p);
            return list;
        }

        // Tách theo nguyên âm - phụ âm
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
        if (s.contains("juː") || s.contains("ju")) return "Diu";
        if (s.contains("sɪdʒ") || s.contains("sʌdʒ") || s.contains("sədʒ")) return "sịch";
        if (s.contains("æp")) return "Ép";
        if (s.contains("əl")) return "pồ";
        if (s.contains("kʌm")) return "Kăm";
        if (s.contains("fɚ")) return "phơ";
        if (s.contains("bəl")) return "bồ";
        if (s.contains("ɡɑːr")) return "Gaa";
        if (s.contains("lɪk")) return "lịk";
        if (s.contains("kæb")) return "Kép";
        if (s.contains("ɪdʒ")) return "bích";
        if (s.contains("let")) return "Le";
        if (s.contains("ɪs")) return "tịt-s";
        if (s.contains("bjuː")) return "Biu";
        if (s.contains("fəl")) return "phun";
        if (s.contains("meɪ")) return "Mây";
        if (s.contains("ʃən")) return "sần";
        if (s.contains("tənt")) return "tần-t";
        if (s.contains("pɔːr")) return "Po";

        return chuyenAmTietSangBoi(enSyl);
    }

    private String chuyenAmTietSangBoi(String syl) {
        String s = syl.toLowerCase().trim();
        if (s.equals("u") || s.equals("you")) return "Diu";
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
        if (s.equals("u")) return "you";
        if (s.equals("sage")) return "sidge";
        if (s.equals("ser")) return "zer";
        if (s.equals("ble")) return "bull";
        if (s.equals("ple")) return "pull";
        if (s.equals("bage")) return "bidge";
        if (s.equals("tuce")) return "tiss";
        if (s.equals("tion")) return "shun";
        if (s.equals("ful")) return "full";
        if (s.equals("for")) return "fer";
        if (s.equals("ta")) return "tuh";
        if (s.equals("com")) return "come";
        if (s.equals("beau")) return "byoo";
        if (s.equals("ti")) return "tee";
        if (s.equals("lic")) return "lick";
        if (s.equals("ap") || s.equals("app")) return "app";
        return enSyl;
    }
}
