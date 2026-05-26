package com.example.dualtales.network.dto

data class StoryResponseDto(
    val id: Long,
    val title: String,
    val coverImageUrl: String?,
    val targetLangCode: String,
    val targetAge: Int,
    val page_count: Int,
    val createdAt: String,
    val nickname: String
)
