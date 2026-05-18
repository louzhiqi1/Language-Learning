package com.example.englishreader.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: Long,
    val word: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
