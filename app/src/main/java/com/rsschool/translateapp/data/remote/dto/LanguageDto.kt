package com.rsschool.translateapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LanguageDto (
    @Json(name = "code")val code: String,
    @Json(name = "name") val name: String
)