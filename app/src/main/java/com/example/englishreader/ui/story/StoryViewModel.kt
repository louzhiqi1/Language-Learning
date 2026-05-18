package com.example.englishreader.ui.story

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.StoryEntity
import com.example.englishreader.data.db.entity.WordEntity
import com.example.englishreader.data.repository.StoryRepository
import com.example.englishreader.data.repository.WordRepository
import com.example.englishreader.domain.model.WordStatus
import com.example.englishreader.domain.story.StoryParser
import com.example.englishreader.domain.vocabulary.BasicDictionary
import com.example.englishreader.inference.PiperTtsEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class StoryUiState(
    val story: StoryEntity? = null,
    val sentenceGroups: List<String> = emptyList(),
    val images: Map<Int, Bitmap> = emptyMap(),
    val isLoadingImages: Boolean = false,
    val isPlaying: Boolean = false,
    val currentSentenceIndex: Int = -1,
    val ttsEnabled: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val selectedWord: String? = null,
    val selectedWordMeaning: String? = null,
    val selectedWordExample: String? = null,
    val selectedWordInVocab: Boolean = false
)

class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val storyRepo = StoryRepository(app.database.storyDao())
    private val wordRepo = WordRepository(app.database.wordDao())
    private val parser = StoryParser()
    private val tts = app.ttsEngine

    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    init {
        if (!tts.isLoaded) tts.load()
    }

    fun loadStory(storyId: Long) {
        viewModelScope.launch {
            val story = storyRepo.getStory(storyId) ?: return@launch
            val groups = parser.splitIntoGroups(story.content, 4)
            _uiState.update { it.copy(story = story, sentenceGroups = groups) }
            storyRepo.markAsRead(storyId)
            registerNewWords(story)
            val locale = if (story.language == "ja") Locale.JAPANESE else Locale.US
            tts.setLanguage(locale)
        }
    }

    private suspend fun registerNewWords(story: StoryEntity) {
        val newWords = story.newWords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        for (word in newWords) {
            val existing = wordRepo.getWord(word)
            if (existing == null) {
                val meaning = BasicDictionary.getMeaning(word) ?: ""
                val example = findSentenceWith(word, story.content)
                wordRepo.addToVocabulary(word, meaning, example)
            }
        }
    }

    private fun findSentenceWith(word: String, content: String): String {
        val sentences = content.split(Regex("(?<=[.!?])\\s+"))
        return sentences.firstOrNull { it.lowercase().contains(word.lowercase()) } ?: ""
    }

    fun onWordLongPress(word: String, sentence: String) {
        viewModelScope.launch {
            val isJapanese = _uiState.value.story?.language == "ja"
            val cleanWord = if (isJapanese) {
                word.trim()
            } else {
                word.lowercase().replace(Regex("[^a-z']"), "")
            }
            if (cleanWord.isBlank()) return@launch
            val entity = wordRepo.getWord(cleanWord)
            val meaning = entity?.meaning?.ifBlank { null }
                ?: BasicDictionary.getMeaning(cleanWord)
            _uiState.update {
                it.copy(
                    selectedWord = cleanWord,
                    selectedWordMeaning = meaning,
                    selectedWordExample = sentence,
                    selectedWordInVocab = entity != null
                )
            }
            speakWord(cleanWord)
        }
    }

    fun dismissWordPopup() {
        _uiState.update {
            it.copy(selectedWord = null, selectedWordMeaning = null, selectedWordExample = null)
        }
    }

    fun addToVocabulary(word: String) {
        viewModelScope.launch {
            val meaning = _uiState.value.selectedWordMeaning ?: BasicDictionary.getMeaning(word) ?: ""
            val example = _uiState.value.selectedWordExample ?: ""
            wordRepo.addToVocabulary(word, meaning, example)
            _uiState.update { it.copy(selectedWordInVocab = true) }
            dismissWordPopup()
        }
    }

    fun markWordAsKnown(word: String) {
        viewModelScope.launch {
            wordRepo.markAsKnown(word)
            dismissWordPopup()
        }
    }

    fun toggleTts() {
        _uiState.update { it.copy(ttsEnabled = !it.ttsEnabled) }
        if (!_uiState.value.ttsEnabled) tts.stop()
    }

    fun setTtsSpeed(speed: Float) {
        tts.setSpeed(speed)
        _uiState.update { it.copy(ttsSpeed = speed) }
    }

    fun speakSentence(text: String) {
        if (!_uiState.value.ttsEnabled) return
        tts.speakAsync(text)
    }

    fun speakWord(word: String) {
        tts.speakAsync(word)
    }

    override fun onCleared() {
        super.onCleared()
        tts.stop()
    }
}
