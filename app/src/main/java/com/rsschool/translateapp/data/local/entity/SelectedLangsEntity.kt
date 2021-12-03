package com.rsschool.translateapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selectedLangs")
data class SelectedLangsEntity(
    @ColumnInfo(name = "srcLangCode") val srcLangCode: String,
    @ColumnInfo(name = "resLangCode") val resLangCode: String,
    @PrimaryKey(autoGenerate = false) val id:Int = 0
)
