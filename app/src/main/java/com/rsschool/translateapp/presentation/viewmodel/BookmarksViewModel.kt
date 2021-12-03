package com.rsschool.translateapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.use_case.AddBookmark
import com.rsschool.translateapp.domain.use_case.GetBookmarks
import com.rsschool.translateapp.domain.use_case.RemoveBookmark
import com.rsschool.translateapp.domain.util.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarks: GetBookmarks,
    private val removeBookmark: RemoveBookmark,
    private val addBookmark: AddBookmark
): ViewModel() {
    private val _bookmarksFlow = MutableStateFlow<ResourceState<List<Bookmark>>>(ResourceState.Loading())
    val bookmarksFlow : StateFlow<ResourceState<List<Bookmark>>> = _bookmarksFlow.asStateFlow()

    private val _eventUIFlow = MutableSharedFlow<BookmarksUIEvent>()
    val eventUIFlow = _eventUIFlow.asSharedFlow()

//todo: unit test
    init {
        refreshBookmarks()
    }

    fun onEvent(event: BookmarksEvent) {
        when (event){
            is BookmarksEvent.RefreshItems -> {
                refreshBookmarks()
            }
            is BookmarksEvent.BookmarkItem -> {
                viewModelScope.launch {
                    if (event.value.view_id == -1L){
                        addBookmark(event.value).onEach { result ->
                            when (result) {
                                is ResourceState.Success -> {
                                    val newList = _bookmarksFlow.value.data!!.toMutableList()
                                    newList[newList.indexOf(event.value)] = Bookmark(
                                        result.data!!,
                                        event.value.sourceLang,
                                        event.value.sourceText,
                                        event.value.resultLang,
                                        event.value.resultText
                                    )
                                    _bookmarksFlow.value = ResourceState.Success(newList)
                                }
                                is ResourceState.Loading -> {
                                }
                                is ResourceState.Error -> {
                                    _eventUIFlow.emit(
                                        BookmarksUIEvent.ShowToast(
                                            "Bookmark addition error"
                                        )
                                    )
                                }
                            }
                        }.launchIn(this)
                    }else {
                        removeBookmark(event.value).onEach { result ->
                            when (result) {
                                is ResourceState.Success -> {
                                    val newList = _bookmarksFlow.value.data!!.toMutableList()
                                    newList[newList.indexOf(event.value)] = Bookmark(
                                        event.value.id,
                                        event.value.sourceLang,
                                        event.value.sourceText,
                                        event.value.resultLang,
                                        event.value.resultText,
                                        -1
                                    )
                                    _bookmarksFlow.value = ResourceState.Success(newList)
                                }
                                is ResourceState.Loading -> {
                                }
                                is ResourceState.Error -> {
                                    _eventUIFlow.emit(
                                        BookmarksUIEvent.ShowToast(
                                            "Bookmark remove error"
                                        )
                                    )
                                }

                            }
                        }.launchIn(this)
                    }
                }
            }
        }
    }

    private fun refreshBookmarks(){
        viewModelScope.launch {
            getBookmarks().onEach{ result ->
                _bookmarksFlow.value = result
            }.launchIn(this)
        }
    }

    sealed class BookmarksEvent{
        data class BookmarkItem(val value: Bookmark): BookmarksEvent()
        object RefreshItems:BookmarksEvent()
    }

    sealed class BookmarksUIEvent{
        data class ShowToast(val message: String): BookmarksUIEvent()
    }
}