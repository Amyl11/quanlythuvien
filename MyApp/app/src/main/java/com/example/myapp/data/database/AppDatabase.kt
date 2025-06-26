package com.example.myapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapp.data.dao.ChapterDao
import com.example.myapp.data.dao.ReadingProgressDao
import com.example.myapp.data.dao.StoryDao
import com.example.myapp.data.entity.Chapter
import com.example.myapp.data.entity.ReadingProgress
import com.example.myapp.data.entity.Story

@Database(entities = [Story::class, Chapter::class, ReadingProgress::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingProgressDao(): ReadingProgressDao
}