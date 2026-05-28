package com.example.dualtales.network.dto

import com.google.gson.annotations.SerializedName

data class StoryDraftResponseDto(
    val draftId: Long,
    val question_ko: String,
    val question_foreign: String,
    val currentStep: Int,
    @SerializedName("final") val isFinal: Boolean
)
