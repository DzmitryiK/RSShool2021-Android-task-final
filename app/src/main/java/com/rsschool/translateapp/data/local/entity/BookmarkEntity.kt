package com.rsschool.translateapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity (
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "sourceLang") val sourceLang: String,
    @ColumnInfo(name = "sourceText") val sourceText: String,
    @ColumnInfo(name = "resultLang") val resultLang: String,
    @ColumnInfo(name = "resultText") val resultText: String
)