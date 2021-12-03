package com.rsschool.translateapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationResult(
    @Json(name = "translatedText") val translatedText: String?
)