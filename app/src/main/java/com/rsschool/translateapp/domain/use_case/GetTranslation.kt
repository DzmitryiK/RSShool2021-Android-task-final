package com.rsschool.translateapp.domain.use_case

import com.rsschool.translateapp.domain.model.TranslationResult
import com.rsschool.translateapp.domain.util.TranslationParams
import com.rsschool.translateapp.data.repository.TranslationRepository
import com.rsschool.translateapp.domain.util.ResourceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTranslation @Inject constructor(private val repository: TranslationRepository) {
    operator fun invoke(params: TranslationParams): Flow<ResourceState<TranslationResult>>{
        return repository.getTranslation(params)
    }
}