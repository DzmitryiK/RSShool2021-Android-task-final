package com.rsschool.translateapp.presentation.util

import com.rsschool.translateapp.domain.model.Bookmark

interface BookmarksListener {
    fun bookmarkItem(bookmark: Bookmark)
}