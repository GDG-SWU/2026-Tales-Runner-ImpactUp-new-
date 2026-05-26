package com.example.dualtales.network.dto

data class StoryDraftCreateRequest(
    val targetLangCode: String,
    val targetAge: Int
)
