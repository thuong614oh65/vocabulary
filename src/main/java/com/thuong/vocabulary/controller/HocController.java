package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.HocDTO;
import com.thuong.vocabulary.service.BoTuVungService;
import com.thuong.vocabulary.service.HocService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;

@Controller
public class HocController {

    private final BoTuVungService boTuVungService;

    private final HocService hocService;

    public HocController(
            BoTuVungService boTuVungService,
            HocService hocService
    ) {
        this.boTuVungService = boTuVungService;
        this.hocService = hocService;
    }

    @GetMapping("/hoc")
    public String hoc(Model model){

        HocDTO dto = new HocDTO();

        dto.setKieuHoc("NGAU_NHIEN");

        model.addAttribute("hocDTO", dto);

        model.addAttribute(
                "dsBo",
                boTuVungService.layTatCa()
        );

        model.addAttribute(
                "dsTatCa",
                hocService.layTatCa()
        );

        model.addAttribute(
                "dsTheoBo",
                new ArrayList<>()
        );

        return "hoc";

    }

    @GetMapping("/hoc/bo/{id}")
    public String hocTheoBo(
            @PathVariable Long id,
            Model model
    ){

        HocDTO dto = new HocDTO();

        dto.setKieuHoc("THEO_BO");

        dto.setBoId(id);

        model.addAttribute(
                "hocDTO",
                dto
        );

        model.addAttribute(
                "dsBo",
                boTuVungService.layTatCa()
        );

        model.addAttribute(
                "dsTatCa",
                hocService.layTatCa()
        );

        model.addAttribute(
                "dsTheoBo",
                hocService.layTheoBo(id)
        );

        return "hoc";

    }

}