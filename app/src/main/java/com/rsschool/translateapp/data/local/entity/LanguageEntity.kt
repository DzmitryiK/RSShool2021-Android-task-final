package com.rsschool.translateapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class LanguageEntity (
    @PrimaryKey val langCode: String,
    @ColumnInfo(name = "langName") val langName: String
)