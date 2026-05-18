package com.example.englishreader.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.englishreader.domain.model.WordStatus

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val word: String,
    val meaning: String,
    val status: WordStatus,
    val language: String = "en",
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val exampleSentence: String = "",
    val nextReviewAt: Long = 0,
    val reviewInterval: Int = 0,
    val easeFactor: Float = 2.5f
)
