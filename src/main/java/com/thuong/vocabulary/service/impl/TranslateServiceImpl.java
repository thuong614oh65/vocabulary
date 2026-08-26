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
        return dich(text, "en", "vi");
    }

    @Override
    public String dich(String text, String fromLang, String toLang) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String tuChuanHoa = text.trim();
        String sl = (fromLang == null || fromLang.isBlank()) ? "auto" : fromLang.trim().toLowerCase();
        String tl = (toLang == null || toLang.isBlank()) ? "vi" : toLang.trim().toLowerCase();

        // 1. Tầng 1: Google Clients5 (Chrome Extension - cực kỳ nhanh, không bị 429)
        String nghia1 = dichQuaGoogleClients5(tuChuanHoa, sl, tl);
        if (nghia1 != null && !nghia1.isBlank()) {
            return chuanHoaDauCau(nghia1);
        }

        // 2. Tầng 2: Google Mobile Web (Chính xác, ổn định)
        String nghia2 = dichQuaGoogleMobile(tuChuanHoa, sl, tl);
        if (nghia2 != null && !nghia2.isBlank()) {
            return chuanHoaDauCau(nghia2);
        }

        // 3. Tầng 3: Google GTX
        String nghia3 = dichQuaGoogleGTX(tuChuanHoa, sl, tl);
        if (nghia3 != null && !nghia3.isBlank()) {
            return chuanHoaDauCau(nghia3);
        }

        // 4. Tầng 4: MyMemory API (Dự phòng cuối cùng)
        String nghia4 = dichQuaMyMemory(tuChuanHoa, sl, tl);
        if (nghia4 != null && !nghia4.isBlank()) {
            return chuanHoaDauCau(nghia4);
        }

        return "";
    }

    @Override
    public String dichAnhSangViet(String text) {
        return dich(text, "en", "vi");
    }

    @Override
    public String dichVietSangAnh(String text) {
        return dich(text, "vi", "en");
    }

    // Tầng 1: Google Clients5 API
    private String dichQuaGoogleClients5(String text, String sl, String tl) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl="
                    + sl + "&tl=" + tl + "&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray() && root.size() > 0) {
                    String res = root.get(0).asText("").trim();
                    if (!res.isEmpty()) {
                        return res;
                    }
                } else if (root.isTextual()) {
                    return root.asText().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("[TranslateService] Google Clients5 error: " + e.getMessage());
        }
        return null;
    }

    // Tầng 2: Google Mobile Web Scraper
    private String dichQuaGoogleMobile(String text, String sl, String tl) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.google.com/m?sl=" + sl + "&tl=" + tl + "&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                String body = response.body();
                String marker = "class=\"result-container\">";
                int start = body.indexOf(marker);
                if (start != -1) {
                    start += marker.length();
                    int end = body.indexOf("</div>", start);
                    if (end != -1) {
                        String res = body.substring(start, end).trim();
                        // Giải mã HTML entities cơ bản nếu có
                        res = org.springframework.web.util.HtmlUtils.htmlUnescape(res);
                        if (!res.isEmpty()) {
                            return res;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[TranslateService] Google Mobile error: " + e.getMessage());
        }
        return null;
    }

    // Tầng 3: Google GTX
    private String dichQuaGoogleGTX(String text, String sl, String tl) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="
                    + sl + "&tl=" + tl + "&dt=t&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

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
            System.err.println("[TranslateService] Google GTX error: " + e.getMessage());
        }
        return null;
    }

    // Tầng 4: MyMemory
    private String dichQuaMyMemory(String text, String sl, String tl) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String source = sl.equals("auto") ? "en" : sl;
            String langpair = URLEncoder.encode(source + "|" + tl, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + langpair;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

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