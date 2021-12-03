package com.rsschool.translateapp.domain.use_case

import com.rsschool.translateapp.data.repository.TranslationRepository
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.model.Language
import com.rsschool.translateapp.domain.util.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateSourceSelectedLang @Inject constructor(private val repository: TranslationRepository) {
    suspend operator fun invoke(languageCode: String){
        repository.updateSourceSelectedLang(languageCode)
    }
}