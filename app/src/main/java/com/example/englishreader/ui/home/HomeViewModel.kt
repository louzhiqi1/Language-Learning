package com.example.englishreader.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.StoryEntity
import com.example.englishreader.data.repository.StoryRepository
import com.example.englishreader.data.repository.WordRepository
import com.example.englishreader.domain.model.Language
import com.example.englishreader.domain.story.StoryGenerator
import com.example.englishreader.domain.vocabulary.VocabularyTracker
import com.example.englishreader.inference.LlamaInference
import com.example.englishreader.inference.MnnImageGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

data class HomeUiState(
    val isGenerating: Boolean = false,
    val currentStory: StoryEntity? = null,
    val masteredWordCount: Int = 0,
    val currentLevel: Int = 5,
    val currentLanguage: Language = Language.ENGLISH,
    val todayCheckedIn: Boolean = false,
    val reviewDueCount: Int = 0,
    val unreadCount: Int = 0,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val wordRepo = WordRepository(app.database.wordDao())
    private val storyRepo = StoryRepository(app.database.storyDao())
    private val generationMutex = Mutex()
    private val imageGenerator = MnnImageGenerator(application)
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigateToStory = MutableSharedFlow<Long>()
    val navigateToStory: SharedFlow<Long> = _navigateToStory.asSharedFlow()

    companion object {
        private const val MIN_UNREAD_THRESHOLD = 3
        private const val BATCH_GENERATE_COUNT = 3
    }

    init {
        viewModelScope.launch {
            val level = prefs.getInt("current_level", 5)
            val lang = Language.fromCode(prefs.getString("language", "en") ?: "en")
            _uiState.update { it.copy(currentLevel = level, currentLanguage = lang) }
            wordRepo.getMasteredCount().collect { count ->
                _uiState.update { it.copy(masteredWordCount = count) }
            }
        }
        viewModelScope.launch {
            wordRepo.getReviewCount().collect { count ->
                _uiState.update { it.copy(reviewDueCount = count) }
            }
        }
        viewModelScope.launch {
            refreshUnreadCount()
            ensureStoryQueue()
        }
    }

    fun readNextStory() {
        viewModelScope.launch {
            val next = storyRepo.getNextUnreadStory()
            if (next != null) {
                _navigateToStory.emit(next.id)
            } else {
                generateSingleStory()
            }
            refreshUnreadCount()
            ensureStoryQueue()
        }
    }

    fun onReturnFromReading() {
        viewModelScope.launch {
            val level = prefs.getInt("current_level", 5)
            val lang = Language.fromCode(prefs.getString("language", "en") ?: "en")
            _uiState.update { it.copy(currentLevel = level, currentLanguage = lang) }
            refreshUnreadCount()
            ensureStoryQueue()
        }
    }

    private suspend fun refreshUnreadCount() {
        val count = storyRepo.getUnreadCount()
        _uiState.update { it.copy(unreadCount = count) }
    }

    private fun ensureStoryQueue() {
        viewModelScope.launch {
            if (generationMutex.isLocked) return@launch
            val unread = storyRepo.getUnreadCount()
            if (unread <= MIN_UNREAD_THRESHOLD) {
                val toGenerate = BATCH_GENERATE_COUNT
                generateStoriesInBackground(toGenerate)
            }
        }
    }

    private suspend fun generateStoriesInBackground(count: Int) {
        generationMutex.withLock {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val llama = LlamaInference(getApplication())
                llama.load("Qwen3-4B-Q4_K_M.gguf", nGpuLayers = 0)

                val tracker = VocabularyTracker(
                    getMasteredSuspend = { wordRepo.getMasteredWords() },
                    getWordSuspend = { null }
                )
                val generator = StoryGenerator(llama, tracker)

                val coverTasks = mutableListOf<Pair<Long, String>>()
                val language = _uiState.value.currentLanguage
                repeat(count) {
                    try {
                        val parsed = generator.generate(_uiState.value.currentLevel, language)
                        android.util.Log.i("StoryGen", "TITLE=[${parsed.title}] CONTENT_LEN=${parsed.content.length} WORDS=${parsed.newWords}")
                        android.util.Log.i("StoryGen", "CONTENT_FIRST100=[${parsed.content.take(100)}]")
                        val entity = StoryEntity(
                            title = parsed.title,
                            content = parsed.content,
                            level = _uiState.value.currentLevel,
                            newWords = parsed.newWords.joinToString(","),
                            imagePrompts = parsed.imagePrompts.joinToString("|")
                        )
                        val id = storyRepo.save(entity)
                        refreshUnreadCount()
                        coverTasks.add(id to (parsed.imagePrompts.firstOrNull() ?: parsed.title))
                    } catch (_: Exception) { }
                }

                llama.unload()

                // Cover generation disabled - SD 1.5 model too large for in-process use
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private suspend fun generateSingleStory() {
        _uiState.update { it.copy(isGenerating = true, error = null) }
        try {
            val llama = LlamaInference(getApplication())
            llama.load("Qwen3-4B-Q4_K_M.gguf", nGpuLayers = 0)

            val tracker = VocabularyTracker(
                getMasteredSuspend = { wordRepo.getMasteredWords() },
                getWordSuspend = { null }
            )
            val generator = StoryGenerator(llama, tracker)
            val parsed = generator.generate(_uiState.value.currentLevel, _uiState.value.currentLanguage)

            llama.unload()

            val entity = StoryEntity(
                title = parsed.title,
                content = parsed.content,
                level = _uiState.value.currentLevel,
                newWords = parsed.newWords.joinToString(","),
                imagePrompts = parsed.imagePrompts.joinToString("|")
            )
            val id = storyRepo.save(entity)
            _uiState.update { it.copy(isGenerating = false) }
            _navigateToStory.emit(id)
        } catch (e: Exception) {
            _uiState.update { it.copy(isGenerating = false, error = e.message) }
        }
    }

    private suspend fun generateCoverImage(storyId: Long, prompt: String) {
        try {
            if (!imageGenerator.isModelAvailable()) return
            val coverDir = File(getApplication<EnglishReaderApp>().filesDir, "covers")
            coverDir.mkdirs()
            val outputPath = File(coverDir, "cover_$storyId.png").absolutePath
            val success = imageGenerator.generate(prompt, outputPath)
            if (success) {
                storyRepo.updateCoverImage(storyId, outputPath)
            }
        } catch (_: Exception) { }
    }
}
