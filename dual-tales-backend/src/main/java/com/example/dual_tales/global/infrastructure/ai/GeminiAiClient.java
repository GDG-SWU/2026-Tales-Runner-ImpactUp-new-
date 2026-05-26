package com.example.dual_tales.global.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiAiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // AI API 엔드포인트 주소
    @org.springframework.beans.factory.annotation.Value("${BASE_URL:https://story-generator-976960175902.asia-northeast3.run.app}")
    private String AI_SERVER_URL;

    public ResponseEntity<String> callAiServer(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 요청 본문(카멜케이스 데이터)과 헤더 합치기
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 3. 주소 파라미터나 헤더 인증 없이, 순수하게 데이터 상자만 POST로 전달
        return restTemplate.exchange(
                AI_SERVER_URL + "/v1/ai/generate",
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}