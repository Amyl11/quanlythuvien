package com.example.myapp.domain.usecase

import com.example.myapp.data.dao.ChapterDao
import com.example.myapp.data.dao.ReadingProgressDao
import com.example.myapp.data.dao.StoryDao
import com.example.myapp.data.entity.Chapter
import com.example.myapp.data.entity.ReadingProgress
import com.example.myapp.data.entity.Story
import kotlinx.coroutines.flow.Flow

class ImportStoryUseCase(private val storyDao: StoryDao, private val chapterDao: ChapterDao) {
    suspend operator fun invoke(story: Story, chapters: List<Chapter>) {

        val storyId = storyDao.insertStory(story)

        val chaptersWithStoryId = chapters.map { chapter ->
            chapter.copy(storyId = storyId.toInt())
        }

        chapterDao.insertChapters(chaptersWithStoryId)
    }
}

class GetStoryUseCase(private val storyDao: StoryDao) {
    operator fun invoke(): Flow<List<Story>> {
        return storyDao.getAllStories()
    }
    suspend fun getStoryById(storyId: Int): Story? {
        return storyDao.getStoryById(storyId)
    }
    fun searchStories(query: String): Flow<List<Story>> {
        return storyDao.searchStories(query)
    }
}

class GetChapterUseCase(private val chapterDao: ChapterDao) {
    operator fun invoke(storyId: Int): Flow<List<Chapter>> {
        return chapterDao.getChaptersByStoryId(storyId)
    }
    suspend fun getChapterByNumber(storyId: Int, chapterNumber: Int): Chapter? {
        return chapterDao.getChapterByNumber(storyId, chapterNumber)
    }
}

class UpdateReadingProgressUseCase(private val readingProgressDao: ReadingProgressDao, private val storyDao: StoryDao) {
    suspend operator fun invoke(storyId: Int, lastReadChapter: Int, lastReadPosition: Int) {
        val readingProgress = readingProgressDao.getReadingProgress(storyId)
        if (readingProgress == null) {
            readingProgressDao.insertReadingProgress(ReadingProgress(storyId, lastReadChapter, lastReadPosition))
        } else {
            readingProgressDao.updateReadingProgress(readingProgress.copy(lastReadChapter = lastReadChapter, lastReadPosition = lastReadPosition))
        }

        val story = storyDao.getStoryById(storyId)
        if (story != null) {
            storyDao.updateStory(story.copy(lastReadChapter = lastReadChapter, lastReadPosition = lastReadPosition))
        }
    }
    suspend fun getReadingProgress(storyId: Int): ReadingProgress? {
        return readingProgressDao.getReadingProgress(storyId)
    }
}

class DeleteStoryUseCase(private val storyDao: StoryDao) {

    suspend operator fun invoke(story: Story) {
        storyDao.deleteStory(story)
    }
}