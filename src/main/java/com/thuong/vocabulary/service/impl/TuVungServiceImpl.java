package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.dto.dictionary.Definition;
import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.service.DictionaryService;
import com.thuong.vocabulary.service.PhienAmService;
import com.thuong.vocabulary.service.TuVungService;
import org.springframework.stereotype.Service;
import com.thuong.vocabulary.service.TranslateService;

@Service
public class TuVungServiceImpl implements TuVungService {

    private final DictionaryService dictionaryService;
    private final TranslateService translateService;
    private final PhienAmService phienAmService;

    public TuVungServiceImpl(
            DictionaryService dictionaryService,
            TranslateService translateService,
            PhienAmService phienAmService
    ) {
        this.dictionaryService = dictionaryService;
        this.translateService = translateService;
        this.phienAmService = phienAmService;
    }

    @Override
    public TuVungDTO traTu(String tu) {

        TuVungDTO dto = new TuVungDTO();

        dto.setTiengAnh(tu);

        // 1. Dịch nghĩa tiếng Việt
        String nghia =
                translateService.dich(tu);

        dto.setTiengViet(
                chuanHoaNghia(
                        nghia,
                        tu
                )
        );

        // 2. Tra cứu phiên âm qua Module PhienAmService chuyên biệt
        String phienAm =
                phienAmService.layPhienAm(tu);

        dto.setPhienAm(phienAm);

        // 3. Tra cứu ví dụ từ từ điển (nếu có)
        DictionaryResponse response =
                dictionaryService.traTu(tu);

        if(response != null){

            if(response.getWord() != null){
                dto.setTiengAnh(response.getWord());
            }

            if(response.getMeanings()!=null &&
                    !response.getMeanings().isEmpty()){

                if(!response.getMeanings()
                        .get(0)
                        .getDefinitions()
                        .isEmpty()){

                    Definition definition =
                            response.getMeanings()
                                    .get(0)
                                    .getDefinitions()
                                    .get(0);

                    dto.setViDu(
                            definition.getExample()
                    );

                }

            }

        }

        return dto;

    }

    private String chuanHoaNghia(
            String nghia,
            String tu
    )
    {

        if(nghia == null || nghia.isBlank()){

            return "";

        }


        nghia = nghia.trim();


        return nghia.substring(0,1).toUpperCase()
                + nghia.substring(1);

    }

}