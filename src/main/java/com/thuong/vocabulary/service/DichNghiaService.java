package com.thuong.vocabulary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class DichNghiaService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DichNghiaService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // =====================================================
    // LẤY NGHĨA TIẾNG VIỆT ĐA TẦNG (GOOGLE GTX -> MYMEMORY)
    // =====================================================
    public String layNghiaTiengViet(String tu) {
        if (tu == null || tu.isBlank()) {
            return "";
        }

        String tuChuanHoa = tu.trim();

        // 1. Tầng 1: Google Translate GTX (Siêu tốc, không giới hạn, không cần API Key)
        String nghiaGoogle = dichQuaGoogleGTX(tuChuanHoa);
        if (nghiaGoogle != null && !nghiaGoogle.isBlank()) {
            return nghiaGoogle;
        }

        // 2. Tầng 2: MyMemory (Dự phòng)
        String nghiaMyMemory = dichQuaMyMemory(tuChuanHoa);
        if (nghiaMyMemory != null && !nghiaMyMemory.isBlank()) {
            return nghiaMyMemory;
        }

        return "";
    }

    private String dichQuaGoogleGTX(String tu) {
        try {
            String encoded = URLEncoder.encode(tu, StandardCharsets.UTF_8);
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
            System.err.println("[DichNghiaService] Google GTX fallback: " + e.getMessage());
        }
        return null;
    }

    private String dichQuaMyMemory(String tu) {
        try {
            String encoded = URLEncoder.encode(tu, StandardCharsets.UTF_8);
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
            System.err.println("[DichNghiaService] MyMemory fallback error: " + e.getMessage());
        }
        return null;
    }
}