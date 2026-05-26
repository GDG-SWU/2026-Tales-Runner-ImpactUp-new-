package com.example.dualtales.network.dto

data class StoryContentDto(
    val sequence: Int,
    val question_ko: String?,
    val question_foreign: String?,
    val answer: String?,
    val content_ko: String,
    val content_foreign: String,
    val image_url: String?
)
