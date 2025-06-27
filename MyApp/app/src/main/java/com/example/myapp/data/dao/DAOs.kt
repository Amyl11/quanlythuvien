package com.example.myapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapp.data.entity.Chapter
import com.example.myapp.data.entity.ReadingProgress
import com.example.myapp.data.entity.Story
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story): Long

    @Query("SELECT * FROM stories")
    fun getAllStories(): Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: Int): Story?

    @Query("SELECT * FROM stories WHERE " +
            "title LIKE '%' || :query || '%' OR " +
            "author LIKE '%' || :query || '%' OR " +
            "genre LIKE '%' || :query || '%' " +
            "ORDER BY title ASC")
    fun searchStories(query: String): Flow<List<Story>>

    @Update
    suspend fun updateStory(story: Story)

    @Delete
    suspend fun deleteStory(story: Story)
}

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY chapterNumber")
    fun getChaptersByStoryId(storyId: Int): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE storyId = :storyId AND chapterNumber = :chapterNumber")
    suspend fun getChapterByNumber(storyId: Int, chapterNumber: Int): Chapter?
}

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgress(readingProgress: ReadingProgress)

    @Query("SELECT * FROM reading_progress WHERE storyId = :storyId")
    suspend fun getReadingProgress(storyId: Int): ReadingProgress?

    @Update
    suspend fun updateReadingProgress(readingProgress: ReadingProgress)
}