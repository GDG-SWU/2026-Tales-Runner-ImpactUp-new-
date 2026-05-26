package com.example.dualtales.network.dto

data class StoryDraftResponseDto(
    val draftId: Long,
    val question_ko: String,
    val question_foreign: String,
    val currentStep: Int,
    val isFinal: Boolean
)
