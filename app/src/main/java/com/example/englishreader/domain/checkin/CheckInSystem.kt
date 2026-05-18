package com.example.englishreader.domain.checkin

class CheckInSystem {
    fun calculatePoints(storiesRead: Int, quizAllCorrect: Boolean): Int {
        var points = storiesRead * 10
        if (quizAllCorrect) points += 5
        return points
    }

    fun streakBonus(streak: Int): Int = when {
        streak >= 7 && streak % 7 == 0 -> 50
        streak >= 3 && streak % 3 == 0 -> 20
        else -> 0
    }
}
