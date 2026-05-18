package com.example.englishreader.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val level: Int,
    val newWords: String,
    val imagePrompts: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val coverImagePath: String = ""
)
