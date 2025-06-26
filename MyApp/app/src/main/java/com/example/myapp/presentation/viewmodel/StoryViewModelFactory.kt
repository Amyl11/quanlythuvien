package com.example.myapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapp.domain.usecase.GetChapterUseCase
import com.example.myapp.domain.usecase.GetStoryUseCase
import com.example.myapp.domain.usecase.ImportStoryUseCase
import com.example.myapp.domain.usecase.UpdateReadingProgressUseCase

class StoryViewModelFactory(
    private val importStoryUseCase: ImportStoryUseCase,
    private val getStoryUseCase: GetStoryUseCase,
    private val getChapterUseCase: GetChapterUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryViewModel(importStoryUseCase, getStoryUseCase, getChapterUseCase, updateReadingProgressUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}