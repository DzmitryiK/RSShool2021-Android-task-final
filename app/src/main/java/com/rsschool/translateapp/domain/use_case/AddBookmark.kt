package com.rsschool.translateapp.domain.use_case

import com.rsschool.translateapp.data.repository.TranslationRepository
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.util.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddBookmark @Inject constructor(private val repository: TranslationRepository) {
    operator fun invoke(bookmark: Bookmark): Flow<ResourceState<Long>> {
        return repository.addBookmark(bookmark)
    }
}