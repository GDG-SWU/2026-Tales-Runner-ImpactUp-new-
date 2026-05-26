package com.example.dualtales.data

data class BookItem(
    val id: String,
    val title: String,
    val coverImageUrl: String? = null,
    val language: String = "",
    val currentPage: Int = 0,    // 현재 읽은 페이지
    val totalPages: Int = 0,     // 전체 페이지
    val createdAt: String = ""   // 생성일
    // TODO: AI 연동 시 필드 추가
)
