package com.rsschool.translateapp.domain.use_case

import com.rsschool.translateapp.data.repository.TranslationRepository
import com.rsschool.translateapp.domain.model.SelectedLang
import com.rsschool.translateapp.domain.util.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSelectedLangs @Inject constructor(private val repository: TranslationRepository) {
    operator fun invoke(): Flow<ResourceState<List<String>>> {
        return repository.getSelectedLangs()
    }
}