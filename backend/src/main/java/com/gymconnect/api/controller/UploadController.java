package com.gymconnect.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${imgbb.api-key}")
    private String imgbbKey;

    private final RestTemplate restTemplate;

    @PostMapping("/imagen")
    public ResponseEntity<?> uploadImage(@AuthenticationPrincipal UserDetails ud,
                                          @RequestParam("image") MultipartFile file) {
        if (ud == null) return ResponseEntity.status(401).build();
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
        if (file.getSize() > 5 * 1024 * 1024)
            return ResponseEntity.badRequest().body(Map.of("error", "Máximo 5 MB"));
        String contentType = file.getContentType();
        if (contentType == null || !java.util.List.of("image/jpeg","image/png","image/webp","image/gif").contains(contentType))
            return ResponseEntity.badRequest().body(Map.of("error", "Solo imágenes JPEG, PNG, WEBP o GIF"));

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    "https://api.imgbb.com/1/upload?key=" + imgbbKey,
                    new HttpEntity<>(body, headers), Map.class);

            Map<?, ?> data = (Map<?, ?>) resp.getBody().get("data");
            String url = (String) data.get("url");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "Error al subir imagen"));
        }
    }
}
