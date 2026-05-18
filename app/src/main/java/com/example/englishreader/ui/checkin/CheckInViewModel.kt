package com.example.englishreader.ui.checkin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishreader.EnglishReaderApp
import com.example.englishreader.data.db.entity.CheckInEntity
import com.example.englishreader.data.repository.CheckInRepository
import com.example.englishreader.domain.checkin.AchievementEvaluator
import com.example.englishreader.domain.model.Achievement
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CheckInUiState(
    val checkIns: List<CheckInEntity> = emptyList(),
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val unlockedAchievements: Set<Achievement> = emptySet()
)

class CheckInViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EnglishReaderApp
    private val checkInRepo = CheckInRepository(app.database.checkInDao(), app.database.quizResultDao())
    private val evaluator = AchievementEvaluator()

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            checkInRepo.getAllCheckIns().collect { checkIns ->
                val totalPoints = checkIns.sumOf { it.pointsEarned }
                val streak = calculateStreak(checkIns)
                val achievements = evaluator.evaluate(
                    totalStoriesRead = checkIns.sumOf { it.storiesRead },
                    currentStreak = streak,
                    masteredWords = 0,
                    currentLevel = 5
                )
                _uiState.update {
                    it.copy(
                        checkIns = checkIns,
                        totalPoints = totalPoints,
                        currentStreak = streak,
                        unlockedAchievements = achievements
                    )
                }
            }
        }
    }

    private fun calculateStreak(checkIns: List<CheckInEntity>): Int {
        if (checkIns.isEmpty()) return 0
        val sorted = checkIns.sortedByDescending { it.date }
        var streak = 1
        for (i in 0 until sorted.size - 1) {
            val current = LocalDate.parse(sorted[i].date)
            val prev = LocalDate.parse(sorted[i + 1].date)
            if (current.minusDays(1) == prev) streak++ else break
        }
        return streak
    }
}
