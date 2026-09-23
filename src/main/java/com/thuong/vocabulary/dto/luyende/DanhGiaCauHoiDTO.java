package com.thuong.vocabulary.dto.luyende;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DanhGiaCauHoiDTO {

    private int soThuTu; // 1, 2, 3
    private int thoiGianQuyDinh; // 15 hoặc 30
    private int soTu; // Số từ học viên đã viết
    private int thoiGianNoiUocTinh; // Ước tính số giây khi đọc to với tốc độ hành thường (130 wpm)

    private int diem; // Thang 0 - 3 (TOEIC standard)
    private String trangThai; // "DUNG", "GAN_DUNG", "THIEU_Y", "SAI"

    private String trichDanDeBai;   // Đoạn văn / dòng thông tin trích dẫn y nguyên từ đề bài dùng để trả lời câu này
    private String huongDanChemTu;  // Hướng dẫn cách chêm từ (chủ ngữ, giới từ, liên từ...) từ thông tin thô để tạo câu hoàn chỉnh
    private String suaCauNguoiDung; // Câu trả lời của học viên sau khi được AI sửa lỗi và hoàn thiện
    private String giaiThichSuaCau; // Giải thích chi tiết các điểm đã sửa từ câu của học viên

    private String danhGiaThongTin; // Đủ / Thiếu / Thừa thông tin
    private String danhGiaThoiGian; // Đánh giá độ dài & thời gian nói
    private String nhanXetChiTiet;  // Nhận xét ngữ pháp, giới từ, tính tự nhiên
    private String cauTraLoiMau;    // Câu trả lời chuẩn bản xứ để học viên nói
    private String dichTiengVietMau;// Dịch tiếng Việt của câu trả lời mẫu
}
