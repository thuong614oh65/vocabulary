package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.dto.dictionary.Definition;
import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.dto.dictionary.Phonetic;
import com.thuong.vocabulary.service.DictionaryService;
import com.thuong.vocabulary.service.TuVungService;
import org.springframework.stereotype.Service;
import com.thuong.vocabulary.service.TranslateService;

@Service
public class TuVungServiceImpl implements TuVungService {

    private final DictionaryService dictionaryService;

    private final TranslateService translateService;

    public TuVungServiceImpl(
            DictionaryService dictionaryService,
            TranslateService translateService
    ) {
        this.dictionaryService = dictionaryService;
        this.translateService = translateService;
    }

    @Override
    public TuVungDTO traTu(String tu) {

        TuVungDTO dto = new TuVungDTO();

        dto.setTiengAnh(tu);

        String nghia =
                translateService.dich(tu);


        dto.setTiengViet(
                chuanHoaNghia(
                        nghia,
                        tu
                )
        );

        DictionaryResponse response =
                dictionaryService.traTu(tu);

        if(response != null){

            if(response.getWord() != null){

                dto.setTiengAnh(response.getWord());

            }

            if(response.getPhonetics() != null){

                for(Phonetic phonetic : response.getPhonetics()){

                    if(phonetic.getText()!=null &&
                            !phonetic.getText().isBlank()){

                        dto.setPhienAm(
                                phonetic.getText()
                        );

                        break;

                    }

                }

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