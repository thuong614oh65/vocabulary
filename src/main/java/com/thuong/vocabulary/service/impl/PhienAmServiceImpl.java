package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.dto.dictionary.Phonetic;
import com.thuong.vocabulary.service.DictionaryService;
import com.thuong.vocabulary.service.PhienAmService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhienAmServiceImpl implements PhienAmService {

    private final DictionaryService dictionaryService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PhienAmServiceImpl(
            DictionaryService dictionaryService,
            RestTemplate restTemplate
    ) {
        this.dictionaryService = dictionaryService;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String layPhienAm(String tu) {
        if (tu == null || tu.trim().isEmpty()) {
            return "";
        }

        String tuChuanHoa = tu.trim();

        // 1. Tầng 1: Free Dictionary API (cho từ đơn không có khoảng trắng)
        if (!tuChuanHoa.contains(" ")) {
            String ipaFreeDict = layTuFreeDictionary(tuChuanHoa);
            if (ipaFreeDict != null && !ipaFreeDict.isBlank()) {
                return chuanHoaIpa(ipaFreeDict);
            }
        }

        // 2. Tầng 2: Datamuse API (chuyên gia ngữ âm từ vựng & cụm từ)
        String ipaDatamuse = layTuDatamuse(tuChuanHoa);
        if (ipaDatamuse != null && !ipaDatamuse.isBlank()) {
            return chuanHoaIpa(ipaDatamuse);
        }

        // 3. Tầng 3: Với cụm từ nhiều chữ hoặc có dấu gạch ngang, tách từng từ và ghép IPA
        if (tuChuanHoa.contains(" ") || tuChuanHoa.contains("-")) {
            String ipaGhep = ghepPhienAmTungTu(tuChuanHoa);
            if (ipaGhep != null && !ipaGhep.isBlank()) {
                return chuanHoaIpa(ipaGhep);
            }
        }

        // 4. Fallback cuối cùng
        String fallbackIpa = layTuFreeDictionary(tuChuanHoa);
        if (fallbackIpa != null && !fallbackIpa.isBlank()) {
            return chuanHoaIpa(fallbackIpa);
        }

        return "";
    }

    private String layTuFreeDictionary(String tu) {
        try {
            DictionaryResponse response = dictionaryService.traTu(tu);
            if (response != null) {
                if (response.getPhonetics() != null) {
                    for (Phonetic phonetic : response.getPhonetics()) {
                        if (phonetic.getText() != null && !phonetic.getText().isBlank()) {
                            return phonetic.getText();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String layTuDatamuse(String tu) {
        try {
            String encoded = URLEncoder.encode(tu, StandardCharsets.UTF_8);
            String url = "https://api.datamuse.com/words?sp=" + encoded + "&qe=sp&md=r&ipa=1";
            String json = restTemplate.getForObject(url, String.class);
            if (json != null && !json.isBlank()) {
                JsonNode root = objectMapper.readTree(json);
                if (root.isArray() && root.size() > 0) {
                    // Ưu tiên phần tử khớp chính xác từ cần tra
                    for (JsonNode item : root) {
                        String wordVal = item.path("word").asText("");
                        if (wordVal.equalsIgnoreCase(tu)) {
                            JsonNode tags = item.path("tags");
                            if (tags.isArray()) {
                                for (JsonNode tag : tags) {
                                    String tagText = tag.asText("");
                                    if (tagText.startsWith("ipa_pron:")) {
                                        String raw = tagText.substring("ipa_pron:".length()).trim();
                                        if (!raw.isEmpty()) {
                                            return raw;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Nếu không khớp hoàn toàn, lấy phần tử đầu tiên có tag ipa_pron
                    JsonNode first = root.get(0);
                    JsonNode tags = first.path("tags");
                    if (tags.isArray()) {
                        for (JsonNode tag : tags) {
                            String tagText = tag.asText("");
                            if (tagText.startsWith("ipa_pron:")) {
                                String raw = tagText.substring("ipa_pron:".length()).trim();
                                if (!raw.isEmpty()) {
                                    return raw;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String ghepPhienAmTungTu(String cumTu) {
        try {
            String[] tuDon = cumTu.split("[\\s\\-]+");
            List<String> danhSachIpa = new ArrayList<>();

            for (String w : tuDon) {
                w = w.trim();
                if (w.isEmpty()) continue;

                String ipa = layTuFreeDictionary(w);
                if (ipa == null || ipa.isBlank()) {
                    ipa = layTuDatamuse(w);
                }

                if (ipa != null && !ipa.isBlank()) {
                    danhSachIpa.add(boGachCheo(ipa));
                } else {
                    danhSachIpa.add(w);
                }
            }

            if (!danhSachIpa.isEmpty()) {
                return String.join(" ", danhSachIpa);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String boGachCheo(String ipa) {
        if (ipa == null) return "";
        String clean = ipa.trim();
        if (clean.startsWith("/")) {
            clean = clean.substring(1);
        }
        if (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        if (clean.startsWith("[") && clean.endsWith("]")) {
            clean = clean.substring(1, clean.length() - 1);
        }
        return clean.trim();
    }

    private String chuanHoaIpa(String rawIpa) {
        if (rawIpa == null || rawIpa.isBlank()) {
            return "";
        }
        String clean = boGachCheo(rawIpa);
        if (clean.isEmpty()) {
            return "";
        }
        return "/" + clean + "/";
    }
}
