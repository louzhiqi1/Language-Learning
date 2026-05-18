package com.example.englishreader.data.repository

import com.example.englishreader.data.db.dao.StoryDao
import com.example.englishreader.data.db.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

class StoryRepository(private val dao: StoryDao) {
    suspend fun save(story: StoryEntity): Long = dao.insert(story)
    suspend fun getStory(id: Long): StoryEntity? = dao.getStory(id)
    suspend fun markAsRead(id: Long) = dao.markAsRead(id)
    fun getAllStories(): Flow<List<StoryEntity>> = dao.getAllStories()
    fun getReadCount(): Flow<Int> = dao.getReadCount()
    suspend fun getUnreadCount(): Int = dao.getUnreadCount()
    suspend fun getNextUnreadStory(): StoryEntity? = dao.getNextUnreadStory()
    suspend fun updateCoverImage(id: Long, path: String) = dao.updateCoverImage(id, path)
}
