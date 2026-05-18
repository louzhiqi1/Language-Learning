package com.example.englishreader.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.StoryEntity
import com.example.englishreader.data.repository.StoryRepository
import kotlinx.coroutines.flow.Flow

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val storyRepo = StoryRepository(app.database.storyDao())

    val stories: Flow<List<StoryEntity>> = storyRepo.getAllStories()
}
