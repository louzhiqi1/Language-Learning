package com.example.englishreader.data.repository

import com.example.englishreader.data.db.dao.CheckInDao
import com.example.englishreader.data.db.dao.QuizResultDao
import com.example.englishreader.data.db.entity.CheckInEntity
import com.example.englishreader.data.db.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

class CheckInRepository(
    private val checkInDao: CheckInDao,
    private val quizResultDao: QuizResultDao
) {
    suspend fun saveCheckIn(checkIn: CheckInEntity) = checkInDao.upsert(checkIn)
    suspend fun getToday(date: String): CheckInEntity? = checkInDao.getCheckIn(date)
    fun getAllCheckIns(): Flow<List<CheckInEntity>> = checkInDao.getAllCheckIns()
    suspend fun getRecentCheckIns(days: Int) = checkInDao.getRecentCheckIns(days)
    suspend fun saveQuizResult(result: QuizResultEntity) = quizResultDao.insert(result)
    suspend fun getRecentAccuracy(storyCount: Int): Float = quizResultDao.getRecentAccuracy(storyCount) ?: 0f
}
