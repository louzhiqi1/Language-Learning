package com.example.englishreader.data.db.dao

import androidx.room.*
import com.example.englishreader.data.db.entity.QuizResultEntity

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insert(result: QuizResultEntity)

    @Query("SELECT * FROM quiz_results WHERE storyId = :storyId")
    suspend fun getResultsForStory(storyId: Long): List<QuizResultEntity>

    @Query("SELECT * FROM quiz_results WHERE word = :word ORDER BY timestamp DESC")
    suspend fun getResultsForWord(word: String): List<QuizResultEntity>

    @Query("SELECT AVG(CASE WHEN isCorrect THEN 1.0 ELSE 0.0 END) FROM quiz_results WHERE storyId IN (SELECT id FROM stories ORDER BY createdAt DESC LIMIT :count)")
    suspend fun getRecentAccuracy(count: Int): Float?
}
