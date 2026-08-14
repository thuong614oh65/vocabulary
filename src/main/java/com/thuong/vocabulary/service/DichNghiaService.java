package com.thuong.vocabulary.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DichNghiaService {

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public DichNghiaService() {

        httpClient =
                HttpClient.newHttpClient();

        objectMapper =
                new ObjectMapper();
    }


    // =====================================================
    // LẤY NGHĨA TIẾNG VIỆT
    // =====================================================

    public String layNghiaTiengViet(String tu)
            throws Exception {

        if (tu == null || tu.isBlank()) {
            return "";
        }

        String tuChuanHoa =
                tu.trim().toLowerCase();

        String tuEncode =
                URLEncoder.encode(
                        tuChuanHoa,
                        StandardCharsets.UTF_8
                );

        String langPair =
                URLEncoder.encode(
                        "en|vi",
                        StandardCharsets.UTF_8
                );

        String url =
                "https://api.mymemory.translated.net/get"
                        + "?q="
                        + tuEncode
                        + "&langpair="
                        + langPair;

        System.out.println(
                "===== URL TRA NGHĨA ====="
        );

        System.out.println(url);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "===== MYMEMORY STATUS ====="
        );

        System.out.println(
                response.statusCode()
        );

        System.out.println(
                response.body()
        );

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "MyMemory HTTP "
                            + response.statusCode()
            );
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        JsonNode responseData =
                root.get("responseData");

        if (responseData == null) {
            return "";
        }

        JsonNode translatedText =
                responseData.get(
                        "translatedText"
                );

        if (translatedText == null) {
            return "";
        }

        return translatedText
                .asText()
                .trim();
    }
}