package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.service.TrichXuatTuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TrichXuatTuController {

    private final TrichXuatTuService trichXuatTuService;

    public TrichXuatTuController(TrichXuatTuService trichXuatTuService) {
        this.trichXuatTuService = trichXuatTuService;
    }

    @PostMapping("/api/trich-xuat-tu")
    public ResponseEntity<Map<String, Object>> trichXuatTu(@RequestParam("file") MultipartFile file) {
        Map<String, Object> ketQua = new HashMap<>();

        try {
            List<String> dsTu = trichXuatTuService.trichXuatTu(file);

            if (dsTu.isEmpty()) {
                ketQua.put("thanhCong", false);
                ketQua.put("thongBao", "Không tìm thấy từ vựng tiếng Anh nào trong tệp này!");
                return ResponseEntity.ok(ketQua);
            }

            String noiDung = String.join("\n", dsTu);

            ketQua.put("thanhCong", true);
            ketQua.put("soLuong", dsTu.size());
            ketQua.put("danhSachTu", dsTu);
            ketQua.put("noiDung", noiDung);
            ketQua.put("thongBao", "Đã trích xuất thành công " + dsTu.size() + " từ vựng!");

            return ResponseEntity.ok(ketQua);

        } catch (Exception e) {
            e.printStackTrace();
            ketQua.put("thanhCong", false);
            ketQua.put("thongBao", "Lỗi xử lý file: " + e.getMessage());
            return ResponseEntity.ok(ketQua);
        }
    }
}
