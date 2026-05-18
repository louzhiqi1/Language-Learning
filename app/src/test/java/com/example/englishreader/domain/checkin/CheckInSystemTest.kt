package com.example.englishreader.domain.checkin

import org.junit.Assert.*
import org.junit.Test

class CheckInSystemTest {
    private val system = CheckInSystem()

    @Test
    fun `calculates points for story completion`() {
        val points = system.calculatePoints(storiesRead = 1, quizAllCorrect = false)
        assertEquals(10, points)
    }

    @Test
    fun `adds bonus for perfect quiz`() {
        val points = system.calculatePoints(storiesRead = 1, quizAllCorrect = true)
        assertEquals(15, points)
    }

    @Test
    fun `adds streak bonus for 3 days`() {
        val bonus = system.streakBonus(streak = 3)
        assertEquals(20, bonus)
    }

    @Test
    fun `adds streak bonus for 7 days`() {
        val bonus = system.streakBonus(streak = 7)
        assertEquals(50, bonus)
    }

    @Test
    fun `no streak bonus for 2 days`() {
        val bonus = system.streakBonus(streak = 2)
        assertEquals(0, bonus)
    }

    @Test
    fun `multiple stories multiply points`() {
        val points = system.calculatePoints(storiesRead = 3, quizAllCorrect = false)
        assertEquals(30, points)
    }
}
