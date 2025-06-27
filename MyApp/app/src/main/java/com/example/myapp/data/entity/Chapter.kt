package com.example.myapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Story::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],

    indices = [Index(value = ["storyId"])]
)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storyId: Int,
    val chapterNumber: Int,
    val content: String
)
