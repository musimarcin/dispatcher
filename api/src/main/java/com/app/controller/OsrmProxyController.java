package com.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/osrm")
public class OsrmProxyController {

    private final RestTemplate restTemplate;

    public OsrmProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/route/v1/{profile}/**")
    public ResponseEntity<String> getRoute(
            HttpServletRequest request,
            @PathVariable String profile,
            @RequestParam Map<String, String> params) {

        String fullPath = request.getRequestURI();
        String basePath = "/osrm/route/v1/" + profile + "/";
        String coordinates = fullPath.substring(fullPath.indexOf(basePath) + basePath.length());
        String url = "http://osrm:5000/route/v1/" + profile + "/" + coordinates;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        params.forEach(builder::queryParam);
        ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
