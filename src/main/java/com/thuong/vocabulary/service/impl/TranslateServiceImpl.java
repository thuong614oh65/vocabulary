package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.service.TranslateService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TranslateServiceImpl implements TranslateService {


    private final RestTemplate restTemplate;


    public TranslateServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public String dich(String text) {

        try {


            String tu =
                    URLEncoder.encode(
                            text,
                            StandardCharsets.UTF_8
                    );


            String url =
                    "https://api.mymemory.translated.net/get?q="
                            + tu
                            + "&langpair=en|vi";


            String json =
                    restTemplate.getForObject(
                            url,
                            String.class
                    );


            ObjectMapper mapper =
                    new ObjectMapper();


            JsonNode node =
                    mapper.readTree(json);


            String ketQua =
                    node
                            .path("responseData")
                            .path("translatedText")
                            .asText();


            if(ketQua == null || ketQua.isBlank()){

                return "";

            }


            return ketQua;


        }catch(Exception e){

            return "";

        }

    }

}