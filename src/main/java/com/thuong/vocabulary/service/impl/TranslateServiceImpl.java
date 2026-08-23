package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.service.TranslateService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class TranslateServiceImpl implements TranslateService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TranslateServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String dich(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String tuChuanHoa = text.trim();

        // 1. Tầng 1: Google Translate GTX (Siêu tốc, không giới hạn)
        String nghiaGoogle = dichQuaGoogleGTX(tuChuanHoa);
        if (nghiaGoogle != null && !nghiaGoogle.isBlank()) {
            return chuanHoaDauCau(nghiaGoogle);
        }

        // 2. Tầng 2: MyMemory (Dự phòng)
        String nghiaMyMemory = dichQuaMyMemory(tuChuanHoa);
        if (nghiaMyMemory != null && !nghiaMyMemory.isBlank()) {
            return chuanHoaDauCau(nghiaMyMemory);
        }

        return "";
    }

    private String dichQuaGoogleGTX(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=vi&dt=t&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray() && root.size() > 0) {
                    JsonNode sentences = root.get(0);
                    if (sentences.isArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode s : sentences) {
                            if (s.isArray() && s.size() > 0) {
                                sb.append(s.get(0).asText(""));
                            }
                        }
                        String res = sb.toString().trim();
                        if (!res.isEmpty()) {
                            return res;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[TranslateService] Google GTX fallback: " + e.getMessage());
        }
        return null;
    }

    private String dichQuaMyMemory(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=en|vi";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String res = root.path("responseData").path("translatedText").asText("");
                if (!res.isBlank() && !res.toUpperCase().contains("MYMEMORY WARNING")) {
                    return res.trim();
                }
            }
        } catch (Exception e) {
            System.err.println("[TranslateService] MyMemory fallback error: " + e.getMessage());
        }
        return null;
    }

    private String chuanHoaDauCau(String str) {
        if (str == null || str.isBlank()) return "";
        str = str.trim();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}