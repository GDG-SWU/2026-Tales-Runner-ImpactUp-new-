package com.example.dualtales.network.dto

data class UserResponseDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val role: String,
    var accessToken: String? = null
)
