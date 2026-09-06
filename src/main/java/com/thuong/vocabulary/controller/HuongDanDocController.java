package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.HuongDanDocDTO;
import com.thuong.vocabulary.service.HuongDanDocService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/huong-dan-doc")
public class HuongDanDocController {

    private final HuongDanDocService huongDanDocService;

    public HuongDanDocController(HuongDanDocService huongDanDocService) {
        this.huongDanDocService = huongDanDocService;
    }

    @GetMapping
    public ResponseEntity<HuongDanDocDTO> layHuongDanDoc(
            @RequestParam("tu") String tu,
            @RequestParam(value = "phienAm", required = false, defaultValue = "") String phienAm,
            @RequestParam(value = "nghia", required = false, defaultValue = "") String nghia
    ) {
        if (tu == null || tu.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        HuongDanDocDTO dto = huongDanDocService.layHuongDanDoc(tu.trim(), phienAm.trim(), nghia.trim());
        return ResponseEntity.ok(dto);
    }
}
