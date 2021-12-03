package com.rsschool.translateapp.domain.util

data class TranslationParams(
    val sourceLanguage:String,
    var sourceText:String,
    val resultLanguage:String
)