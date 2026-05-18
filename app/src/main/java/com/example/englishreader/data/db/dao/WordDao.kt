package com.example.englishreader.data.db.dao

import androidx.room.*
import com.example.englishreader.data.db.entity.WordEntity
import com.example.englishreader.domain.model.WordStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE status = :status")
    suspend fun getWordsByStatus(status: WordStatus): List<WordEntity>

    @Query("SELECT * FROM words WHERE status = 'MASTERED'")
    suspend fun getMasteredWords(): List<WordEntity>

    @Query("SELECT * FROM words WHERE word = :word")
    suspend fun getWord(word: String): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(word: WordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("UPDATE words SET status = :status, lastSeen = :now WHERE word = :word")
    suspend fun updateStatus(word: String, status: WordStatus, now: Long = System.currentTimeMillis())

    @Query("UPDATE words SET correctCount = correctCount + 1, lastSeen = :now WHERE word = :word")
    suspend fun incrementCorrect(word: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE words SET wrongCount = wrongCount + 1, lastSeen = :now WHERE word = :word")
    suspend fun incrementWrong(word: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM words WHERE status = 'MASTERED'")
    fun getMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM words WHERE status = 'LEARNING' AND nextReviewAt <= :now AND nextReviewAt > 0 ORDER BY nextReviewAt ASC")
    suspend fun getWordsForReview(now: Long = System.currentTimeMillis()): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE status = 'LEARNING' AND nextReviewAt <= :now AND nextReviewAt > 0")
    fun getReviewCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("UPDATE words SET nextReviewAt = :nextReview, reviewInterval = :interval, easeFactor = :ease, lastSeen = :now WHERE word = :word")
    suspend fun updateReviewSchedule(word: String, nextReview: Long, interval: Int, ease: Float, now: Long = System.currentTimeMillis())
}
