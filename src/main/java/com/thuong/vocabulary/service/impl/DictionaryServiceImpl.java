package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.service.DictionaryService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DictionaryServiceImpl implements DictionaryService {

    private final RestTemplate restTemplate;
    // Bộ nhớ cache nhanh trong RAM để tra cứu tức thì 0ms
    private final Map<String, DictionaryResponse> cache = new ConcurrentHashMap<>();
    private static final DictionaryResponse NOT_FOUND_SENTINEL = new DictionaryResponse();

    public DictionaryServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public DictionaryResponse traTu(String tu) {
        if (tu == null || tu.isBlank()) {
            return null;
        }

        String tuChuanHoa = tu.trim().toLowerCase();
        if (cache.containsKey(tuChuanHoa)) {
            DictionaryResponse cached = cache.get(tuChuanHoa);
            return (cached == NOT_FOUND_SENTINEL) ? null : cached;
        }

        try {
            String encoded = URLEncoder.encode(tu.trim(), StandardCharsets.UTF_8).replace("+", "%20");
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + encoded;

            ResponseEntity<List<DictionaryResponse>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<DictionaryResponse>>() {}
                    );

            if (response.getBody() == null || response.getBody().isEmpty()) {
                cache.put(tuChuanHoa, NOT_FOUND_SENTINEL);
                return null;
            }

            DictionaryResponse res = response.getBody().get(0);
            cache.put(tuChuanHoa, res);
            return res;

        } catch (Exception e) {
            cache.put(tuChuanHoa, NOT_FOUND_SENTINEL);
            return null;
        }
    }

}