package com.rsschool.translateapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rsschool.translateapp.data.local.entity.BookmarkEntity
import com.rsschool.translateapp.data.local.entity.LanguageEntity
import com.rsschool.translateapp.data.local.entity.SelectedLangsEntity
import com.rsschool.translateapp.domain.util.TranslationParams

@Dao
interface TranslationBookmarkDao {
    @Query("SELECT * FROM languages")
    suspend fun getAllLanguages(): List<LanguageEntity>

    @Query("SELECT * FROM languages " +
            "WHERE langCode = :code")
    suspend fun getLanguage(code: String): LanguageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languageList: List<LanguageEntity>)

    @Query("SELECT * FROM bookmarks " +
            "WHERE sourceLang = (SELECT langName FROM languages WHERE langCode = :sourceLanguage LIMIT 1)" +
            " AND sourceText = :sourceText" +
            " AND resultLang =  (SELECT langName FROM languages WHERE langCode = :resultLanguage LIMIT 1)")
    suspend fun getBookmarkByValues(sourceLanguage:String,
                            sourceText:String,
                            resultLanguage:String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmark(id: Long): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity): Int

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM selectedLangs LIMIT 1")
    suspend fun getSelectedLangs(): SelectedLangsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedLangs(selectedLangsEntity: SelectedLangsEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSelectedLang(selectedLangsEntity: SelectedLangsEntity): Int
}