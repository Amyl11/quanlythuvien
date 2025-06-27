package com.example.myapp.presentation.activity

import android.os.Bundle
import android.text.method.ScrollingMovementMethod

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

import com.example.myapp.data.database.AppDatabase
import com.example.myapp.data.database.DatabaseProvider
import com.example.myapp.data.dao.ChapterDao
import com.example.myapp.data.dao.ReadingProgressDao
import com.example.myapp.data.dao.StoryDao
import com.example.myapp.databinding.ActivityStoryDetailBinding
import com.example.myapp.domain.usecase.DeleteStoryUseCase
import com.example.myapp.domain.usecase.GetChapterUseCase
import com.example.myapp.domain.usecase.GetStoryUseCase
import com.example.myapp.domain.usecase.ImportStoryUseCase
import com.example.myapp.domain.usecase.UpdateReadingProgressUseCase
import com.example.myapp.presentation.viewmodel.StoryViewModel
import com.example.myapp.presentation.viewmodel.StoryViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding
    private lateinit var database: AppDatabase
    private lateinit var storyDao: StoryDao
    private lateinit var chapterDao: ChapterDao
    private lateinit var readingProgressDao: ReadingProgressDao
    private lateinit var viewModelFactory: StoryViewModelFactory
    private val storyViewModel: StoryViewModel by viewModels { viewModelFactory }
    private var storyId: Int = 0
    private var currentChapterNumber: Int = 1
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = DatabaseProvider.provideDatabase(this)
        storyDao = database.storyDao()
        chapterDao = database.chapterDao()
        readingProgressDao = database.readingProgressDao()
        viewModelFactory = StoryViewModelFactory(
            ImportStoryUseCase(storyDao, chapterDao),
            GetStoryUseCase(storyDao),
            GetChapterUseCase(chapterDao),
            UpdateReadingProgressUseCase(readingProgressDao, storyDao),
            DeleteStoryUseCase(storyDao)
        )

        storyId = intent.getIntExtra("storyId", 0)
        storyViewModel.loadChapters(storyId)
        storyViewModel.loadReadingProgress(storyId)

        binding.storyContentTextView.movementMethod = ScrollingMovementMethod()

        storyViewModel.chapters.observe(this, Observer { chapters ->
            if (chapters.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val chapter = storyViewModel.getChapterByNumber(storyId, currentChapterNumber)
                    if (chapter != null) {
                        binding.storyContentTextView.text = chapter.content
                    }
                }
            }
        })
        storyViewModel.readingProgress.observe(this, Observer { readingProgress ->
            if (readingProgress != null) {
                currentChapterNumber = readingProgress.lastReadChapter
                currentPosition = readingProgress.lastReadPosition
                binding.storyContentTextView.post {
                    binding.storyContentTextView.scrollTo(0, currentPosition)
                }
            }
        })

        binding.nextChapterButton.setOnClickListener {
            currentChapterNumber++
            loadChapter()
        }

        binding.previousChapterButton.setOnClickListener {
            if (currentChapterNumber > 1) {
                currentChapterNumber--
                loadChapter()
            }
        }
    }

    private fun loadChapter() {
        CoroutineScope(Dispatchers.Main).launch {
            val chapter = storyViewModel.getChapterByNumber(storyId, currentChapterNumber)
            if (chapter != null) {
                binding.storyContentTextView.text = chapter.content
                binding.storyContentTextView.scrollTo(0, 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        currentPosition = binding.storyContentTextView.scrollY
        storyViewModel.updateReadingProgress(storyId, currentChapterNumber, currentPosition)
    }
}