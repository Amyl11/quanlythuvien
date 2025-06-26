package com.example.myapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey val storyId: Int,
    val lastReadChapter: Int,
    val lastReadPosition: Int
)