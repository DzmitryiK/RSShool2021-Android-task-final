package com.rsschool.translateapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.model.Language
import com.rsschool.translateapp.domain.use_case.AddBookmark
import com.rsschool.translateapp.domain.use_case.GetBookmarks
import com.rsschool.translateapp.domain.use_case.RemoveBookmark
import com.rsschool.translateapp.domain.util.ResourceState
import com.rsschool.translateapp.presentation.viewmodel.BookmarksViewModel
import com.rsschool.translateapp.presentation.viewmodel.TranslatorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class BookmarksViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var addBookmark: AddBookmark
    @Mock
    private lateinit var removeBookmark: RemoveBookmark
    @Mock
    private lateinit var getBookmarks: GetBookmarks

    private val mockBookmark = Bookmark(1L,"testLanguage","sourceTranslation",
        "testLanguage2","TranslationRes")
    private var mockBookmarkResLoading = ResourceState.Loading<Long>()
    private val mockRemovedBookmark = Bookmark(mockBookmark.id,mockBookmark.sourceLang,mockBookmark.sourceText,
        mockBookmark.resultLang, mockBookmark.resultText, -1L)

    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun setup() {

        getBookmarks = Mockito.mock(GetBookmarks::class.java)
        val mockGetBookmarksFlow = flow {
            emit(ResourceState.Loading())
            delay(10)
            emit(ResourceState.Success(listOf(mockBookmark)))
        }
        Mockito.`when`(getBookmarks.invoke())
            .thenReturn(mockGetBookmarksFlow)

        addBookmark = Mockito.mock(AddBookmark::class.java)
        val mockAddBookmarkFlow = flow {
            emit(mockBookmarkResLoading)
            delay(10)
            emit(ResourceState.Success(1L))
        }
        Mockito.`when`(addBookmark.invoke(mockRemovedBookmark))
            .thenReturn(mockAddBookmarkFlow)

        removeBookmark = Mockito.mock(RemoveBookmark::class.java)
        val mockRemoveBookmarkFlow = flow {
            emit(mockBookmarkResLoading)
            delay(10)
            emit(ResourceState.Success(-1L))
        }
        Mockito.`when`(removeBookmark.invoke(mockBookmark))
            .thenReturn(mockRemoveBookmarkFlow)

        viewModel = BookmarksViewModel(getBookmarks,
            removeBookmark, addBookmark)
    }

    @Test
    fun refreshItemsTest()= coroutineRule.testDispatcher.runBlockingTest {
        //no action because it is done in init
        //Loading state
        assert(viewModel.bookmarksFlow.value is ResourceState.Loading<List<Bookmark>>)
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.bookmarksFlow.value is ResourceState.Success<List<Bookmark>>)
        assertEquals(listOf(mockBookmark),viewModel.bookmarksFlow.value.data)
    }

    @Test
    fun bookmarkTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for bookmarks to load
        advanceTimeBy(10)
        //Trigger event - remove Bookmark
        viewModel.onEvent(BookmarksViewModel.BookmarksEvent.BookmarkItem(mockBookmark))
        //Wait for load
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.bookmarksFlow.value is ResourceState.Success<List<Bookmark>>)
        var newBookmarkList = listOf(mockRemovedBookmark)
        assertEquals(newBookmarkList,viewModel.bookmarksFlow.value.data)

        //Trigger event - add Bookmark
        viewModel.onEvent(BookmarksViewModel.BookmarksEvent.BookmarkItem(mockRemovedBookmark))
        //Wait for load
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.bookmarksFlow.value is ResourceState.Success<List<Bookmark>>)
        newBookmarkList = listOf(mockBookmark)
        assertEquals(newBookmarkList,viewModel.bookmarksFlow.value.data)
    }


}