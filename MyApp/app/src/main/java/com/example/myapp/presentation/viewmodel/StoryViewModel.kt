package com.example.myapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.entity.Chapter
import com.example.myapp.data.entity.ReadingProgress
import com.example.myapp.data.entity.Story
import com.example.myapp.domain.usecase.DeleteStoryUseCase
import com.example.myapp.domain.usecase.GetChapterUseCase
import com.example.myapp.domain.usecase.GetStoryUseCase
import com.example.myapp.domain.usecase.ImportStoryUseCase
import com.example.myapp.domain.usecase.UpdateReadingProgressUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.text.isBlank

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

class StoryViewModel(
    private val importStoryUseCase: ImportStoryUseCase,
    private val getStoryUseCase: GetStoryUseCase,
    private val getChapterUseCase: GetChapterUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val deleteStoryUseCase: DeleteStoryUseCase
) : ViewModel() {

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> = _chapters

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val displayedStories: LiveData<List<Story>> = _searchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                getStoryUseCase()
            } else {
                getStoryUseCase.searchStories(query)
            }
        }
        .asLiveData()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _readingProgress = MutableLiveData<ReadingProgress?>()
    val readingProgress: LiveData<ReadingProgress?> = _readingProgress

    fun importStory(story: Story, chapters: List<Chapter>) {
        viewModelScope.launch {
            importStoryUseCase(story, chapters)
        }
    }

    fun loadStories() {
        viewModelScope.launch {
            getStoryUseCase().collectLatest {
                _stories.value = it
            }
        }
    }

    fun deleteStory(story: Story) {
        viewModelScope.launch {
            deleteStoryUseCase(story)
        }
    }

    fun loadChapters(storyId: Int) {
        viewModelScope.launch {
            getChapterUseCase(storyId).collectLatest {
                _chapters.value = it
            }
        }
    }

    fun updateReadingProgress(storyId: Int, lastReadChapter: Int, lastReadPosition: Int) {
        viewModelScope.launch {
            updateReadingProgressUseCase(storyId, lastReadChapter, lastReadPosition)
        }
    }

    fun loadReadingProgress(storyId: Int) {
        viewModelScope.launch {
            _readingProgress.value = updateReadingProgressUseCase.getReadingProgress(storyId)
        }
    }
    suspend fun getStoryById(storyId: Int): Story? {
        return getStoryUseCase.getStoryById(storyId)
    }
    suspend fun getChapterByNumber(storyId: Int, chapterNumber: Int): Chapter? {
        return getChapterUseCase.getChapterByNumber(storyId, chapterNumber)
    }
}