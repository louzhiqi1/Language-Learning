package com.example.englishreader.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.QuizResultEntity
import com.example.englishreader.data.repository.CheckInRepository
import com.example.englishreader.data.repository.StoryRepository
import com.example.englishreader.data.repository.WordRepository
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
    val totalScore: Float = 0f
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val storyRepo = StoryRepository(app.database.storyDao())
    private val wordRepo = WordRepository(app.database.wordDao())
    private val checkInRepo = CheckInRepository(app.database.checkInDao(), app.database.quizResultDao())

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

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
}
