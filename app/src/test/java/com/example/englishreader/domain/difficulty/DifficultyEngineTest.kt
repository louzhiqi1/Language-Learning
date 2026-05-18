package com.example.englishreader.domain.difficulty

import org.junit.Assert.*
import org.junit.Test

class DifficultyEngineTest {
    private val engine = DifficultyEngine()

    @Test
    fun `promotes after 5 consecutive high scores`() {
        val scores = listOf(0.9f, 0.85f, 0.9f, 0.82f, 0.95f)
        val result = engine.evaluate(currentLevel = 5, recentScores = scores)
        assertEquals(DifficultyAction.LEVEL_UP, result)
    }

    @Test
    fun `demotes after 3 consecutive low scores`() {
        val scores = listOf(0.4f, 0.3f, 0.45f)
        val result = engine.evaluate(currentLevel = 6, recentScores = scores)
        assertEquals(DifficultyAction.LEVEL_DOWN, result)
    }

    @Test
    fun `stays same with mixed scores`() {
        val scores = listOf(0.9f, 0.4f, 0.7f, 0.8f, 0.6f)
        val result = engine.evaluate(currentLevel = 5, recentScores = scores)
        assertEquals(DifficultyAction.STAY, result)
    }

    @Test
    fun `never goes below level 1`() {
        val scores = listOf(0.2f, 0.3f, 0.1f)
        val result = engine.evaluate(currentLevel = 1, recentScores = scores)
        assertEquals(DifficultyAction.STAY, result)
    }

    @Test
    fun `returns stay when not enough data`() {
        val scores = listOf(0.9f, 0.9f)
        val result = engine.evaluate(currentLevel = 5, recentScores = scores)
        assertEquals(DifficultyAction.STAY, result)
    }
}
