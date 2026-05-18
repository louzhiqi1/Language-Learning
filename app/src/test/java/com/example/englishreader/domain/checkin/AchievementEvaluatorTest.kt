package com.example.englishreader.domain.checkin

import com.example.englishreader.domain.model.Achievement
import org.junit.Assert.*
import org.junit.Test

class AchievementEvaluatorTest {
    private val evaluator = AchievementEvaluator()

    @Test
    fun `unlocks first read achievement`() {
        val unlocked = evaluator.evaluate(
            totalStoriesRead = 1, currentStreak = 1,
            masteredWords = 10, currentLevel = 5
        )
        assertTrue(unlocked.contains(Achievement.FIRST_STORY))
    }

    @Test
    fun `unlocks 7 day streak`() {
        val unlocked = evaluator.evaluate(
            totalStoriesRead = 10, currentStreak = 7,
            masteredWords = 50, currentLevel = 5
        )
        assertTrue(unlocked.contains(Achievement.STREAK_7))
    }

    @Test
    fun `unlocks vocab 100`() {
        val unlocked = evaluator.evaluate(
            totalStoriesRead = 20, currentStreak = 3,
            masteredWords = 100, currentLevel = 5
        )
        assertTrue(unlocked.contains(Achievement.VOCAB_100))
    }

    @Test
    fun `unlocks level up achievements`() {
        val unlocked = evaluator.evaluate(
            totalStoriesRead = 30, currentStreak = 5,
            masteredWords = 200, currentLevel = 7
        )
        assertTrue(unlocked.contains(Achievement.LEVEL_UP_6))
        assertTrue(unlocked.contains(Achievement.LEVEL_UP_7))
        assertFalse(unlocked.contains(Achievement.LEVEL_UP_8))
    }

    @Test
    fun `does not unlock unearned achievements`() {
        val unlocked = evaluator.evaluate(
            totalStoriesRead = 1, currentStreak = 1,
            masteredWords = 10, currentLevel = 5
        )
        assertFalse(unlocked.contains(Achievement.STREAK_30))
        assertFalse(unlocked.contains(Achievement.VOCAB_1000))
        assertFalse(unlocked.contains(Achievement.STORIES_100))
    }
}
