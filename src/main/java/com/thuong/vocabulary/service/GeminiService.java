package com.thuong.vocabulary.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GeminiService {

    // Danh sách các Client tương ứng với từng API Key trong Pool
    private final List<Client> clients = new ArrayList<>();
    private final List<String> maskedKeys = new ArrayList<>();
    private final AtomicInteger keyIndex = new AtomicInteger(0);

    // Các model được hỗ trợ trên hệ thống theo thứ tự ưu tiên
    private static final String MODEL_FLASH_LITE = "gemini-3.5-flash-lite";
    private static final String MODEL_FLASH = "gemini-3.6-flash";

    public GeminiService(@Value("${gemini.api-keys:${gemini.api-key:}}") String configApiKeys) {
        Set<String> uniqueKeys = new LinkedHashSet<>();

        // 1. Đọc từ biến môi trường GEMINI_API_KEY & GEMINI_API_KEYS
        collectKeys(uniqueKeys, System.getenv("GEMINI_API_KEY"));
        collectKeys(uniqueKeys, System.getenv("GEMINI_API_KEYS"));

        // 2. Đọc từ application.properties
        collectKeys(uniqueKeys, configApiKeys);

        for (String rawKey : uniqueKeys) {
            String trimmedKey = rawKey.trim();
            if (!trimmedKey.isEmpty()) {
                try {
                    Client client = Client.builder()
                            .apiKey(trimmedKey)
                            .build();
                    clients.add(client);
                    maskedKeys.add(maskKey(trimmedKey));
                } catch (Exception e) {
                    System.err.println("[GeminiService] Lỗi khi khởi tạo Gemini Client với key: "
                            + maskKey(trimmedKey) + " - " + e.getMessage());
                }
            }
        }

        if (clients.isEmpty()) {
            System.err.println("[GeminiService] ⚠️ CẢNH BÁO: Chưa cấu hình GEMINI_API_KEY. Vui lòng thiết lập ít nhất 1 API key.");
        } else {
            System.out.println("=====================================================");
            System.out.printf("[GeminiService] ✅ ĐÃ KHỞI TẠO THÀNH CÔNG %d GEMINI API KEY(S) TRONG POOL:%n", clients.size());
            for (int i = 0; i < maskedKeys.size(); i++) {
                System.out.printf("  👉 Key #%d: %s%n", i + 1, maskedKeys.get(i));
            }
            System.out.printf("  🤖 Model chính: %s | Model dự phòng: %s%n", MODEL_FLASH_LITE, MODEL_FLASH);
            System.out.println("=====================================================");
        }
    }

    private void collectKeys(Set<String> destination, String rawKeys) {
        if (rawKeys == null || rawKeys.isBlank()) {
            return;
        }
        String[] tokens = rawKeys.split("[,;\\n\\r]+");
        for (String token : tokens) {
            String k = token.trim();
            if (!k.isEmpty()) {
                destination.add(k);
            }
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    // =========================================================
    // HÀM GỌI GEMINI AN TOÀN VỚI TỰ ĐỘNG XOAY VÒNG KEY & FALLBACK MODEL
    // =========================================================
    private String goiGeminiAnToan(String prompt, String[] danhSachModel) throws Exception {
        if (clients.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY chưa được thiết lập trên server.");
        }

        int tongSoClient = clients.size();
        Exception loiCuoiCung = null;

        // Thử lần lượt từng model
        for (String modelName : danhSachModel) {
            // Với mỗi model, thử qua toàn bộ các API Key trong pool
            for (int attempt = 0; attempt < tongSoClient; attempt++) {
                int index = keyIndex.getAndUpdate(i -> (i + 1) % tongSoClient);
                Client currentClient = clients.get(index);
                String currentMasked = maskedKeys.get(index);

                try {
                    GenerateContentResponse response = currentClient.models.generateContent(
                            modelName,
                            prompt,
                            null
                    );

                    if (response != null && response.text() != null && !response.text().isBlank()) {
                        return response.text();
                    }
                } catch (Exception e) {
                    loiCuoiCung = e;
                    String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                    System.err.printf("[GeminiService] Key #%d (%s) với model '%s' gặp lỗi: %s%n",
                            index + 1, currentMasked, modelName, msg);

                    if (tongSoClient > 1) {
                        System.out.printf("[GeminiService] 🔄 Đang tự động chuyển sang Key tiếp theo trong pool...%n");
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        // Nếu tất cả các key và model đều tạm thời quá tải, chờ 1 giây và thử lại lần cuối với model chính
        if (tongSoClient > 0) {
            try {
                System.out.println("[GeminiService] ⏳ Đang chờ 1 giây để thử lại lần cuối...");
                Thread.sleep(1000);
                int retryIndex = keyIndex.getAndUpdate(i -> (i + 1) % tongSoClient);
                GenerateContentResponse response = clients.get(retryIndex).models.generateContent(
                        danhSachModel[0],
                        prompt,
                        null
                );
                if (response != null && response.text() != null && !response.text().isBlank()) {
                    return response.text();
                }
            } catch (Exception retryEx) {
                loiCuoiCung = retryEx;
            }
        }

        throw loiCuoiCung != null ? loiCuoiCung : new RuntimeException("Tất cả Gemini API Key và Model đều không phản hồi.");
    }

    // =====================================================
    // 1. TẠO ĐOẠN VĂN LUYỆN DỊCH
    // =====================================================
    public String taoDoanVan(String danhSachTu) {
        String prompt = """
                Ban la giao vien tieng Anh ban xu.
                Hay viet mot doan van tieng Anh ngan gon, tu nhien, troi chay va co y nghia mach lac (khoang 80 den 140 tu) de nguoi hoc luyen dich.

                YEU CAU QUAN TRONG:
                1. MUC TIEU HOC TAP: Uu tien su dung cac tu vung duoc cung cap trong danh sach cua nguoi hoc ben duoi.
                2. NGU PHAP VA NGU CANH: Doan van phai chuan 100% ngu phap tieng Anh, cau van logic, tu nhien. Tuyet doi khong ghep tu guong ep khien cau vo nghia.
                3. BO SUNG TU LINH HOAT: Neu danh sach tu vung it, hay linh hoat them cac tu ngu thong dung, tu nhien.
                4. Do dai: 80 - 140 tu.
                5. Chi tra ve duy nhat doan van tieng Anh, khong giai thich, khong dich, khong danh so.

                Danh sach tu vung cua nguoi hoc:
                """ + danhSachTu;

        try {
            return goiGeminiAnToan(prompt, new String[]{MODEL_FLASH_LITE, MODEL_FLASH});
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo đoạn văn: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // 2. KIỂM TRA BẢN DỊCH
    // =====================================================
    public String kiemTraBanDich(String doanVan, String banDich) {
        String prompt = """
            Ban la giao vien tieng Anh dang cham bai dich cho mot nguoi Viet Nam hoc tieng Anh.

            Hay danh gia ban dich tieng Viet cua nguoi hoc dua tren doan van tieng Anh goc.

            DOAN VAN TIENG ANH:
            
            %s

            BAN DICH CUA NGUOI HOC:

            %s

            Hay danh gia theo cac yeu cau sau:
            1. Kiem tra xem ban dich co truyen dat dung y nghia cua doan van tieng Anh hay khong.
            2. Khong yeu cau ban dich phai giong tung chu voi ban dich mau.
            3. Neu nguoi hoc dung cach dien dat tieng Viet khac nhung van dung nghia thi coi la dung.
            4. Phan biet ro: Dung / Gan dung / Sai hoac thieu y
            5. Neu co loi, hay chi ra nhung y bi sai, thieu hoac dich chua chinh xac.
            6. Dua ra ban dich tieng Viet tu nhien, chinh xac de nguoi hoc tham khao.
            7. Dua ra goi y sua ngan gon, de hieu cho nguoi hoc.

            Hay tra ket qua theo dung dinh dang:

            DANH GIA:
            [Dung / Gan dung / Sai hoac thieu y]

            NHAN XET:
            [Nhan xet ngan gon]

            LOI HOAC Y THIEU:
            [Cac loi hoac y bi thieu]

            BAN DICH GOI Y:
            [Ban dich tieng Viet chinh xac va tu nhien]

            GOI Y CAI THIEN:
            [Goi y ngan gon]

            Khong giai thich them ngoai cac muc tren.
            """.formatted(doanVan, banDich);

        try {
            return goiGeminiAnToan(prompt, new String[]{MODEL_FLASH, MODEL_FLASH_LITE});
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kiểm tra bản dịch: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // 3. TẠO ĐOẠN VĂN ĐIỀN VÀO CHỖ TRỐNG (CLOZE TEST)
    // =====================================================
    public String taoDoanVanDienTu(String danhSachTu) {
        String prompt = """
                Ban la giao vien tieng Anh ban xu.
                Hay viet mot doan van tieng Anh tu nhien, co cot truyen hoac ngu canh doi song thuc te ro rang (khoang 80 - 140 tu) de tao bai tap "Dien tu vao cho trong".

                QUY TAC BAT BUOC:
                1. MUC TIEU HOC TAP: Chon khoang 4 den 10 tu vung phu hop nhat trong danh sach cua nguoi hoc de dat vao dung ngu canh tu nhien cua cau.
                2. CHUAN NGU PHAP VA TU NHIEN: Doan van phai dung ngu phap tieng Anh, logic va de hieu. Linh hoat them cac tu ngu quen thuoc ben ngoai de cau van tron ven, khong nhoi nhet tu guong ep.
                3. TAO THE CHO TRONG: Tai vi tri moi tu vung duoc chon, thay the bang the chinh xac:
                   [[BLANK:so_thu_tu:tu_tieng_anh_goc:nghia_tieng_viet]]
                   Vi du: "Every morning, I drink a cup of [[BLANK:1:coffee:ca phe]] before going to [[BLANK:2:school:truong hoc]]."
                4. So thu tu bat dau tu 1 va tang dan: 1, 2, 3...
                5. Tu tieng Anh trong the co the chia thi/dang so nhieu phu hop ngu canh.
                6. BAN DICH TIENG VIET: O cuoi bai, hay them ban dich tieng Viet hoan chinh dat trong khoi:
                   [DICH_TIENG_VIET]
                   (Ban dich tieng Viet chinh xac va tu nhien cua doan van)
                   [/DICH_TIENG_VIET]
                7. Chi tra ve duy nhat doan van tieng Anh co chua cac the [[BLANK:...]] va khoi [DICH_TIENG_VIET], khong them loi chao hay giai thich nao khac.

                Danh sach tu vung cua nguoi hoc:
                """ + danhSachTu;

        try {
            return goiGeminiAnToan(prompt, new String[]{MODEL_FLASH_LITE, MODEL_FLASH});
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo bài tập điền từ: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // 4. TẠO CÁC CÂU LUYỆN NGHE ĐIỀN (DICTATION)
    // =====================================================
    public String taoCauNgheDien(String danhSachTu, int soCau) {
        String prompt = """
                Ban la giao vien tieng Anh ban xu.
                Hay tao chinh xac %d cau tieng Anh ngan gon, tu nhien, thiet thuc (moi cau tu 5 den 12 tu) de nguoi hoc luyen nghe va chep chinh ta (Dictation).

                QUY TAC BAT BUOC:
                1. MUC TIEU HOC TAP: Moi cau phai long ghep kheo leo it nhat 1 tu vung trong danh sach cua nguoi hoc ben duoi.
                2. CHUAN NGU PHAP VA DUNG NGU CANH: Cau van phai hoan toan tu nhien, chuan xac ngu phap tieng Anh. Tuyet doi khong ghep tu vo nghia.
                3. LINH HOAT THEM TU: Neu danh sach tu vung it, hay bo sung cac tu ngu quen thuoc, tu nhien.
                4. Tao dung %d cau (danh so tu 1 den %d).
                5. Tra ve dung dinh dang tung dong:
                   [CAU:so_thu_tu:cau_tieng_anh:nghia_tieng_viet]
                   Vi du:
                   [CAU:1:I enjoy drinking hot coffee every morning.:Toi thich uong ca phe nong moi sang.]
                6. Chi tra ve danh sach cac dong [CAU:...], khong them bat ky loi chao hay giai thich nao khac.

                Danh sach tu vung cua nguoi hoc:
                %s
                """.formatted(soCau, soCau, soCau, danhSachTu);

        try {
            return goiGeminiAnToan(prompt, new String[]{MODEL_FLASH_LITE, MODEL_FLASH});
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo câu nghe điền: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // 5. CHẤM ĐIỂM PHÁT ÂM QUA AUDIO FILE BẰNG GEMINI MULTIMODAL
    // Chuan IELTS / CEFR / Forvo - 5 tiêu chí quốc tế
    // =====================================================
    public String chamDiemPhatAmAudio(byte[] audioBytes, String contentType, String tuGoc, String phienAm) {
        if (clients.isEmpty()) {
            return "{\"score\":0,\"status\":\"error\",\"recognizedText\":\"\",\"ipaRecognized\":\"\",\"ipaTarget\":"
                + "\"\",\"breakdown\":{\"phonemeAccuracy\":0,\"stress\":0,\"vowelQuality\":0,\"consonantClarity\":0,\"fluency\":0},"
                + "\"phonemeDetails\":[],\"correctParts\":[],\"incorrectParts\":[],"
                + "\"feedback\":\"GEMINI_API_KEY chua duoc thiet lap.\","
                + "\"suggestion\":\"Vui long cau hinh GEMINI_API_KEY tren server.\","
                + "\"encouragement\":\"\"}";
        }

        String mimeType = "audio/webm";
        if (contentType != null && !contentType.isBlank() && !contentType.equals("application/octet-stream")) {
            mimeType = contentType.split(";")[0].trim().toLowerCase();
        }

        String tuMucTieu = tuGoc != null ? tuGoc : "";
        String phienAmChuan = phienAm != null ? phienAm : "";

        String prompt = "You are an international English phonetics expert and IELTS/CEFR Speaking Examiner.\n\n"
            + "TASK: Listen to the audio and evaluate pronunciation of the target English word/phrase.\n"
            + "- TARGET WORD: \"" + tuMucTieu + "\"\n"
            + "- STANDARD IPA: \"" + phienAmChuan + "\"\n\n"
            + "STRICT EVALUATION STEPS:\n\n"
            + "STEP 1 - SPEECH RECOGNITION:\n"
            + "- recognizedText: exactly what the learner said\n"
            + "- ipaRecognized: convert the learner speech to IPA notation\n"
            + "- ipaTarget: the correct IPA of the target word\n\n"
            + "STEP 2 - SCORE 5 CRITERIA (total 100 points):\n\n"
            + "1. phonemeAccuracy (0-40 pts): Accuracy of each phoneme/sound\n"
            + "   36-40: All phonemes correct | 28-35: 1-2 minor errors | 16-27: Multiple errors | 0-15: Completely wrong\n\n"
            + "2. stress (0-20 pts): Correct stress placement and intensity\n"
            + "   18-20: Native-like | 13-17: Recognizable | 7-12: Wrong position | 0-6: Flat/unrecognizable\n\n"
            + "3. vowelQuality (0-20 pts): Vowel quality (tense/lax, length, diphthongs)\n"
            + "   18-20: Accurate | 13-17: Near-correct | 7-12: Affects meaning | 0-6: Completely wrong\n\n"
            + "4. consonantClarity (0-10 pts): Clear consonants, no dropped final consonants\n"
            + "   9-10: All clear | 6-8: Slight dropping | 3-5: Noticeable dropping | 0-2: Missing sounds\n\n"
            + "5. fluency (0-10 pts): Natural delivery, appropriate pace\n"
            + "   9-10: Natural and smooth | 6-8: Fairly natural | 3-5: Hesitant | 0-2: Very disrupted\n\n"
            + "STEP 3 - PHONEME ANALYSIS (phonemeDetails array):\n"
            + "For EACH phoneme in the target word, provide:\n"
            + "- symbol: IPA symbol\n"
            + "- word: corresponding English letter(s)\n"
            + "- status: 'correct', 'incorrect', or 'missing'\n"
            + "- note: brief explanation if incorrect (empty string if correct)\n\n"
            + "STEP 4 - FEEDBACK IN VIETNAMESE:\n"
            + "- feedback: Specific feedback in Vietnamese on correct/wrong aspects\n"
            + "- suggestion: SPECIFIC mouth/tongue/lip guidance in Vietnamese to fix errors\n"
            + "- encouragement: One short encouraging sentence in Vietnamese\n\n"
            + "CALCULATE:\n"
            + "score = phonemeAccuracy + stress + vowelQuality + consonantClarity + fluency (max 100)\n"
            + "status: 90-100=\"excellent\" | 75-89=\"good\" | 50-74=\"average\" | 0-49=\"poor\"\n\n"
            + "RETURN ONLY VALID JSON. No markdown, no explanation outside JSON:\n"
            + "{\n"
            + "  \"score\": 85,\n"
            + "  \"status\": \"good\",\n"
            + "  \"recognizedText\": \"word heard\",\n"
            + "  \"ipaRecognized\": \"IPA of what was heard\",\n"
            + "  \"ipaTarget\": \"correct IPA\",\n"
            + "  \"breakdown\": {\n"
            + "    \"phonemeAccuracy\": 34,\n"
            + "    \"stress\": 17,\n"
            + "    \"vowelQuality\": 16,\n"
            + "    \"consonantClarity\": 9,\n"
            + "    \"fluency\": 9\n"
            + "  },\n"
            + "  \"phonemeDetails\": [\n"
            + "    {\"symbol\": \"ae\", \"word\": \"a\", \"status\": \"correct\", \"note\": \"\"},\n"
            + "    {\"symbol\": \"p\", \"word\": \"p\", \"status\": \"incorrect\", \"note\": \"Bi nuot am cuoi, can bat nhe moi\"}\n"
            + "  ],\n"
            + "  \"correctParts\": [\"am dung\"],\n"
            + "  \"incorrectParts\": [\"am sai\"],\n"
            + "  \"feedback\": \"Nhan xet chi tiet bang tieng Viet\",\n"
            + "  \"suggestion\": \"Huong dan khau hinh bang tieng Viet\",\n"
            + "  \"encouragement\": \"Loi dong vien ngan gon\"\n"
            + "}";

        try {
            Part audioPart = Part.fromBytes(audioBytes, mimeType);
            Part promptPart = Part.fromText(prompt);

            Content content = Content.builder()
                    .parts(List.of(audioPart, promptPart))
                    .build();

            int tongSoClient = clients.size();
            String[] models = new String[]{MODEL_FLASH, MODEL_FLASH_LITE};
            Exception lastAudioEx = null;

            for (String modelName : models) {
                for (int attempt = 0; attempt < tongSoClient; attempt++) {
                    int index = keyIndex.getAndUpdate(i -> (i + 1) % tongSoClient);
                    Client currentClient = clients.get(index);
                    String currentMasked = maskedKeys.get(index);

                    try {
                        GenerateContentResponse response = currentClient.models.generateContent(
                                modelName,
                                content,
                                null
                        );

                        String result = response.text();
                        if (result != null) {
                            result = result.trim();
                            if (result.startsWith("```json")) {
                                result = result.substring(7);
                            } else if (result.startsWith("```")) {
                                result = result.substring(3);
                            }
                            if (result.endsWith("```")) {
                                result = result.substring(0, result.length() - 3);
                            }
                            return result.trim();
                        }
                    } catch (Exception e) {
                        lastAudioEx = e;
                        System.err.printf("[GeminiService] Audio - Key #%d (%s) model '%s' gặp sự cố: %s%n",
                                index + 1, currentMasked, modelName, e.getMessage());

                        if (tongSoClient > 1) {
                            System.out.println("[GeminiService] 🔄 Đang tự động chuyển sang Key tiếp theo cho Audio...");
                        }

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException ignored) {}
                    }
                }
            }

            throw lastAudioEx != null ? lastAudioEx : new RuntimeException("Tất cả key đều không chấm được âm thanh.");

        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Unknown error";
            return "{\"score\":0,\"status\":\"error\",\"recognizedText\":\"\",\"ipaRecognized\":\"\",\"ipaTarget\":\"\","
                + "\"breakdown\":{\"phonemeAccuracy\":0,\"stress\":0,\"vowelQuality\":0,\"consonantClarity\":0,\"fluency\":0},"
                + "\"phonemeDetails\":[],\"correctParts\":[],\"incorrectParts\":[],"
                + "\"feedback\":\"Loi cham diem: " + errMsg + "\","
                + "\"suggestion\":\"Vui long thu lai sau giay lat.\","
                + "\"encouragement\":\"Dung nan long, hay thu lai nhe!\"}";
        }
    }
}