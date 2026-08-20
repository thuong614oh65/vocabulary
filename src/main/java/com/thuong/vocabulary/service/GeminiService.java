package com.thuong.vocabulary.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiService {

    private final Client client;

    public GeminiService() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey != null && !apiKey.isBlank()) {
            this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        } else {
            System.err.println("[GeminiService] CANH BAO: GEMINI_API_KEY chua duoc thiet lap.");
            this.client = null;
        }
    }

    // =====================================================
    // TAO DOAN VAN
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


        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        return response.text();
    }

    // =====================================================
    // KIEM TRA BAN DICH
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

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.6-flash",
                        prompt,
                        null
                );

        return response.text();
    }

    // =====================================================
    // TAO DOAN VAN DIEN VAO CHO TRONG (CLOZE TEST)
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

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        return response.text();
    }

    // =====================================================
    // TAO CAC CAU LUYEN NGHE DIEN (DICTATION)
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

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        return response.text();
    }

    // =====================================================
    // CHAM DIEM PHAT AM QUA AUDIO FILE BANG GEMINI MULTIMODAL
    // Chuan IELTS / CEFR / Forvo - 5 tieu chi quoc te
    // =====================================================
    public String chamDiemPhatAmAudio(byte[] audioBytes, String contentType, String tuGoc, String phienAm) {
        if (client == null) {
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

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3.6-flash",
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
                result = result.trim();
            }
            return result;
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