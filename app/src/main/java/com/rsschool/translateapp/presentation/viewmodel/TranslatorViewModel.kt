package com.rsschool.translateapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.model.Language
import com.rsschool.translateapp.domain.model.SelectedLang
import com.rsschool.translateapp.domain.use_case.*
import com.rsschool.translateapp.domain.util.TranslationParams
import com.rsschool.translateapp.domain.util.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val getListOfLanguages: GetListOfLanguages,
    private val getTranslation: GetTranslation,
    private val addBookmark: AddBookmark,
    private val removeBookmark: RemoveBookmark,
    private val getBookmark: GetBookmark,
    private val getSelectedLangs: GetSelectedLangs,
    private val updateSourceSelectedLang: UpdateSourceSelectedLang,
    private val updateResultSelectedLang: UpdateResultSelectedLang
    ): ViewModel() {

    private val _eventUIFlow = MutableSharedFlow<TranslatorUIEvent>()
    val eventUIFlow = _eventUIFlow.asSharedFlow()

    private val _languagesListFlow = MutableStateFlow<ResourceState<List<Language>>>(ResourceState.Loading(emptyList()))
    val languagesListFlow : StateFlow<ResourceState<List<Language>>> = _languagesListFlow.asStateFlow()

    private val _srcLangFlow = MutableStateFlow<ResourceState<SelectedLang>>(ResourceState.Loading(SelectedLang(0)))
    val srcLangFlow : StateFlow<ResourceState<SelectedLang>> = _srcLangFlow.asStateFlow()

    private val _resLangFlow = MutableStateFlow<ResourceState<SelectedLang>>(ResourceState.Loading(SelectedLang(0)))
    val resLangFlow : StateFlow<ResourceState<SelectedLang>> = _resLangFlow.asStateFlow()

    private val _translateResultFlow = MutableStateFlow<ResourceState<String>>(ResourceState.Success(""))
    val translateResultFlow : StateFlow<ResourceState<String>> = _translateResultFlow.asStateFlow()

    private val _currentBookmarkFlow = MutableStateFlow<ResourceState<Long>>(ResourceState.Success(-1))
    val currentBookmarkFlow : StateFlow<ResourceState<Long>> = _currentBookmarkFlow.asStateFlow()

    private val _translateQueryFlow = MutableStateFlow("")
    val translateQueryFlow: StateFlow<String> = _translateQueryFlow
    private var latestTranslationQuery = ""

    private var translateJob: Job? = null
    val langErrorMessage = "Languages are not initialized.\nPlease, check your connection and restart app"

    init{
        initListOfLanguages()
    }

    private fun initListOfLanguages(){
        viewModelScope.launch {
            getListOfLanguages().onEach{ result ->
                _languagesListFlow.value = result

                //Update current selected languages indexes and names
                when(_languagesListFlow.value){
                    is ResourceState.Success ->{
                        _translateResultFlow.value = ResourceState.Success("")
                        getSelectedLangs().onEach { langs ->
                            if (langs is ResourceState.Success){
                                val sourceLang = result.data!!.find { it.code == langs.data!![0] }
                                if (sourceLang != null){
                                    val index = result.data.indexOf(sourceLang)
                                    _srcLangFlow.value = ResourceState.Success(SelectedLang(index, sourceLang.name))
                                }
                                val resultLang = result.data.find { it.code == langs.data!![1] }
                                if (resultLang != null){
                                    val index = result.data.indexOf(resultLang)
                                    _resLangFlow.value = ResourceState.Success(SelectedLang(index, resultLang.name))
                                }
                            }
                        }.launchIn(this)
                    }
                    is ResourceState.Loading ->{
                        _translateResultFlow.value = ResourceState.Loading()
                    }
                    is ResourceState.Error ->{
                        _translateResultFlow.value = ResourceState.Error(langErrorMessage)
                    }
                }

            }.launchIn(this)
        }
    }

    fun onEvent(event: TranslatorEvent){
        val langList = _languagesListFlow.value.data
        val langListIsReady = _languagesListFlow.value is ResourceState.Success && langList != null
        var errorMessage = ""
        if (!langListIsReady){
            errorMessage = langErrorMessage
        }
        when (event){
            is TranslatorEvent.TextChanged -> {
                _translateQueryFlow.value = event.value
            }
            is TranslatorEvent.SelectSourceLang -> {
                if (langListIsReady){
                    _srcLangFlow.value = ResourceState.Success(SelectedLang(event.value,
                        langList!![event.value].name))
                    viewModelScope.launch {
                        onEvent(TranslatorEvent.TextEnter)
                        updateSourceSelectedLang(langList[event.value].code)
                    }
                }
            }
            is TranslatorEvent.SelectResultLang -> {
                if (langListIsReady)  {
                    _resLangFlow.value = ResourceState.Success(SelectedLang(event.value,
                        langList!![event.value].name))
                    viewModelScope.launch {
                        onEvent(TranslatorEvent.TextEnter)
                        updateResultSelectedLang(langList[event.value].code)
                    }
                }
            }
            is TranslatorEvent.SwapLanguages -> {
                if (langListIsReady && _resLangFlow.value is ResourceState.Success
                    && _srcLangFlow.value is ResourceState.Success) {
                    val resValue = _resLangFlow.value
                    _resLangFlow.value = _srcLangFlow.value
                    _srcLangFlow.value = resValue
                    val resultText = _translateResultFlow.value.data
                    if (resultText != null && resultText.isNotBlank()){
                        _translateQueryFlow.value = resultText
                        viewModelScope.launch {
                            onEvent(TranslatorEvent.TextEnter)
                        }
                    }
                }else if (errorMessage.isBlank()){
                    errorMessage = langErrorMessage
                }
            }
            is TranslatorEvent.TextEnter -> {
                val srcIndex = _srcLangFlow.value.data
                val resIndex = _resLangFlow.value.data
                val query = _translateQueryFlow.value
                if (langListIsReady
                    && srcIndex != null && resIndex != null ){
                        if (query.isNotBlank())
                            translateText(TranslationParams(langList!![srcIndex.index].code,
                                query,langList[resIndex.index].code))
                }else {
                    errorMessage = langErrorMessage
                }
            }
            is TranslatorEvent.CopyToClipboard -> {
                val textToCopy = _translateResultFlow.value.data
                if (textToCopy != null && _translateResultFlow.value is ResourceState.Success) {
                    if (textToCopy.isNotBlank())
                        viewModelScope.launch {
                            _eventUIFlow.emit(
                                TranslatorUIEvent.CopyToClipboard(
                                    textToCopy
                                )
                            )
                        }
                }
                else
                    showToast("Copy error")
            }
            is TranslatorEvent.Bookmark -> {
                val translatedText = _translateResultFlow.value.data
                val srcIndex = _srcLangFlow.value.data
                val resIndex = _resLangFlow.value.data
                val bookmarkId = _currentBookmarkFlow.value.data
                if (!(langListIsReady && srcIndex != null && resIndex != null)){
                    errorMessage = langErrorMessage
                }
                else if (_translateResultFlow.value is ResourceState.Success
                     && bookmarkId != null) {
                        if (translatedText != null && translatedText.isNotBlank()
                            && _translateQueryFlow.value.isNotBlank())
                        onAddRemoveBookmark(
                            Bookmark(
                                bookmarkId,
                                langList!![srcIndex.index].name,
                                _translateQueryFlow.value,
                                langList[resIndex.index].name,
                                translatedText
                            )
                        )
                }else{
                    showToast("Bookmark error")
                }
            }
            is TranslatorEvent.Share -> {
                val textToCopy = _translateResultFlow.value.data
                val srcIndex = _srcLangFlow.value.data
                val resIndex = _resLangFlow.value.data
                if (!(langListIsReady && srcIndex != null && resIndex != null)){
                    errorMessage = langErrorMessage
                }
                else if (textToCopy != null && _translateResultFlow.value is ResourceState.Success) {
                    if (textToCopy.isNotBlank() && latestTranslationQuery.isNotBlank())
                      viewModelScope.launch {
                        _eventUIFlow.emit(
                            TranslatorUIEvent.Share(
                                latestTranslationQuery,
                                textToCopy,
                                langList!![srcIndex.index].code,
                                langList[resIndex.index].code
                            )
                        )
                      }
                }else{
                    showToast("Share error")
                }
            }
            is TranslatorEvent.ClearText -> {
                _translateResultFlow.value = ResourceState.Success("")
                latestTranslationQuery = ""
            }
            is TranslatorEvent.CheckBookmark -> {
                if (_languagesListFlow.value is ResourceState.Loading &&
                        errorMessage == langErrorMessage){
                    errorMessage = ""
                }

                if (_currentBookmarkFlow.value is ResourceState.Success
                    && _currentBookmarkFlow.value.data!! != -1L) {
                    viewModelScope.launch {
                        getBookmark(_currentBookmarkFlow.value.data!!).onEach { result ->
                            when(result){
                                is ResourceState.Success ->{
                                    _currentBookmarkFlow.value =
                                        ResourceState.Success(result.data!!.id)
                                }
                                else ->{
                                    _currentBookmarkFlow.value =
                                        ResourceState.Success(-1)
                                }
                            }
                        }.launchIn(this)
                    }
                }else{
                    showToast("Bookmark check error")
                }
            }
        }
        if (errorMessage.isNotBlank()){
            _translateResultFlow.value =
                ResourceState.Error(errorMessage)
        }
    }


    private fun translateText(params:TranslationParams){
        translateJob?.cancel()
        _translateQueryFlow.value = params.sourceText
        translateJob = viewModelScope.launch {
            getTranslation(params).onEach{result ->
                when (result){
                    is ResourceState.Success -> {
                        val translatedText = result.data?.result ?: ""
                        val bookmarkId = result.data?.bookmarkId ?: -1
                        _translateResultFlow.value = ResourceState.Success(translatedText)
                        _currentBookmarkFlow.value = ResourceState.Success(bookmarkId)
                        latestTranslationQuery = params.sourceText
                    }
                    is ResourceState.Loading -> {
                        _translateResultFlow.value = ResourceState.Loading()
                    }
                    is ResourceState.Error -> {
                        val errorMessage = result.message ?: ""
                        _translateResultFlow.value = ResourceState.Error(errorMessage)
                        _currentBookmarkFlow.value = ResourceState.Error(errorMessage)
                    }
                }
            }.launchIn(this)
        }
    }

    private fun showToast(text: String){
        viewModelScope.launch {
            _eventUIFlow.emit(TranslatorUIEvent.ShowToast(text))
        }
    }

    private fun onAddRemoveBookmark(bookmark: Bookmark){
        viewModelScope.launch {
            if (bookmark.id != -1L){
                removeBookmark(bookmark).onEach { result ->
                    when (result) {
                        is ResourceState.Success -> {
                            _currentBookmarkFlow.value = ResourceState.Success(result.data!!)
                        }
                        is ResourceState.Loading -> {
                            _currentBookmarkFlow.value = ResourceState.Loading(-1)
                        }
                        is ResourceState.Error -> {
                            _currentBookmarkFlow.value = ResourceState.Error(result.message!!, -1)
                        }
                    }
                }.launchIn(this)
            }else {
                addBookmark(bookmark).onEach { result ->
                    when (result) {
                        is ResourceState.Success -> {
                            _currentBookmarkFlow.value = ResourceState.Success(result.data!!)
                        }
                        is ResourceState.Loading -> {
                            _currentBookmarkFlow.value = ResourceState.Loading(-1)
                        }
                        is ResourceState.Error -> {
                            _currentBookmarkFlow.value = ResourceState.Error(result.message!!, -1)
                        }
                    }
                }.launchIn(this)
            }
        }
    }

    sealed class TranslatorEvent {
        data class TextChanged(val value: String):TranslatorEvent()
        object TextEnter: TranslatorEvent()
        data class SelectSourceLang(val value: Int): TranslatorEvent()
        data class SelectResultLang(val value: Int): TranslatorEvent()
        object SwapLanguages: TranslatorEvent()
        object CopyToClipboard: TranslatorEvent()
        object ClearText:TranslatorEvent()
        object Bookmark: TranslatorEvent()
        object Share: TranslatorEvent()
        object CheckBookmark: TranslatorEvent()
    }

    sealed class TranslatorUIEvent{
        data class CopyToClipboard(val value: String): TranslatorUIEvent()
        data class Share(val query: String, val result: String, val sourceLang:String, val resultLang:String): TranslatorUIEvent()
        data class ShowToast(val message: String): TranslatorUIEvent()
    }
}