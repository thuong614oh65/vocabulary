package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.HocDTO;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.service.HocService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.thuong.vocabulary.dto.ThemTuDTO;
import org.springframework.ui.Model;
import com.thuong.vocabulary.service.DanhSachTuService;
import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.service.LuuTuVungService;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;
import com.thuong.vocabulary.service.TuVungService;

import java.util.ArrayList;

@Controller
public class HomeController {

    private final DanhSachTuService danhSachTuService;

    private final TuVungService tuVungService;

    private final LuuTuVungService luuTuVungService;

    private final HocService hocService;

    public HomeController(
            DanhSachTuService danhSachTuService,
            TuVungService tuVungService,
            LuuTuVungService luuTuVungService,
            HocService hocService
    ) {
        this.danhSachTuService = danhSachTuService;
        this.tuVungService = tuVungService;
        this.luuTuVungService = luuTuVungService;
        this.hocService = hocService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/them-tu")
    public String themTu(Model model) {

        model.addAttribute("themTuDTO", new ThemTuDTO());

        return "them-tu";
    }

    @PostMapping("/tra-hang-loat")
    public String traHangLoat(
            @ModelAttribute ThemTuDTO themTuDTO,
            Model model,
            HttpSession session
    ) {

        List<String> danhSach =
                danhSachTuService.tachDanhSach(
                        themTuDTO.getNoiDung()
                );


        List<TuVungDTO> ketQua = new ArrayList<>();

        List<String> loi = new ArrayList<>();


        for (String tu : danhSach) {

            try {

                TuVungDTO dto =
                        tuVungService.traTu(tu);


                if(dto != null){

                    ketQua.add(dto);

                }else{

                    loi.add(tu);

                }


            } catch(Exception e){

                loi.add(tu);

            }

        }


        model.addAttribute(
                "themTuDTO",
                themTuDTO
        );


        model.addAttribute(
                "ketQua",
                ketQua
        );


        if(!loi.isEmpty()){

            session.setAttribute(
                    "thongBao",
                    "Không tìm thấy: " + String.join(", ", loi)
            );

        }


        session.setAttribute(
                "danhSachTu",
                ketQua
        );


        return "them-tu";

    }

    @PostMapping("/luu-bo")
    public String luuBo(

            @RequestParam List<String> tiengAnh,
            @RequestParam List<String> tiengViet,
            @RequestParam(required = false) List<String> phienAm,
            @RequestParam(required = false) List<String> viDu,

            HttpSession session
    ) {

        List<TuVungDTO> danhSach = new ArrayList<>();

        for (int i = 0; i < tiengAnh.size(); i++) {

            TuVungDTO dto = new TuVungDTO();

            dto.setTiengAnh(tiengAnh.get(i));
            dto.setTiengViet(tiengViet.get(i));

            if (phienAm != null && i < phienAm.size()) {
                dto.setPhienAm(phienAm.get(i));
            }

            if (viDu != null && i < viDu.size()) {
                dto.setViDu(viDu.get(i));
            }

            danhSach.add(dto);
        }

        String thongBao =
                luuTuVungService.luuBo(danhSach);

        session.setAttribute(
                "thongBao",
                thongBao
        );

        session.removeAttribute("danhSachTu");

        return "redirect:/them-tu";
    }

    @PostMapping("/hoc")
    public String batDauHoc(
            @ModelAttribute HocDTO hocDTO,
            Model model,
            HttpSession session
    ){

        System.out.println(hocDTO.getKieuHoc());

        if (hocDTO.getTuIds() != null) {
            for (Long id : hocDTO.getTuIds()) {
                System.out.println(id);
            }
        } else {
            System.out.println("tuIds = null");
        }

        List<TuVung> dsHoc = new ArrayList<>();

        switch (hocDTO.getKieuHoc()) {

            case "NGAU_NHIEN":

                dsHoc = hocService.layNgauNhien();

                break;

            case "THEO_BO":

                dsHoc = hocService.layTheoBo(
                        hocDTO.getBoId()
                );

                break;

            case "CHON_TUNG_TU":

                dsHoc = hocService.layTheoIds(
                        hocDTO.getTuIds()
                );

                break;

            case "TU_SAI":
                dsHoc = hocService.layTuSai();
                break;

        }

        model.addAttribute(
                "hocDTO",
                hocDTO
        );

        model.addAttribute(
                "dsHoc",
                dsHoc

        );

        session.setAttribute(
                "dsHoc",
                dsHoc
        );

        return "hoc-bat-dau";


    }

    @PostMapping("/bat-dau-hoc")
    public String batDauHoc(
            HttpSession session
    ){

        List<TuVung> dsHoc =
                (List<TuVung>)
                        session.getAttribute("dsHoc");


        Collections.shuffle(dsHoc);


        session.setAttribute(
                "tuDangHoc",
                dsHoc
        );


        session.setAttribute(
                "luotHoc",
                1
        );


        return "hoc-chon";

    }

    @GetMapping("/hoc/tiep")
    public String tiepLuot(
            HttpSession session,
            Model model
    ){


        Integer luot =
                (Integer)
                        session.getAttribute("luotHoc");



        luot++;



        List<TuVung> dsHoc =
                (List<TuVung>)
                        session.getAttribute("tuDangHoc");



        Collections.shuffle(dsHoc);



        session.setAttribute(
                "luotHoc",
                luot
        );



        session.setAttribute(
                "tuDangHoc",
                dsHoc
        );



        model.addAttribute(
                "dsHoc",
                dsHoc
        );



        if(luot % 2 == 1){


            return "hoc-chon";


        }
        else{


            return "hoc-luot2";


        }


    }

    @GetMapping("/hoc/dung")
    public String dungHoc(
            HttpSession session
    ){

        session.removeAttribute("tuDangHoc");

        session.removeAttribute("dsHoc");

        session.removeAttribute("luotHoc");

        return "redirect:/";

    }

    @ResponseBody
    @PostMapping("/hoc/sai/{id}")
    public void tangSoLanSai(
            @PathVariable Long id
    ){

        hocService.tangSoLanSai(id);

    }

    @ResponseBody
    @PostMapping("/hoc/dung/{id}")
    public void giamSoLanSai(@PathVariable Long id) {
        hocService.giamSoLanSai(id);
    }

}