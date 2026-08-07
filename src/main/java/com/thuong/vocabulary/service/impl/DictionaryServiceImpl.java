package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.service.DictionaryService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DictionaryServiceImpl implements DictionaryService {

    private final RestTemplate restTemplate;

    public DictionaryServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public DictionaryResponse traTu(String tu) {

        try {

            String url =
                    "https://api.dictionaryapi.dev/api/v2/entries/en/"
                            + tu;


            ResponseEntity<List<DictionaryResponse>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<DictionaryResponse>>() {}
                    );


            if(response.getBody()==null
                    || response.getBody().isEmpty()){

                return null;

            }


            return response.getBody().get(0);


        }catch(Exception e){

            return null;

        }

    }

}