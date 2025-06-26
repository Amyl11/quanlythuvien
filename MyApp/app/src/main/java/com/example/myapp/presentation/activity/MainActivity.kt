package com.example.myapp.presentation.activity

import android.content.Intent
import android.os.Bundle

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.myapp.data.database.AppDatabase
import com.example.myapp.data.database.DatabaseProvider

import com.example.myapp.data.entity.Story
import com.example.myapp.data.dao.ChapterDao
import com.example.myapp.data.dao.ReadingProgressDao
import com.example.myapp.data.dao.StoryDao
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.domain.usecase.GetChapterUseCase
import com.example.myapp.domain.usecase.GetStoryUseCase
import com.example.myapp.domain.usecase.ImportStoryUseCase
import com.example.myapp.domain.usecase.UpdateReadingProgressUseCase
import com.example.myapp.presentation.adapter.StoryAdapter
import com.example.myapp.presentation.viewmodel.StoryViewModel
import com.example.myapp.presentation.viewmodel.StoryViewModelFactory
import com.example.myapp.utils.DocxParser

import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var database: AppDatabase
    private lateinit var storyDao: StoryDao
    private lateinit var chapterDao: ChapterDao
    private lateinit var readingProgressDao: ReadingProgressDao
    private lateinit var viewModelFactory: StoryViewModelFactory
    private val storyViewModel: StoryViewModel by viewModels { viewModelFactory }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val contentResolver = applicationContext.contentResolver
                val inputStream = contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val file = File(cacheDir, "temp.docx")
                    inputStream.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val docxParser = DocxParser()
                    val storyTitle = docxParser.getTitle(file)
                    val storyAuthor = docxParser.getAuthor(file)
                    val chapters = docxParser.getChapters(file)
                    val story = Story(
                        title = storyTitle,
                        author = storyAuthor,
                        filePath = file.absolutePath,
                        totalChapters = chapters.size
                    )
                    storyViewModel.importStory(story, chapters)
                    Toast.makeText(this, "Imported story: ${story.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = DatabaseProvider.provideDatabase(this)
        storyDao = database.storyDao()
        chapterDao = database.chapterDao()
        readingProgressDao = database.readingProgressDao()
        viewModelFactory = StoryViewModelFactory(
            ImportStoryUseCase(storyDao, chapterDao),
            GetStoryUseCase(storyDao),
            GetChapterUseCase(chapterDao),
            UpdateReadingProgressUseCase(readingProgressDao, storyDao)
        )

        storyAdapter = StoryAdapter(
            onItemClick = { story ->
                val intent = Intent(this, StoryDetailActivity::class.java)
                intent.putExtra("storyId", story.id)
                startActivity(intent)
            }
        )
        binding.storyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }

        storyViewModel.stories.observe(this, Observer { stories ->
            storyAdapter.submitList(stories)
        })

        storyViewModel.loadStories()

        binding.importButton.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        }
    }
}