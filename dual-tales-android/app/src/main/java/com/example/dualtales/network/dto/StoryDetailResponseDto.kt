package com.example.dualtales.network.dto

data class StoryDetailResponseDto(
    val storyId: Long,
    val title: String,
    val contents: List<StoryContentDto>
)
