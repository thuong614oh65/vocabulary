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
                Bạn là giáo viên tiếng Anh.

                Hãy viết một đoạn văn tiếng Anh tự nhiên,
                dễ hiểu và phù hợp để người học tiếng Anh luyện dịch.

                YÊU CẦU:

                1. Bắt buộc sử dụng càng nhiều từ trong danh sách
                   được cung cấp càng tốt.
                2. Cố gắng sử dụng ít nhất 10 từ trong danh sách
                   nếu danh sách có đủ từ.
                3. Có thể thêm các từ tiếng Anh cơ bản khác
                   để câu văn tự nhiên.
                4. Không cần sử dụng tất cả các từ nếu việc sử dụng
                   làm đoạn văn trở nên không tự nhiên.
                5. Độ dài khoảng 100 đến 150 từ.
                6. Mỗi lần tạo phải viết một đoạn văn khác nhau.
                7. Chỉ trả về đoạn văn tiếng Anh.
                8. Không giải thích, không dịch sang tiếng Việt.
                9. Không đánh số.

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
}