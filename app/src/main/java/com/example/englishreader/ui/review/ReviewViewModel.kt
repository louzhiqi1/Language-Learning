package com.example.englishreader.ui.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.WordEntity
import com.example.englishreader.data.repository.WordRepository
import com.example.englishreader.domain.vocabulary.ReviewQuality
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReviewUiState(
    val words: List<WordEntity> = emptyList(),
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val isComplete: Boolean = false,
    val reviewedCount: Int = 0
)

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val wordRepo = WordRepository(app.database.wordDao())

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            val words = wordRepo.getWordsForReview()
            _uiState.update { it.copy(words = words, isComplete = words.isEmpty()) }
        }
    }

    fun reveal() {
        _uiState.update { it.copy(isRevealed = true) }
    }

    fun rate(quality: ReviewQuality) {
        viewModelScope.launch {
            val current = _uiState.value
            val word = current.words.getOrNull(current.currentIndex) ?: return@launch
            wordRepo.recordReview(word.word, quality)

            val nextIndex = current.currentIndex + 1
            if (nextIndex >= current.words.size) {
                _uiState.update { it.copy(isComplete = true, reviewedCount = nextIndex) }
            } else {
                _uiState.update {
                    it.copy(currentIndex = nextIndex, isRevealed = false, reviewedCount = nextIndex)
                }
            }
        }
    }
}
