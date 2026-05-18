package com.example.englishreader.domain.difficulty

enum class DifficultyAction { LEVEL_UP, LEVEL_DOWN, STAY }

class DifficultyEngine {
    fun evaluate(currentLevel: Int, recentScores: List<Float>): DifficultyAction {
        if (recentScores.size >= 5 && recentScores.takeLast(5).all { it > 0.8f }) {
            return DifficultyAction.LEVEL_UP
        }
        if (recentScores.size >= 3 && recentScores.takeLast(3).all { it < 0.5f }) {
            if (currentLevel <= 1) return DifficultyAction.STAY
            return DifficultyAction.LEVEL_DOWN
        }
        return DifficultyAction.STAY
    }
}
