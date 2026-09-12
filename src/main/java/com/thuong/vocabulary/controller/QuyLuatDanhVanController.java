package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.QuyLuatDanhVanDTO;
import com.thuong.vocabulary.service.QuyLuatDanhVanService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class QuyLuatDanhVanController {

    private final QuyLuatDanhVanService quyLuatDanhVanService;

    public QuyLuatDanhVanController(QuyLuatDanhVanService quyLuatDanhVanService) {
        this.quyLuatDanhVanService = quyLuatDanhVanService;
    }

    /**
     * Màn hình chính Sơ đồ Tư duy Quy luật Đánh vần (Phonics Mindmap)
     */
    @GetMapping({"/so-do-danh-van", "/quy-luat-danh-van/so-do"})
    public String trangSoDoDanhVan(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "cat", required = false) String cat,
            @RequestParam(value = "tu", required = false) String tu,
            Model model
    ) {
        List<QuyLuatDanhVanDTO> dsQuyLuat = quyLuatDanhVanService.layTatCaQuyLuat();
        QuyLuatDanhVanDTO qlHienTai = null;

        if (tu != null && !tu.isBlank()) {
            qlHienTai = quyLuatDanhVanService.taoSoDoTuAI(tu.trim());
        }

        if (qlHienTai == null && id != null && !id.isBlank()) {
            qlHienTai = quyLuatDanhVanService.layTheoId(id);
        }

        // Nếu người dùng không chỉ định id hay tu, qlHienTai giữ null hoặc lấy câu đầu tiên để dự phòng
        if (qlHienTai == null && !dsQuyLuat.isEmpty()) {
            qlHienTai = dsQuyLuat.get(0);
        }

        boolean moBangNgoai = (id == null || id.isBlank()) && (tu == null || tu.isBlank());

        model.addAttribute("dsQuyLuat", dsQuyLuat);
        model.addAttribute("quyLuatHienTai", qlHienTai);
        model.addAttribute("idHienTai", (id != null && !id.isBlank()) ? id : (qlHienTai != null ? qlHienTai.getId() : "w_or"));
        model.addAttribute("catHienTai", cat != null ? cat : "");
        model.addAttribute("moBangNgoai", moBangNgoai);

        return "so-do-danh-van";
    }

    /**
     * API lấy danh sách tất cả các quy tắc đã phân loại
     */
    @GetMapping("/api/so-do-danh-van/danh-sach")
    @ResponseBody
    public ResponseEntity<List<QuyLuatDanhVanDTO>> layDanhSachQuyLuat() {
        return ResponseEntity.ok(quyLuatDanhVanService.layTatCaQuyLuat());
    }

    /**
     * API lấy chi tiết một sơ đồ quy tắc theo ID
     */
    @GetMapping("/api/so-do-danh-van/chi-tiet")
    @ResponseBody
    public ResponseEntity<QuyLuatDanhVanDTO> layChiTietQuyLuat(@RequestParam("id") String id) {
        QuyLuatDanhVanDTO dto = quyLuatDanhVanService.layTheoId(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * API tìm kiếm quy luật theo từ vựng hoặc cụm chữ cái
     */
    @GetMapping("/api/so-do-danh-van/tim-kiem")
    @ResponseBody
    public ResponseEntity<List<QuyLuatDanhVanDTO>> timKiemQuyLuat(@RequestParam("q") String q) {
        return ResponseEntity.ok(quyLuatDanhVanService.timKiemQuyLuat(q));
    }

    /**
     * API tạo sơ đồ tư duy bằng AI Gemini cho bất kỳ âm hoặc từ nào
     */
    @GetMapping("/api/so-do-danh-van/ai-generate")
    @ResponseBody
    public ResponseEntity<QuyLuatDanhVanDTO> taoSoDoAI(@RequestParam("q") String q) {
        QuyLuatDanhVanDTO dto = quyLuatDanhVanService.taoSoDoTuAI(q);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }
}
