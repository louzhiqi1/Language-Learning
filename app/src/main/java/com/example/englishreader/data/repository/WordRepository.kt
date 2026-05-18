package com.example.englishreader.data.repository

import com.example.englishreader.data.db.dao.WordDao
import com.example.englishreader.data.db.entity.WordEntity
import com.example.englishreader.domain.model.WordStatus
import com.example.englishreader.domain.vocabulary.ReviewQuality
import com.example.englishreader.domain.vocabulary.SpacedRepetition
import kotlinx.coroutines.flow.Flow

class WordRepository(private val dao: WordDao) {
    suspend fun getMasteredWords(): List<String> =
        dao.getMasteredWords().map { it.word }

    suspend fun getMasteredWordsByLanguage(language: String): List<String> =
        dao.getMasteredWordsByLanguage(language).map { it.word }

    suspend fun getWord(word: String): WordEntity? = dao.getWord(word)

    suspend fun markAsKnown(word: String) {
        val existing = dao.getWord(word)
        if (existing != null) {
            dao.updateStatus(word, WordStatus.MASTERED)
        } else {
            dao.upsert(WordEntity(word = word, meaning = "", status = WordStatus.MASTERED))
        }
    }

    suspend fun markAsLearning(word: String, meaning: String) {
        dao.upsert(WordEntity(word = word, meaning = meaning, status = WordStatus.LEARNING))
    }

    suspend fun addToVocabulary(word: String, meaning: String, exampleSentence: String, language: String = "en") {
        val now = System.currentTimeMillis()
        val nextReview = now + 24 * 60 * 60 * 1000L
        dao.upsert(
            WordEntity(
                word = word,
                meaning = meaning,
                status = WordStatus.LEARNING,
                language = language,
                exampleSentence = exampleSentence,
                nextReviewAt = nextReview,
                reviewInterval = 1,
                easeFactor = 2.5f
            )
        )
    }

    suspend fun recordCorrect(word: String) {
        dao.incrementCorrect(word)
        val w = dao.getWord(word) ?: return
        if (w.correctCount + 1 >= 2 && w.status == WordStatus.LEARNING) {
            dao.updateStatus(word, WordStatus.MASTERED)
        }
    }

    suspend fun recordWrong(word: String) {
        dao.incrementWrong(word)
        val w = dao.getWord(word) ?: return
        if (w.wrongCount + 1 >= 2 && w.status == WordStatus.MASTERED) {
            dao.updateStatus(word, WordStatus.LEARNING)
        }
    }

    suspend fun getWordsForReview(): List<WordEntity> =
        dao.getWordsForReview(System.currentTimeMillis())

    fun getReviewCount(): Flow<Int> =
        dao.getReviewCount(System.currentTimeMillis())

    suspend fun recordReview(word: String, quality: ReviewQuality) {
        val entity = dao.getWord(word) ?: return
        val result = SpacedRepetition.calculateNext(entity, quality)
        dao.updateReviewSchedule(word, result.nextReviewAt, result.newInterval, result.newEaseFactor)
        if (quality != ReviewQuality.AGAIN) {
            dao.incrementCorrect(word)
        } else {
            dao.incrementWrong(word)
        }
    }

    suspend fun insertInitialVocabulary(words: List<WordEntity>) {
        dao.insertAll(words)
    }

    fun getMasteredCount(): Flow<Int> = dao.getMasteredCount()
}
