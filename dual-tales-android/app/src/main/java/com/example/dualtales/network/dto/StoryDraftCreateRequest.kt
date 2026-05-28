package com.example.dualtales.network.dto

import com.google.gson.annotations.SerializedName

data class StoryDraftCreateRequest(
    @SerializedName("target_lang_code") val targetLangCode: String,
    @SerializedName("target_age") val targetAge: Int
)
