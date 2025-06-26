package com.example.myapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val filePath: String, // Path to the DOC file (or internal storage)
    @ColumnInfo(defaultValue = "0") val lastReadChapter: Int = 0,
    @ColumnInfo(defaultValue = "0") val lastReadPosition: Int = 0,
    @ColumnInfo(defaultValue = "0") val totalChapters: Int = 0
)
