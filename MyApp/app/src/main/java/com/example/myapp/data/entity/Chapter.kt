package com.example.myapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storyId: Int,
    val chapterNumber: Int,
    val content: String
)
