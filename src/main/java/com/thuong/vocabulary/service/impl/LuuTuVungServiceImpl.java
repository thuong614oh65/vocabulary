package com.thuong.vocabulary.service.impl;


import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.LuuTuVungService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class LuuTuVungServiceImpl
        implements LuuTuVungService {


    private final BoTuVungRepository boRepo;

    private final TuVungRepository tuRepo;


    public LuuTuVungServiceImpl(
            BoTuVungRepository boRepo,
            TuVungRepository tuRepo
    ) {

        this.boRepo = boRepo;
        this.tuRepo = tuRepo;

    }


    @Override
    public String luuBo(List<TuVungDTO> danhSach) {


        if(danhSach == null || danhSach.isEmpty()){

            return "Không có từ nào để lưu";

        }



        List<String> daCo = new ArrayList<>();

        List<String> trung = new ArrayList<>();

        List<TuVungDTO> dsLuu = new ArrayList<>();



        for(TuVungDTO dto : danhSach){


            // Không có dữ liệu
            if(dto == null ||
                    dto.getTiengAnh() == null ||
                    dto.getTiengAnh().isBlank()){

                continue;

            }



            String tu =
                    dto.getTiengAnh()
                            .trim()
                            .toLowerCase();



            // trùng trong danh sách nhập
            if(daCo.contains(tu)){

                trung.add(tu);

                continue;

            }



            // trùng database
            if(tuRepo.existsByTiengAnhIgnoreCase(tu)){


                trung.add(tu);

                continue;

            }



            daCo.add(tu);

            dsLuu.add(dto);


        }



        // không còn từ nào để lưu

        if(dsLuu.isEmpty()){


            return "Không có từ mới để lưu. Từ trùng: "
                    + trung;


        }




        // tạo bộ mới

        BoTuVung bo = new BoTuVung();


        bo.setTenBo(
                "Bộ "
                        +(boRepo.count()+1)
        );


        bo.setNgayTao(
                LocalDateTime.now()
        );


        boRepo.save(bo);




        // lưu từ

        for(TuVungDTO dto : dsLuu){


            TuVung tu = new TuVung();


            tu.setTiengAnh(
                    dto.getTiengAnh()
            );


            tu.setTiengViet(
                    dto.getTiengViet()
            );


            tu.setPhienAm(
                    dto.getPhienAm()
            );


            tu.setViDu(
                    dto.getViDu()
            );


            tu.setBoTuVung(bo);


            tuRepo.save(tu);


        }




        String ketQua =
                "Đã lưu "
                        + dsLuu.size()
                        +" từ";


        if(!trung.isEmpty()){

            ketQua +=
                    ". Bỏ qua từ trùng: "
                            + trung;

        }


        return ketQua;


    }

}