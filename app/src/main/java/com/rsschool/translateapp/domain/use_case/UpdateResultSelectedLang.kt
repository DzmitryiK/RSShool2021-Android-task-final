package com.rsschool.translateapp.domain.use_case

import com.rsschool.translateapp.data.repository.TranslationRepository
import com.rsschool.translateapp.domain.model.Language
import javax.inject.Inject

class UpdateResultSelectedLang @Inject constructor(private val repository: TranslationRepository) {
    suspend operator fun invoke(languageCode: String){
        repository.updateResultSelectedLang(languageCode)
    }
}