package com.example.dual_tales.service.story;

import com.example.dual_tales.global.infrastructure.ai.AIResponse;
import com.example.dual_tales.global.infrastructure.ai.FinalStoryResponse;

import java.util.Map;

public interface AiService {
    // 첫번째 질문 가져오기
    AIResponse generateFirstQuestion(Map<String, Object> requestBody);

    // 답변을 토대로 다음 질문 가져오기
    AIResponse getNextQuestion(Map<String, Object> requestBody);

    //최종 동화 원고와 이미지 프롬프트 생성
    FinalStoryResponse generateFinalStory(Map<String, Object> requestBody);
}
