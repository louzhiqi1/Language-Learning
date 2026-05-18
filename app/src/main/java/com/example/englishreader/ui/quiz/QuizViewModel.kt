package com.example.englishreader.ui.quiz

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.QuizResultEntity
import com.example.englishreader.data.repository.CheckInRepository
import com.example.englishreader.data.repository.StoryRepository
import com.example.englishreader.data.repository.WordRepository
import com.example.englishreader.domain.difficulty.DifficultyAction
import com.example.englishreader.domain.difficulty.DifficultyEngine
import com.example.englishreader.domain.model.QuizQuestion
import com.example.englishreader.domain.model.QuizType
import com.example.englishreader.domain.quiz.QuizGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val isFinished: Boolean = false,
    val totalScore: Float = 0f,
    val levelChanged: DifficultyAction? = null
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val storyRepo = StoryRepository(app.database.storyDao())
    private val wordRepo = WordRepository(app.database.wordDao())
    private val checkInRepo = CheckInRepository(app.database.checkInDao(), app.database.quizResultDao())
    private val difficultyEngine = DifficultyEngine()

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun loadQuiz(storyId: Long) {
        viewModelScope.launch {
            val story = storyRepo.getStory(storyId) ?: return@launch
            val newWords = story.newWords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val generator = QuizGenerator(getMeaning = { word ->
                val entity = wordRepo.getWord(word)
                entity?.meaning?.ifBlank { null }
                    ?: com.example.englishreader.domain.vocabulary.BasicDictionary.getMeaning(word)
                    ?: ""
            })
            val questions = generator.generate(newWords, story.content, maxQuestions = 5)
            _uiState.update { it.copy(questions = questions) }
        }
    }

    fun submitAnswer(answer: String, storyId: Long) {
        val current = _uiState.value
        val question = current.questions.getOrNull(current.currentIndex) ?: return
        val correct = answer == question.correctAnswer

        viewModelScope.launch {
            val word = when (question.type) {
                QuizType.ENGLISH_TO_CHINESE -> question.question
                else -> question.correctAnswer
            }
            if (correct) wordRepo.recordCorrect(word) else wordRepo.recordWrong(word)
            checkInRepo.saveQuizResult(QuizResultEntity(
                storyId = storyId, word = word, isCorrect = correct
            ))
        }

        _uiState.update {
            it.copy(
                selectedAnswer = answer,
                isCorrect = correct,
                correctCount = if (correct) it.correctCount + 1 else it.correctCount
            )
        }
    }

    fun nextQuestion() {
        val current = _uiState.value
        if (current.currentIndex + 1 >= current.questions.size) {
            val score = if (current.questions.isNotEmpty())
                current.correctCount.toFloat() / current.questions.size else 0f
            _uiState.update { it.copy(isFinished = true, totalScore = score) }
            evaluateDifficulty(score)
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedAnswer = null,
                    isCorrect = null
                )
            }
        }
    }

    private fun evaluateDifficulty(score: Float) {
        viewModelScope.launch {
            val currentLevel = prefs.getInt("current_level", 5)
            val scores = getRecentScores(score)
            val action = difficultyEngine.evaluate(currentLevel, scores)
            if (action != DifficultyAction.STAY) {
                val newLevel = when (action) {
                    DifficultyAction.LEVEL_UP -> (currentLevel + 1).coerceAtMost(9)
                    DifficultyAction.LEVEL_DOWN -> (currentLevel - 1).coerceAtLeast(1)
                    else -> currentLevel
                }
                prefs.edit().putInt("current_level", newLevel).apply()
                saveScore(score)
                _uiState.update { it.copy(levelChanged = action) }
            } else {
                saveScore(score)
            }
        }
    }

    private fun getRecentScores(currentScore: Float): List<Float> {
        val saved = prefs.getString("recent_scores", "") ?: ""
        val list = saved.split(",").filter { it.isNotBlank() }.map { it.toFloat() }.toMutableList()
        list.add(currentScore)
        return list.takeLast(10)
    }

    private fun saveScore(score: Float) {
        val saved = prefs.getString("recent_scores", "") ?: ""
        val list = saved.split(",").filter { it.isNotBlank() }.toMutableList()
        list.add(score.toString())
        val trimmed = list.takeLast(10)
        prefs.edit().putString("recent_scores", trimmed.joinToString(",")).apply()
    }
}
