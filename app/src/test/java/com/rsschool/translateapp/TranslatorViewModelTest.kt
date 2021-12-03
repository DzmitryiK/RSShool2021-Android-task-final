package com.rsschool.translateapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.model.Language
import com.rsschool.translateapp.domain.model.SelectedLang
import com.rsschool.translateapp.domain.model.TranslationResult
import com.rsschool.translateapp.domain.use_case.*
import com.rsschool.translateapp.domain.util.ResourceState
import com.rsschool.translateapp.domain.util.TranslationParams
import com.rsschool.translateapp.presentation.viewmodel.TranslatorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class TranslatorViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var getListOfLanguages: GetListOfLanguages
    @Mock
    private lateinit var getTranslation: GetTranslation
    @Mock
    private lateinit var addBookmark: AddBookmark
    @Mock
    private lateinit var removeBookmark: RemoveBookmark
    @Mock
    private lateinit var getBookmark: GetBookmark
    @Mock
    private lateinit var getSelectedLangs: GetSelectedLangs
    @Mock
    private lateinit var updateSourceSelectedLang: UpdateSourceSelectedLang
    @Mock
    private lateinit var updateResultSelectedLang: UpdateResultSelectedLang

    private val mockLanguageList = ResourceState.Success(listOf(Language("tt","testLanguage"),
        Language("tr","testLanguage2")))

    private lateinit var mockTranslationParams : TranslationParams
    private val mockTranslationRes = ResourceState.Success(TranslationResult("TranslationRes"))

    private val mockBookmark = Bookmark(-1,"testLanguage","sourceTranslation",
        "testLanguage2","TranslationRes")
    private var mockBookmarkResLoading = ResourceState.Loading<Long>()
    private val mockRemoveBookmark = Bookmark(1,mockBookmark.sourceLang,mockBookmark.sourceText,
        mockBookmark.resultLang, mockBookmark.resultText)

    private val mockGetBookmarkRes = ResourceState.Success(mockBookmark)

    private val mockGetSelectedLangs = ResourceState.Success(listOf("tt","tr"))

    private lateinit var viewModel: TranslatorViewModel

    @Before
    fun setup(){
        getListOfLanguages = mock(GetListOfLanguages::class.java)
        val mockLanguagesFlow: Flow<ResourceState<List<Language>>> = flow {
            emit(ResourceState.Loading<List<Language>>())
            delay(10)
            emit(mockLanguageList)

        }
        Mockito.`when`(getListOfLanguages.invoke())
            .thenReturn(mockLanguagesFlow)

        getTranslation = mock(GetTranslation::class.java)
        mockTranslationParams = TranslationParams(mockLanguageList.data!![0].code,"sourceTranslation",mockLanguageList.data!![1].code)
        val mockTranslationFlow = flow {
            emit(ResourceState.Loading<TranslationResult>())
            delay(10)
            emit(mockTranslationRes)
        }
        Mockito.`when`(getTranslation.invoke(mockTranslationParams))
            .thenReturn(mockTranslationFlow)

        addBookmark = mock(AddBookmark::class.java)
        val mockAddBookmarkFlow = flow {
            emit(mockBookmarkResLoading)
            delay(10)
            emit(ResourceState.Success(1L))
        }
        Mockito.`when`(addBookmark.invoke(mockBookmark))
            .thenReturn(mockAddBookmarkFlow)

        removeBookmark = mock(RemoveBookmark::class.java)
        val mockRemoveBookmarkFlow = flow {
            emit(mockBookmarkResLoading)
            delay(10)
            emit(ResourceState.Success(-1L))
        }
        Mockito.`when`(removeBookmark.invoke(mockRemoveBookmark))
            .thenReturn(mockRemoveBookmarkFlow)

        getBookmark = mock(GetBookmark::class.java)
        val mockGetBookmarkFlow = flow {
            emit(ResourceState.Loading<Bookmark>())
            delay(10)
            emit(mockGetBookmarkRes)
        }
        Mockito.`when`(getBookmark.invoke(1))
            .thenReturn(mockGetBookmarkFlow)

        getSelectedLangs = mock(GetSelectedLangs::class.java)
        val mockGetSelectedLangsFlow = flow {
            emit(ResourceState.Loading<List<String>>())
            delay(10)
            emit(mockGetSelectedLangs)
        }
        Mockito.`when`(getSelectedLangs.invoke())
            .thenReturn(mockGetSelectedLangsFlow)

        updateSourceSelectedLang = mock(UpdateSourceSelectedLang::class.java)
        updateResultSelectedLang = mock(UpdateResultSelectedLang::class.java)


        viewModel = TranslatorViewModel(getListOfLanguages, getTranslation,
            addBookmark, removeBookmark,
            getBookmark, getSelectedLangs,
            updateSourceSelectedLang, updateResultSelectedLang)
    }

    @Test
    fun getLanguagesTest()= coroutineRule.testDispatcher.runBlockingTest {
        //no action because it is done in init
        //Loading state
        assert(viewModel.languagesListFlow.value is ResourceState.Loading<List<Language>>)
        advanceTimeBy(10)
        //Success state and value
        assertEquals(mockLanguageList,viewModel.languagesListFlow.value)
        //Wait for getSelectedLangs load
        advanceTimeBy(10)
        //Check default selected languages
        assertEquals(SelectedLang(0,"testLanguage"),viewModel.srcLangFlow.value.data)
        assertEquals(SelectedLang(1,"testLanguage2"),viewModel.resLangFlow.value.data)
    }

    @Test
    fun getTranslationTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages to load
        advanceTimeBy(20)
        //Trigger translate event
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextChanged("sourceTranslation"))
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextEnter)
        //Loading state
        assert(viewModel.translateResultFlow.value is ResourceState.Loading<String>)
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.translateResultFlow.value is ResourceState.Success<String>)
        assertEquals(mockTranslationRes.data!!.result,viewModel.translateResultFlow.value.data)
    }

    @Test
    fun changeSourceLangTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages to load
        advanceTimeBy(10)
        //Trigger event
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.SelectSourceLang(1))
        //Success state and value
        assertEquals(SelectedLang(1,"testLanguage2"),viewModel.srcLangFlow.value.data)
    }

    @Test
    fun changeResultLangTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages to load
        advanceTimeBy(10)
        //Trigger translate event
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.SelectResultLang(1))
        //Success state and value
        assertEquals(SelectedLang(1,"testLanguage2"),viewModel.resLangFlow.value.data)
    }

    @Test
    fun swapLanguagesTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages and getSelectedLangs to load
        advanceTimeBy(20)
        //Trigger event
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.SwapLanguages)
        //Success state and value
        assertEquals(SelectedLang(0,"testLanguage"),viewModel.resLangFlow.value.data)
        assertEquals(SelectedLang(1,"testLanguage2"),viewModel.srcLangFlow.value.data)
    }

    @Test
    fun textChangedTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Trigger event
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextChanged(mockBookmark.sourceText))
        //Success state and value
        assertEquals(mockBookmark.sourceText,viewModel.translateQueryFlow.value)
    }

    @Test
    fun clearTextTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages and getSelectedLangs to load
        advanceTimeBy(20)
        //Change text
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextChanged(mockBookmark.sourceText))
        //Trigger event
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.ClearText)
        //Success state and value
        assert(viewModel.translateResultFlow.value is ResourceState.Success<String>)
        assertEquals("",viewModel.translateResultFlow.value.data)
    }

    @Test
    fun bookmarkTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages and getSelectedLangs to load
        advanceTimeBy(20)
        //Load translation
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextChanged(mockBookmark.sourceText))
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextEnter)
        advanceTimeBy(10)
        //Trigger event - add Bookmark
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.Bookmark)
        //Loading state
        assert(viewModel.currentBookmarkFlow.value is ResourceState.Loading<Long>)
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.currentBookmarkFlow.value is ResourceState.Success<Long>)
        assertEquals(mockRemoveBookmark.id,viewModel.currentBookmarkFlow.value.data)


        //Trigger event - remove Bookmark
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.Bookmark)
        //Loading state
        assert(viewModel.currentBookmarkFlow.value is ResourceState.Loading<Long>)
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.currentBookmarkFlow.value is ResourceState.Success<Long>)
        assertEquals(mockBookmark.id,viewModel.currentBookmarkFlow.value.data)
    }

    @Test
    fun checkBookmarkTest()= coroutineRule.testDispatcher.runBlockingTest {
        //Wait for languages and getSelectedLangs to load
        advanceTimeBy(20)
        //Load translation
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextChanged(mockBookmark.sourceText))
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextEnter)
        advanceTimeBy(10)
        //Trigger event - add Bookmark
        viewModel.onEvent(TranslatorViewModel.TranslatorEvent.CheckBookmark)
        //Loading state
        assert(viewModel.currentBookmarkFlow.value is ResourceState.Success<Long>)
        assertEquals(-1L,viewModel.currentBookmarkFlow.value.data)
        advanceTimeBy(10)
        //Success state and value
        assert(viewModel.currentBookmarkFlow.value is ResourceState.Success<Long>)
        assertEquals(mockBookmark.id,viewModel.currentBookmarkFlow.value.data)
    }

}