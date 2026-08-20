package com.thuong.vocabulary.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    public GeminiService() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY chưa được thiết lập."
            );
        }

        client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    // =====================================================
    // TẠO ĐOẠN VĂN
    // =====================================================

    public String taoDoanVan(String danhSachTu) {

        String prompt = """
                Bạn là giáo viên tiếng Anh bản xứ.
                Hãy viết một đoạn văn tiếng Anh ngắn gọn, tự nhiên, trôi chảy và có ý nghĩa mạch lạc (khoảng 80 đến 140 từ) để người học luyện dịch.

                YÊU CẦU QUAN TRỌNG:
                1. MỤC TIÊU HỌC TẬP: Ưu tiên sử dụng các từ vựng được cung cấp trong danh sách của người học bên dưới.
                2. NGỮ PHÁP & NGỮ CẢNH: Đoạn văn phải chuẩn 100% ngữ pháp tiếng Anh, câu văn logic, tự nhiên như văn phong đời sống thực tế. Tuyệt đối không ghép từ gượng ép khiến câu vô nghĩa.
                3. BỔ SUNG TỪ LINH HOẠT: Nếu danh sách từ vựng ít hoặc khó ghép lại với nhau, hãy linh hoạt thêm các từ ngữ thông dụng, tự nhiên để đoạn văn hoàn chỉnh và giàu ý nghĩa.
                4. Độ dài: 80 - 140 từ.
                5. Chỉ trả về duy nhất đoạn văn tiếng Anh, không giải thích, không dịch, không đánh số.

                Danh sách từ vựng của người học:
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
    // KIỂM TRA BẢN DỊCH
    // =====================================================

    public String kiemTraBanDich(
            String doanVan,
            String banDich) {

        String prompt = """
            Bạn là giáo viên tiếng Anh đang chấm bài dịch
            cho một người Việt Nam học tiếng Anh.

            Hãy đánh giá bản dịch tiếng Việt của người học
            dựa trên đoạn văn tiếng Anh gốc.

            ĐOẠN VĂN TIẾNG ANH:
            
            %s

            BẢN DỊCH CỦA NGƯỜI HỌC:

            %s

            Hãy đánh giá theo các yêu cầu sau:

            1. Kiểm tra xem bản dịch có truyền đạt đúng
               ý nghĩa của đoạn văn tiếng Anh hay không.

            2. Không yêu cầu bản dịch phải giống từng chữ
               với bản dịch mẫu.

            3. Nếu người học dùng cách diễn đạt tiếng Việt
               khác nhưng vẫn đúng nghĩa thì coi là đúng.

            4. Phân biệt rõ:
               - Đúng
               - Gần đúng
               - Sai hoặc thiếu ý

            5. Nếu có lỗi, hãy chỉ ra những ý bị sai,
               thiếu hoặc dịch chưa chính xác.

            6. Đưa ra bản dịch tiếng Việt tự nhiên,
               chính xác để người học tham khảo.

            7. Đưa ra gợi ý sửa ngắn gọn,
               dễ hiểu cho người học.

            Hãy trả kết quả theo đúng định dạng:

            ĐÁNH GIÁ:
            [Đúng / Gần đúng / Sai hoặc thiếu ý]

            NHẬN XÉT:
            [Nhận xét ngắn gọn]

            LỖI HOẶC Ý THIẾU:
            [Các lỗi hoặc ý bị thiếu]

            BẢN DỊCH GỢI Ý:
            [Bản dịch tiếng Việt chính xác và tự nhiên]

            GỢI Ý CẢI THIỆN:
            [Gợi ý ngắn gọn]

            Không giải thích thêm ngoài các mục trên.
            """.formatted(
                doanVan,
                banDich
        );

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.6-flash",
                        prompt,
                        null
                );

        return response.text();
    }

    // =====================================================
    // TẠO ĐOẠN VĂN ĐIỀN VÀO CHỖ TRỐNG (CLOZE TEST)
    // =====================================================

    public String taoDoanVanDienTu(String danhSachTu) {

        String prompt = """
                Bạn là giáo viên tiếng Anh bản xứ.
                Hãy viết một đoạn văn tiếng Anh tự nhiên, có cốt truyện hoặc ngữ cảnh đời sống thực tế rõ ràng (khoảng 80 - 140 từ) để tạo bài tập "Điền từ vào chỗ trống".

                QUY TẮC BẮT BUỘC:
                1. MỤC TIÊU HỌC TẬP: Chọn khoảng 4 đến 10 từ vựng phù hợp nhất trong danh sách của người học để đặt vào đúng ngữ cảnh tự nhiên của câu.
                2. CHUẨN NGỮ PHÁP & TỰ NHIÊN: Đoạn văn phải đúng ngữ pháp tiếng Anh, logic và dễ hiểu. Linh hoạt thêm các từ ngữ quen thuộc bên ngoài để câu văn trọn vẹn, không nhồi nhét từ gượng ép.
                3. TẠO THẺ CHỖ TRỐNG: Tại vị trí mỗi từ vựng được chọn, thay thế bằng thẻ chính xác:
                   [[BLANK:số_thứ_tự:từ_tiếng_anh_gốc:nghĩa_tiếng_việt]]
                   Ví dụ: "Every morning, I drink a cup of [[BLANK:1:coffee:cà phê]] before going to [[BLANK:2:school:trường học]]."
                4. Số thứ tự bắt đầu từ 1 và tăng dần: 1, 2, 3...
                5. Từ tiếng Anh trong thẻ có thể chia thì/dạng số nhiều phù hợp ngữ cảnh (ví dụ: study hoặc studying).
                6. BẢN DỊCH TIẾNG VIỆT: Ở cuối bài, hãy thêm bản dịch tiếng Việt hoàn chỉnh của đoạn văn đặt trong khối:
                   [DỊCH_TIẾNG_VIỆT]
                   (Bản dịch tiếng Việt chính xác và tự nhiên của đoạn văn)
                   [/DỊCH_TIẾNG_VIỆT]
                7. Chỉ trả về duy nhất đoạn văn tiếng Anh có chứa các thẻ [[BLANK:...]] và khối [DỊCH_TIẾNG_VIỆT], không thêm lời chào hay giải thích nào khác.

                Danh sách từ vựng của người học:
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
    // TẠO CÁC CÂU LUYỆN NGHE ĐIỀN (DICTATION)
    // =====================================================

    public String taoCauNgheDien(String danhSachTu, int soCau) {

        String prompt = """
                Bạn là giáo viên tiếng Anh bản xứ.
                Hãy tạo chính xác %d câu tiếng Anh ngắn gọn, tự nhiên, thiết thực (mỗi câu từ 5 đến 12 từ) để người học luyện nghe và chép chính tả (Dictation).

                QUY TẮC BẮT BUỘC:
                1. MỤC TIÊU HỌC TẬP: Mỗi câu phải lồng ghép khéo léo ít nhất 1 từ vựng trong danh sách của người học bên dưới.
                2. CHUẨN NGỮ PHÁP & ĐÚNG NGỮ CẢNH: Câu văn phải hoàn toàn tự nhiên, chuẩn xác ngữ pháp tiếng Anh, mang ý nghĩa thực tế trong giao tiếp, sinh hoạt, học tập hoặc công việc hàng ngày. Tuyệt đối không ghép từ vô nghĩa.
                3. LINH HOẠT THÊM TỪ: Nếu danh sách từ vựng ít, hãy bổ sung các từ ngữ quen thuộc, tự nhiên để tạo thành câu văn hoàn chỉnh và hay nhất.
                4. Tạo đúng %d câu (đánh số từ 1 đến %d).
                5. Trả về đúng định dạng từng dòng:
                   [CÂU:số_thứ_tự:câu_tiếng_anh:nghĩa_tiếng_việt]
                   Ví dụ:
                   [CÂU:1:I enjoy drinking hot coffee every morning.:Tôi thích uống cà phê nóng mỗi sáng.]
                   [CÂU:2:She decided to buy a new laptop yesterday.:Cô ấy đã quyết định mua một chiếc máy tính xách tay mới vào hôm qua.]
                6. Chỉ trả về danh sách các dòng [CÂU:...], không thêm bất kỳ lời chào hay giải thích nào khác.

                Danh sách từ vựng của người học:
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
}