package com.rsschool.translateapp.data.repository

import android.util.Log
import com.rsschool.translateapp.data.local.TranslationBookmarkDao
import com.rsschool.translateapp.data.local.entity.BookmarkEntity
import com.rsschool.translateapp.data.local.entity.LanguageEntity
import com.rsschool.translateapp.data.local.entity.SelectedLangsEntity
import com.rsschool.translateapp.domain.model.Language
import com.rsschool.translateapp.data.remote.TranslatorApi
import com.rsschool.translateapp.data.util.DataConstants.DEFAULT_RESULT_LANG_CODE
import com.rsschool.translateapp.data.util.DataConstants.DEFAULT_SOURCE_LANG_CODE
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.model.SelectedLang
import com.rsschool.translateapp.domain.model.TranslationResult
import com.rsschool.translateapp.domain.util.ResourceState
import com.rsschool.translateapp.domain.util.TranslationParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

class TranslationRepository(
    private val api: TranslatorApi,
    private val dao: TranslationBookmarkDao
) {
    fun getListOfLanguages():Flow<ResourceState<List<Language>>> = flow {
        emit(ResourceState.Loading())
        val resList: List<Language>

        try {
            resList = api.getLanguages().map { result -> Language(result.code, result.name) }

            val localList = dao.getAllLanguages().map { result -> Language(result.langCode, result.langName) }

            if (resList != localList){
                dao.insertLanguages(resList.map {LanguageEntity(it.code, it.name) })
            }

            emit(ResourceState.Success(resList))
        }catch (e:HttpException) {
            Log.d("getListOfLanguages","Body: "+e.response()?.errorBody()?.string())
            emit(ResourceState.Error(
                "Something went wrong, check your Internet connection"
            ))
        }catch (e: IOException) {
            Log.d("getListOfLanguages","Body: "+e.message)
            emit(ResourceState.Error(
                "Something went wrong, check your query"
            ))
        }
    }

    fun getSelectedLangs():Flow<ResourceState<List<String>>> = flow{
        emit(ResourceState.Loading())
        val result = dao.getSelectedLangs()
        if (result != null){
            val resList = listOf(result.srcLangCode, result.resLangCode)
            emit(ResourceState.Success(resList))
        } else {
            val defaultEntity = SelectedLangsEntity(DEFAULT_SOURCE_LANG_CODE,DEFAULT_RESULT_LANG_CODE,0)
            val insertRes = dao.insertSelectedLangs(defaultEntity)
            if (insertRes == 0){
                val resList = listOf(defaultEntity.srcLangCode, defaultEntity.resLangCode)
                emit(ResourceState.Success(resList))
            } else {
                emit(ResourceState.Error("Couldn't instantiate default languages"))
            }
        }
    }

    suspend fun updateSourceSelectedLang(langCode: String){
        var result = dao.getSelectedLangs()
        if (result != null) {
            result = SelectedLangsEntity(langCode, result.resLangCode, 0)
            dao.updateSelectedLang(result)
        }else{
            Log.d("updSourceSelectedLang","SelectedLangs not found")
        }
    }

    suspend fun updateResultSelectedLang(langCode: String){
        var result = dao.getSelectedLangs()
        if (result != null) {
            result = SelectedLangsEntity(result.srcLangCode, langCode, 0)
            dao.updateSelectedLang(result)
        }else{
            Log.d("updResultSelectedLang","SelectedLangs not found")
        }
    }

    fun getTranslation(params: TranslationParams):Flow<ResourceState<TranslationResult>> = flow{
        emit(ResourceState.Loading())

        val result = TranslationResult("",-1)
        val cachedTranslation = dao.getBookmarkByValues(params.sourceLanguage,
            params.sourceText, params.resultLanguage)
        if (cachedTranslation != null){
            result.bookmarkId = cachedTranslation.id
            result.result = cachedTranslation.resultText
            emit(ResourceState.Success(result))
        }

        try{
            val apiResult = api.getTranslation(params.sourceText,params.sourceLanguage,params.resultLanguage).translatedText
        if (apiResult != null) {
            result.result = apiResult
            emit(ResourceState.Success(result))
            //Bookmarked pair translation updating
            if (cachedTranslation != null && cachedTranslation.resultText != apiResult) {
                dao.updateBookmark(
                    BookmarkEntity(
                        cachedTranslation.id, cachedTranslation.sourceLang,
                        cachedTranslation.sourceText, cachedTranslation.resultLang,
                        apiResult
                    )
                )
            }

        }else emit(ResourceState.Error("Something went wrong, API error"))
        }catch (e:HttpException){
            Log.d("getTranslation","Body: "+e.response()?.errorBody()?.string())
            emit(ResourceState.Error(
                "Something went wrong, check your Internet connection"
            ))
        }catch (e:IOException){
            Log.d("getTranslation","Body: "+e.message)
            emit(ResourceState.Error(
                "Something went wrong, check your query"
            ))
        }
    }

    fun getBookmarks():Flow<ResourceState<List<Bookmark>>> = flow{
        emit(ResourceState.Loading())
        val bookmarks = dao.getAllBookmarks().map { entity ->
            Bookmark(entity.id, entity.sourceLang, entity.sourceText, entity.resultLang, entity.resultText)
        }
        emit(ResourceState.Success(bookmarks))
    }

    fun addBookmark(bookmark: Bookmark) = flow{
        emit(ResourceState.Loading())
        val result = dao.insertBookmark(
            BookmarkEntity(
                if (bookmark.id == -1L) 0 else (bookmark.id),
                bookmark.sourceLang,
                bookmark.sourceText,
                bookmark.resultLang,
                bookmark.resultText
            )
        )
        if (result >= 1){
            emit(ResourceState.Success(result))
        }else{
            Log.d("addBookmark","Insert didn't return anything")
            emit(ResourceState.Error("Bookmark insert error"))
        }

    }

    fun removeBookmark(bookmark: Bookmark) = flow{
        emit(ResourceState.Loading())
        val result = dao.deleteBookmark(
            BookmarkEntity(
                bookmark.id,
                bookmark.sourceLang,
                bookmark.sourceText,
                bookmark.resultLang,
                bookmark.resultText
            )
        )
        if (result >= 1){
            emit(ResourceState.Success(-1L))
            if (result > 1)
                Log.d("removeBookmark","Remove affected several bookmarks. Id = ${bookmark.id.toString()}")
        }else{
            Log.d("addBookmark","Insert didn't return anything")
            emit(ResourceState.Error("Bookmark insert error"))
        }
    }

    fun getBookmark(id:Long) = flow{
        emit(ResourceState.Loading())
        val resultEntity = dao.getBookmark(id)
        if (resultEntity != null){
            val result = Bookmark(resultEntity.id,
                resultEntity.sourceLang,
                resultEntity.sourceText,
                resultEntity.resultLang,
                resultEntity.resultText
            )
            emit(ResourceState.Success(result))
        }else{
            emit(ResourceState.Error("not found"))
        }
    }

    companion object{

    }
}