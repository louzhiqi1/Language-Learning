package com.example.englishreader.data.db.dao

import androidx.room.*
import com.example.englishreader.data.db.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert
    suspend fun insert(story: StoryEntity): Long

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStory(id: Long): StoryEntity?

    @Query("UPDATE stories SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("SELECT COUNT(*) FROM stories WHERE isRead = 1")
    fun getReadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM stories WHERE isRead = 0")
    suspend fun getUnreadCount(): Int

    @Query("SELECT * FROM stories WHERE isRead = 0 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextUnreadStory(): StoryEntity?

    @Query("UPDATE stories SET coverImagePath = :path WHERE id = :id")
    suspend fun updateCoverImage(id: Long, path: String)

    @Query("SELECT * FROM stories WHERE isRead = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentReadStories(limit: Int): List<StoryEntity>
}
