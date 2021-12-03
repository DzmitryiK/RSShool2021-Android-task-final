package com.rsschool.translateapp.data.remote

import com.rsschool.translateapp.data.remote.dto.LanguageDto
import com.rsschool.translateapp.data.remote.dto.TranslationResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TranslatorApi {

    @GET("languages")
    suspend fun getLanguages(): List<LanguageDto>

    @POST("translate")
    suspend fun getTranslation(
        @Query("q") sourceLine:String,
        @Query("source") sourceLang:String,
        @Query("target") targetLang:String
    ): TranslationResult

    companion object {
        const val BASE_URL = "https://libretranslate.de/"
    }
}