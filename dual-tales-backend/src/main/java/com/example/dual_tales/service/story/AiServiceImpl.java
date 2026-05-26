package com.example.dual_tales.service.story;

import com.example.dual_tales.global.infrastructure.ai.AIResponse;
import com.example.dual_tales.global.infrastructure.ai.FinalStoryResponse;
import com.example.dual_tales.global.infrastructure.ai.GeminiAiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final GeminiAiClient geminiAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AIResponse generateFirstQuestion(Map<String, Object> requestBody) {
        try {
            //AI 서버 호출
            ResponseEntity<String> response = geminiAiClient.callAiServer(requestBody);
            //받은 JSON 문자열을 AIResponse 객체로 파싱해서 리턴
            return objectMapper.readValue(response.getBody(), AIResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 첫 질문 요청 실패", e);
        }
    }

    @Override
    public AIResponse getNextQuestion(Map<String, Object> requestBody) {
        try {
            //AI 서버 호출
            ResponseEntity<String> response = geminiAiClient.callAiServer(requestBody);
            //받은 JSON 문자열을 AIResponse 객체로 파싱해서 리턴
            return objectMapper.readValue(response.getBody(), AIResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 다음 질문 요청 실패", e);
        }
    }

    @Override
    public FinalStoryResponse generateFinalStory(Map<String, Object> requestBody) {
        try {
            ResponseEntity<String> response = geminiAiClient.callAiServer(requestBody);
            //최종 동화 데이터 포멧(FinalStoryResponnse)로 파싱해서 리턴
            return objectMapper.readValue(response.getBody(), FinalStoryResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 최종 동화 생성 요청 실패", e);
        }
    }
}