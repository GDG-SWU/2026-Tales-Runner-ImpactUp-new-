package com.example.dual_tales.global.infrastructure.ai;

public interface AiClient {
    AIResponse getNextQuestion(String history, String targetLangCode, int targetAge);

    //마지막 답변 이후 최종 동화 원고와 이미지 프롬프트 생성
    FinalStoryResponse generateFinalStory(String fullHistory, String targetLangCode);
}
