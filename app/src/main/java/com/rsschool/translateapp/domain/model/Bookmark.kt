package com.rsschool.translateapp.domain.model


data class Bookmark (
    val id: Long,
    val sourceLang: String,
    val sourceText: String,
    val resultLang: String,
    val resultText: String,
    val view_id:Long = id
)