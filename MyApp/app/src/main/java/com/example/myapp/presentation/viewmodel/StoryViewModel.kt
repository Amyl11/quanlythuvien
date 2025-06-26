package com.example.myapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.entity.Chapter
import com.example.myapp.data.entity.ReadingProgress
import com.example.myapp.data.entity.Story
import com.example.myapp.domain.usecase.GetChapterUseCase
import com.example.myapp.domain.usecase.GetStoryUseCase
import com.example.myapp.domain.usecase.ImportStoryUseCase
import com.example.myapp.domain.usecase.UpdateReadingProgressUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryViewModel(
    private val importStoryUseCase: ImportStoryUseCase,
    private val getStoryUseCase: GetStoryUseCase,
    private val getChapterUseCase: GetChapterUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase
) : ViewModel() {

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> = _chapters

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