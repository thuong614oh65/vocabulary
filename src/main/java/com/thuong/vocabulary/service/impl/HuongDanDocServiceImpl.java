package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.HuongDanDocDTO;
import com.thuong.vocabulary.service.HuongDanDocService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HuongDanDocServiceImpl implements HuongDanDocService {

    private final Map<String, HuongDanDocDTO> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-keys:${gemini.api-key:}}")
    private String configApiKeys;

    @Override
    public HuongDanDocDTO layHuongDanDoc(String tu, String phienAm, String nghia) {
        if (tu == null || tu.isBlank()) {
            return taoHuongDanDocDuPhong("hello", "/həˈloʊ/", "xin chào");
        }

        String tuChuanHoa = tu.trim().toLowerCase();
        if (cache.containsKey(tuChuanHoa)) {
            return cache.get(tuChuanHoa);
        }

        HuongDanDocDTO ketQua = null;

        // 1. Thử gọi Gemini AI nếu có API Key
        String apiKey = layApiKeyKhaDung();
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                ketQua = goiGeminiPhanTich(tu.trim(), phienAm, nghia, apiKey);
            } catch (Exception e) {
                System.err.println("[HuongDanDocService] Lỗi gọi Gemini AI: " + e.getMessage() + " -> Dùng bộ quy tắc dự phòng chuẩn hóa.");
            }
        }

        // 2. Nếu không có AI hoặc lỗi -> Dùng thuật toán chuẩn hóa tiếng Việt chất lượng cao
        if (ketQua == null) {
            ketQua = taoHuongDanDocDuPhong(tu.trim(), phienAm, nghia);
        }

        cache.put(tuChuanHoa, ketQua);
        return ketQua;
    }

    private String layApiKeyKhaDung() {
        if (configApiKeys == null || configApiKeys.isBlank()) {
            return System.getenv("GEMINI_API_KEY");
        }
        String[] keys = configApiKeys.split("[,;\\n\\r]+");
        for (String k : keys) {
            String trimmed = k.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return System.getenv("GEMINI_API_KEY");
    }

    private HuongDanDocDTO goiGeminiPhanTich(String tu, String phienAm, String nghia, String apiKey) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String prompt = """
                Bạn là chuyên gia ngữ âm tiếng Anh hàng đầu cho người Việt Nam.
                Hãy phân tích chi tiết cách phát âm của từ tiếng Anh sau:
                - Từ: "%s"
                - Phiên âm IPA: "%s"
                - Nghĩa: "%s"

                YÊU CẦU QUAN TRỌNG VỀ PHIÊN ÂM TIẾNG VIỆT (CHUẨN HÓA TIẾNG VIỆT):
                1. 'phienAmTiengViet': Viết cách đọc chuẩn hóa tiếng Việt cực kỳ tự nhiên, dễ đọc, chuẩn chữ quốc ngữ, nối các âm bằng dấu gạch ngang '-'.
                   Ví dụ kinh điển:
                   - "hello" -> "Hê - lô"
                   - "apple" -> "Ép - pồ"
                   - "garlic" -> "Gaa - lịk"
                   - "lettuce" -> "Le - tịt"
                   - "cabbage" -> "Kép - bích"
                   - "comfortable" -> "Kăm - phơ - tờ - bồ"
                   - "important" -> "Im - po - tần - t"
                   - "beautiful" -> "Biu - ti - phun"
                   Âm mang trọng âm chính hãy viết HOA (ví dụ: "HÊ - lô", "GAA - lịk", "ÉP - pồ").
                2. 'amTiet': Mảng các âm tiết tiếng Anh kèm bồi tiếng Việt tương ứng, ví dụ: ["hel (HÊ)", "lo (lô)"]
                3. 'amTietIpa': Mảng phiên âm IPA từng âm tiết.
                4. 'amTietBoi': Mảng chỉ gồm phiên âm bồi tiếng Việt từng âm, ví dụ: ["HÊ", "lô"]
                5. 'amNhanIndex': Chỉ số âm tiết mang trọng âm chính (bắt đầu từ 0).
                6. 'trongAm': Lời giải thích ngắn gọn âm nào cần nhấn mạnh, to, cao hơn.
                7. 'khauHinh': Hướng dẫn mở miệng, đặt đầu lưỡi, lấy hơi.
                8. 'amDuoi': Nhắc nhở các âm đuôi /s/, /t/, /d/, /k/, /ʃ/, /tʃ/ cần bật hơi rõ.
                9. 'loiThuongGap': Lỗi người Việt thường phát âm sai ở từ này.
                10. 'meoGhiNho': Một câu mẹo ngắn, dễ nhớ để đọc đúng từ này.

                CHỈ TRẢ VỀ DUY NHẤT 1 ĐỐI TƯỢNG JSON (KHÔNG KÈM MARKDOWN, KHÔNG GIẢI THÍCH):
                {
                  "tu": "%s",
                  "phienAm": "%s",
                  "nghia": "%s",
                  "amTiet": ["hel (HÊ)", "lo (lô)"],
                  "amTietIpa": ["hə", "loʊ"],
                  "amTietBoi": ["HÊ", "lô"],
                  "amNhanIndex": 0,
                  "phienAmTiengViet": "HÊ - lô",
                  "trongAm": "Nhấn mạnh vào âm tiết thứ nhất (HÊ)...",
                  "khauHinh": "...",
                  "amDuoi": "...",
                  "loiThuongGap": "...",
                  "meoGhiNho": "..."
                }
                """.formatted(tu, phienAm != null ? phienAm : "", nghia != null ? nghia : "",
                tu, phienAm != null ? phienAm : "", nghia != null ? nghia : "");

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String rawText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                String cleanJson = rawText.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
                return objectMapper.readValue(cleanJson, HuongDanDocDTO.class);
            }
        }
        return null;
    }

    // =========================================================================
    // BỘ QUY TẮC CHUẨN HÓA TIẾNG VIỆT CHẤT LƯỢNG CAO (HEURISTIC FALLBACK)
    // =========================================================================
    private static final Map<String, HuongDanDocDTO> TU_DIEN_BOI_CHUAN = new HashMap<>();

    static {
        TU_DIEN_BOI_CHUAN.put("hello", new HuongDanDocDTO(
                "hello", "/həˈloʊ/", "xin chào",
                List.of("hel (HÊ)", "lo (lô)"),
                List.of("hə", "loʊ"),
                List.of("HÊ", "lô"),
                0,
                "HÊ - lô",
                "Trọng âm rơi vào âm tiết đầu HÊ (đọc cao và dứt khoát hơn: HÊ-lô, hoặc hơ-LÔU)",
                "Miệng mở vừa phải, bật hơi nhẹ âm /h/, tròn môi khi kết thúc âm /oʊ/.",
                "Âm đuôi /oʊ/ cần chu môi tròn nhẹ ở cuối âm.",
                "Tránh đọc bằng phẳng như tiếng Việt 'hê-lô', cần nhấn vào âm HÊ.",
                "Mẹo nhớ: 'HÊ' bạn ơi, ra đây 'LÔ' diện nào!"
        ));

        TU_DIEN_BOI_CHUAN.put("hi", new HuongDanDocDTO(
                "hi", "/haɪ/", "xin chào",
                List.of("hi (HAI)"),
                List.of("haɪ"),
                List.of("HAI"),
                0,
                "HAI",
                "Từ có 1 âm tiết, đọc rõ âm đôi /aɪ/ (kéo từ 'a' sang 'i').",
                "Hơi thở đẩy nhẹ qua kẽ răng tạo âm /h/, sau đó mở rộng miệng đọc 'a' rồi thu hẹp về 'i'.",
                "Không có phụ âm đuôi, kết thúc bằng nguyên âm mở.",
                "Tránh nuốt âm /h/ thành 'ai'.",
                "Mẹo nhớ: Giơ 2 ngón tay chào 'HAI'."
        ));

        TU_DIEN_BOI_CHUAN.put("lemon", new HuongDanDocDTO(
                "lemon", "/ˈlem.ən/", "quả chanh vàng",
                List.of("le (LE)", "mon (mần)"),
                List.of("lem", "ən"),
                List.of("LE", "mần"),
                0,
                "LE - mần",
                "Trọng âm rơi vào âm tiết thứ 1 (LE) -> đọc to và cao hơn âm 'mần'.",
                "Môi hơi bẹt sang hai bên khi phát âm /e/, khép môi nhẹ tạo âm /m/ rồi thả lỏng lưỡi đọc /ən/.",
                "Âm kết thúc bằng /n/ nhẹ ở vòm miệng.",
                "Người Việt hay đọc nhầm thành 'le-mon' (âm o) thay vì âm /ə/ (mần).",
                "Mẹo nhớ: Quả chanh vắt vào 'LE' lưỡi vì chua, ăn 'MẦN' chi nữa!"
        ));

        TU_DIEN_BOI_CHUAN.put("lettuce", new HuongDanDocDTO(
                "lettuce", "/ˈlet̬.ɪs/", "rau diếp, xà lách",
                List.of("let (LE)", "tuce (tịt-s)"),
                List.of("let", "ɪs"),
                List.of("LE", "tịt-s"),
                0,
                "LE - tịt - s",
                "Trọng âm rơi vào âm tiết thứ 1 (LE).",
                "Đầu lưỡi chạm nướu trên bật âm /t/, sau đó xì nhẹ hơi /s/ ở cuối.",
                "⚠️ Bắt buộc phải xì nhẹ âm /s/ ở cuối từ (tịt-s).",
                "Hay bị đọc sai thành 'lét-tu-xê' hoặc quên âm xì /s/ cuối.",
                "Mẹo nhớ: Ăn rau xà lách 'LE' lưỡi vì 'TỊT' ngòi!"
        ));

        TU_DIEN_BOI_CHUAN.put("apples", new HuongDanDocDTO(
                "apples", "/ˈæp.əlz/", "những quả táo",
                List.of("ap (ÉP)", "ples (pồ-z)"),
                List.of("æp", "əlz"),
                List.of("ÉP", "pồ-z"),
                0,
                "ÉP - pồ - z",
                "Trọng âm rơi vào âm 1 (ÉP) -> đọc to, cao, kéo dài.",
                "Miệng mở rộng theo chiều dọc để phát âm /æ/ (lai giữa e và a), khép môi bật /p/ rồi uốn lưỡi âm /l/.",
                "Âm đuôi /z/ rung nhẹ ở thanh quản vì sau nguyên âm /l/.",
                "Hay quên âm đuôi /z/ khi có 's' số nhiều.",
                "Mẹo nhớ: 'ÉP' quả táo lấy nước uống cùng bạn 'PỒ'."
        ));

        TU_DIEN_BOI_CHUAN.put("apple", new HuongDanDocDTO(
                "apple", "/ˈæp.əl/", "quả táo",
                List.of("ap (ÉP)", "ple (pồ)"),
                List.of("æp", "əl"),
                List.of("ÉP", "pồ"),
                0,
                "ÉP - pồ",
                "Trọng âm rơi vào âm tiết đầu (ÉP).",
                "Mở miệng to đọc âm /æ/, ngậm môi bật hơi /p/, đầu lưỡi chạm chân răng trên đọc âm /l/ nhẹ.",
                "Âm cuối là âm uốn lưỡi /l/ nhẹ (dark l).",
                "Tránh đọc cứng thành 'áp-pồ', hãy đọc âm /æ/ mềm mại: 'ép-pồ'.",
                "Mẹo nhớ: 'ÉP' nước trái cây 'PỒ' ơi!"
        ));

        TU_DIEN_BOI_CHUAN.put("beans", new HuongDanDocDTO(
                "beans", "/biːnz/", "các loại hạt đậu",
                List.of("beans (BIIN-z)"),
                List.of("biːnz"),
                List.of("BIIN-z"),
                0,
                "BIIN - z",
                "Từ có 1 âm tiết, nguyên âm /iː/ là âm 'i' dài -> cần kéo dài giọng hơn tiếng Việt.",
                "Khép hai môi bật âm /b/, kéo căng khóe miệng như đang mỉm cười để đọc /iː/, kết thúc rung nhẹ âm /z/.",
                "⚠️ Đuôi /z/ rung nhẹ, không được bỏ quên.",
                "Tránh đọc cộc lốc thành 'bin', phải đọc kéo dài 'biiin-z'.",
                "Mẹo nhớ: Hạt đậu thần của bác 'BIIN' (Bean) cười 'Z'ui vẻ!"
        ));

        TU_DIEN_BOI_CHUAN.put("cabbage", new HuongDanDocDTO(
                "cabbage", "/ˈkæb.ɪdʒ/", "bắp cải",
                List.of("cab (KÉP)", "bage (bích)"),
                List.of("kæb", "ɪdʒ"),
                List.of("KÉP", "bích"),
                0,
                "KÉP - bích",
                "Trọng âm rơi vào âm tiết 1 (KÉP).",
                "Bật hơi /k/ ở cuống họng, mở miệng /æ/, ngậm môi /b/ rồi chu môi bật âm /dʒ/ (giống 'ch' tiếng Việt nhưng rung thanh quản).",
                "⚠️ Âm đuôi /dʒ/ cần chu nhẹ môi và bật hơi dứt khoát.",
                "Hay bị đọc thành 'cáp-ba-ge' theo mặt chữ.",
                "Mẹo nhớ: Bắp cải to quá phải 'KÉP' lại cho vào 'BÍCH' (bịch) mang về."
        ));

        TU_DIEN_BOI_CHUAN.put("garlic", new HuongDanDocDTO(
                "garlic", "/ˈɡɑːr.lɪk/", "củ tỏi",
                List.of("gar (GAA)", "lic (lịk)"),
                List.of("ɡɑːr", "lɪk"),
                List.of("GAA", "lịk"),
                0,
                "GAA - lịk",
                "Trọng âm rơi vào âm 1 (GAA) -> đọc to, trầm và ngân dài.",
                "Mở vòm họng tròn sâu phát âm /ɑː/, cong nhẹ lưỡi nếu theo giọng Mỹ, chuyển sang âm /lɪk/ kết thúc bằng ngắt hơi /k/.",
                "Âm đuôi /k/ ngắt hơi sắc ở cuống họng.",
                "Tránh đọc thành 'gát-lích' phẳng giọng.",
                "Mẹo nhớ: Ăn củ tỏi cay quá kêu 'GAA' lên, mắt 'LỊK' đi vì cay!"
        ));

        TU_DIEN_BOI_CHUAN.put("comfortable", new HuongDanDocDTO(
                "comfortable", "/ˈkʌm.fɚ.t̬ə.bəl/", "thoải mái, tiện nghi",
                List.of("com (KĂM)", "for (phơ)", "ta (tờ)", "ble (bồ)"),
                List.of("kʌm", "fɚ", "t̬ə", "bəl"),
                List.of("KĂM", "phơ", "tờ", "bồ"),
                0,
                "KĂM - phơ - tờ - bồ",
                "Trọng âm rơi vào âm 1 (KĂM), các âm sau đọc lướt nhanh và nhẹ.",
                "Bật hơi /k/ ở họng, răng trên chạm môi dưới phát âm /f/, kết thúc bằng âm /bəl/ nhẹ.",
                "Âm kết thúc /bəl/ phát âm lướt nhẹ, không nhấn.",
                "Lỗi rất phổ biến: Đọc 4 âm riêng 'côm-pho-tây-bồ' thay vì 3 hoặc 4 âm chuẩn 'KĂM-phơ-tờ-bồ' hoặc 'KĂM-tờ-bồ'.",
                "Mẹo nhớ: 'KĂM' (cầm) túi nước xả 'PHƠ' (Comfort) nằm trên giường thật thoải mái 'TỜ - BỒ'!"
        ));

        TU_DIEN_BOI_CHUAN.put("beautiful", new HuongDanDocDTO(
                "beautiful", "/ˈbjuː.t̬ə.fəl/", "xinh đẹp",
                List.of("beau (BIU)", "ti (ti)", "ful (phun)"),
                List.of("bjuː", "t̬ə", "fəl"),
                List.of("BIU", "ti", "phun"),
                0,
                "BIU - ti - phun",
                "Trọng âm rơi vào âm tiết đầu tiên (BIU).",
                "Khép môi bật /b/, chu môi đọc âm dài /juː/, âm /t/ có thể đọc nhẹ như 'ti' hoặc 'đờ' (Mỹ), đuôi /fəl/ đọc như 'phun'.",
                "Đuôi /l/ uốn nhẹ đầu lưỡi chạm chân răng trên.",
                "Tránh đọc 'beo-ti-phun' theo chữ viết.",
                "Mẹo nhớ: 'BIU' (View) cảnh biển này 'TI'ệt đẹp, quá 'PHUN' (Full) sắc màu!"
        ));
    }

    private HuongDanDocDTO taoHuongDanDocDuPhong(String tu, String phienAm, String nghia) {
        String tuLower = tu.toLowerCase().trim();

        if (TU_DIEN_BOI_CHUAN.containsKey(tuLower)) {
            HuongDanDocDTO mau = TU_DIEN_BOI_CHUAN.get(tuLower);
            return new HuongDanDocDTO(
                    tu,
                    phienAm != null && !phienAm.isBlank() ? phienAm : mau.getPhienAm(),
                    nghia != null && !nghia.isBlank() ? nghia : mau.getNghia(),
                    mau.getAmTiet(),
                    mau.getAmTietIpa(),
                    mau.getAmTietBoi(),
                    mau.getAmNhanIndex(),
                    mau.getPhienAmTiengViet(),
                    mau.getTrongAm(),
                    mau.getKhauHinh(),
                    mau.getAmDuoi(),
                    mau.getLoiThuongGap(),
                    mau.getMeoGhiNho()
            );
        }

        // Tách âm tiết heuristic thông minh
        List<String> rawSyllables = tachAmTietHeuristic(tuLower);
        List<String> amTiet = new ArrayList<>();
        List<String> amTietIpa = new ArrayList<>();
        List<String> amTietBoi = new ArrayList<>();
        List<String> boiList = new ArrayList<>();

        int amNhan = 0; // Mặc định âm 1

        for (int i = 0; i < rawSyllables.size(); i++) {
            String syl = rawSyllables.get(i);
            String boi = chuyenAmSangTiengViet(syl);
            if (i == amNhan) {
                boi = boi.toUpperCase();
            } else {
                boi = boi.toLowerCase();
            }
            boiList.add(boi);
            amTietBoi.add(boi);
            amTietIpa.add(syl);
            amTiet.add(syl + " (" + boi + ")");
        }

        String phienAmViet = String.join(" - ", boiList);
        String ipaHienThi = (phienAm != null && !phienAm.isBlank()) ? phienAm : "/" + tuLower + "/";
        String nghiaHienThi = (nghia != null && !nghia.isBlank()) ? nghia : "";

        String amCuoi = tuLower.length() > 0 ? String.valueOf(tuLower.charAt(tuLower.length() - 1)) : "";
        String chuYAmDuoi = switch (amCuoi) {
            case "s" -> "⚠️ Bắt buộc xì nhẹ âm đuôi /s/ ở cuối từ.";
            case "t" -> "⚠️ Bật nhẹ đầu lưỡi ở chân răng trên để tạo âm bật /t/ dứt khoát.";
            case "d" -> "⚠️ Chặn nhẹ đầu lưỡi và rung thanh quản cho âm /d/.";
            case "k", "c" -> "⚠️ Bật hơi dứt khoát âm /k/ ở cuống họng.";
            default -> "Đọc liền mạch, không nuốt âm nguyên âm cuối.";
        };

        return new HuongDanDocDTO(
                tu,
                ipaHienThi,
                nghiaHienThi,
                amTiet,
                amTietIpa,
                amTietBoi,
                amNhan,
                phienAmViet,
                "Trọng âm rơi vào âm tiết đầu (" + (boiList.isEmpty() ? tu : boiList.get(0)) + ") - đọc to và cao hơn các âm còn lại.",
                "Mở khẩu hình tự nhiên, thả lỏng cơ hàm và lấy hơi từ bụng để giọng phát âm dày và vang.",
                chuYAmDuoi,
                "Người Việt hay có thói quen đọc phẳng các âm tiết bằng nhau, hãy nhớ nhấn mạnh vào âm có trọng âm!",
                "Mẹo nhỏ: Hãy chia từ làm từng khúc, luyện đọc to từng âm tiết rồi ráp lại nhanh dần."
        );
    }

    private List<String> tachAmTietHeuristic(String word) {
        List<String> syllables = new ArrayList<>();
        if (word.length() <= 3) {
            syllables.add(word);
            return syllables;
        }

        String pattern = "(?<=[aeiouy])(?=[bcdfghjklmnpqrstvwxz][aeiouy])";
        String[] parts = word.split(pattern);

        if (parts.length > 0) {
            for (String p : parts) {
                if (!p.isBlank()) syllables.add(p);
            }
        }
        if (syllables.isEmpty()) {
            syllables.add(word);
        }
        return syllables;
    }

    private String chuyenAmSangTiengViet(String syl) {
        String s = syl.toLowerCase();
        if (s.equals("hel")) return "hê";
        if (s.equals("lo")) return "lô";
        if (s.equals("com")) return "kăm";
        if (s.equals("for")) return "phơ";
        if (s.equals("ta")) return "tờ";
        if (s.equals("ble")) return "bồ";
        if (s.equals("ap") || s.equals("app")) return "ép";
        if (s.equals("ple") || s.equals("ples")) return "pồ";
        if (s.equals("gar")) return "gaa";
        if (s.equals("lic")) return "lịk";
        if (s.equals("cab")) return "kép";
        if (s.equals("bage")) return "bích";
        if (s.equals("let")) return "le";
        if (s.equals("tuce")) return "tịt";
        if (s.equals("le")) return "le";
        if (s.equals("mon")) return "mần";
        if (s.equals("tion")) return "sần";
        if (s.equals("ment")) return "mừn";
        if (s.equals("ing")) return "ing";

        // Quy tắc chuyển đổi vần tiếng Anh sang bồi tiếng Việt
        s = s.replaceAll("tion", "sần")
                .replaceAll("sion", "sần")
                .replaceAll("ment", "mừn")
                .replaceAll("ing", "ing")
                .replaceAll("ph", "f")
                .replaceAll("th", "th")
                .replaceAll("ch", "ch")
                .replaceAll("sh", "s")
                .replaceAll("ee|ea", "ii")
                .replaceAll("oo", "uu")
                .replaceAll("ai|ay", "ay")
                .replaceAll("oi|oy", "oi")
                .replaceAll("ou|ow", "ao")
                .replaceAll("ar", "aa")
                .replaceAll("or", "oo")
                .replaceAll("er|ur|ir", "ơ")
                .replaceAll("c(?=[eiy])", "s")
                .replaceAll("c", "k")
                .replaceAll("x", "ks")
                .replaceAll("w", "u");

        return s;
    }
}
