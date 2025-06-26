package com.example.myapp.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "story_reader_database"
        ).build()
    }
}