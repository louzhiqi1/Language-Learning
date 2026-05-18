package com.example.englishreader.data.db.dao

import androidx.room.*
import com.example.englishreader.data.db.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(checkIn: CheckInEntity)

    @Query("SELECT * FROM check_ins WHERE date = :date")
    suspend fun getCheckIn(date: String): CheckInEntity?

    @Query("SELECT * FROM check_ins ORDER BY date DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY date DESC LIMIT :days")
    suspend fun getRecentCheckIns(days: Int): List<CheckInEntity>
}
