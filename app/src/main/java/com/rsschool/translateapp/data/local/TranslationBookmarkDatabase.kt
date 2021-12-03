package com.rsschool.translateapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rsschool.translateapp.data.local.entity.BookmarkEntity
import com.rsschool.translateapp.data.local.entity.LanguageEntity
import com.rsschool.translateapp.data.local.entity.SelectedLangsEntity

@Database(
    entities = [BookmarkEntity::class, LanguageEntity::class, SelectedLangsEntity::class],
    version = 1
)
abstract class TranslationBookmarkDatabase: RoomDatabase() {

    abstract val bookmarksDao: TranslationBookmarkDao
}