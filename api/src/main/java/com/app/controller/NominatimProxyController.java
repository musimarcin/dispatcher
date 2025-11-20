package com.app.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nominatim")
public class NominatimProxyController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NominatimProxyController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<?> query(@RequestParam String q) throws JsonProcessingException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("format", "json")
                .queryParam("limit", "10")
                .queryParam("q", q);
            ResponseEntity<String> response = getStringResponseEntity(builder);
            JsonNode json = objectMapper.readTree(response.getBody());
            if (json.isArray() && !json.isEmpty()) {
                List<Map<String, Object>> result = getMaps(json);
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", result));
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> structured(@RequestBody Map<String, String> params) throws JsonProcessingException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("format", "json")
                .queryParam("limit", "10");
            params.forEach((key, value) -> {
                if (value != null && !value.isBlank()) builder.queryParam(key, value);
            });
            ResponseEntity<String> response = getStringResponseEntity(builder);
            JsonNode json = objectMapper.readTree(response.getBody());
            if (json.isArray() && !json.isEmpty()) {
                List<Map<String, Object>> result = getMaps(json);
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", result));
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Location not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }

    }

    private static List<Map<String, Object>> getMaps(JsonNode json) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode node : json) {
            result.add(Map.of(
                    "lat", node.get("lat").asText(),
                    "lon", node.get("lon").asText(),
                    "display_name", node.get("display_name").asText()
            ));
        }
        return result;
    }

    private ResponseEntity<String> getStringResponseEntity(UriComponentsBuilder builder) {
        String url = builder.toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "dispatcher-app");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
    }
}
