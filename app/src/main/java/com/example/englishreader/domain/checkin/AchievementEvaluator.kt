package com.example.englishreader.domain.checkin

import com.example.englishreader.domain.model.Achievement

class AchievementEvaluator {
    fun evaluate(
        totalStoriesRead: Int,
        currentStreak: Int,
        masteredWords: Int,
        currentLevel: Int
    ): Set<Achievement> {
        val unlocked = mutableSetOf<Achievement>()

        if (totalStoriesRead >= 1) unlocked.add(Achievement.FIRST_STORY)
        if (totalStoriesRead >= 10) unlocked.add(Achievement.STORIES_10)
        if (totalStoriesRead >= 50) unlocked.add(Achievement.STORIES_50)
        if (totalStoriesRead >= 100) unlocked.add(Achievement.STORIES_100)

        if (currentStreak >= 7) unlocked.add(Achievement.STREAK_7)
        if (currentStreak >= 30) unlocked.add(Achievement.STREAK_30)

        if (masteredWords >= 100) unlocked.add(Achievement.VOCAB_100)
        if (masteredWords >= 300) unlocked.add(Achievement.VOCAB_300)
        if (masteredWords >= 500) unlocked.add(Achievement.VOCAB_500)
        if (masteredWords >= 1000) unlocked.add(Achievement.VOCAB_1000)

        if (currentLevel >= 6) unlocked.add(Achievement.LEVEL_UP_6)
        if (currentLevel >= 7) unlocked.add(Achievement.LEVEL_UP_7)
        if (currentLevel >= 8) unlocked.add(Achievement.LEVEL_UP_8)

        return unlocked
    }
}
