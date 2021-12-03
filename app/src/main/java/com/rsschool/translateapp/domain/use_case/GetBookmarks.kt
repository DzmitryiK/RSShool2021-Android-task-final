package com.rsschool.translateapp.domain.use_case

import com.rsschool.translateapp.data.repository.TranslationRepository
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.model.Language
import com.rsschool.translateapp.domain.util.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarks @Inject constructor(private val repository: TranslationRepository) {
    operator fun invoke(): Flow<ResourceState<List<Bookmark>>> {
        return repository.getBookmarks()
    }
}