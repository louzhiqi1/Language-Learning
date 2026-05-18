package com.example.englishreader.domain.vocabulary

import com.example.englishreader.domain.model.WordStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VocabularyTrackerTest {
    private lateinit var tracker: VocabularyTracker

    @Before
    fun setup() {
        tracker = VocabularyTracker.createForTest(
            getMastered = { listOf("cat", "dog", "run", "the", "is") },
            getWord = { null }
        )
    }

    @Test
    fun `calculates new word ratio correctly`() {
        val storyWords = listOf("cat", "dog", "run", "fly", "jump")
        val ratio = tracker.calculateNewWordRatio(storyWords)
        assertEquals(0.4f, ratio, 0.01f)
    }

    @Test
    fun `identifies new words in story`() {
        val storyWords = listOf("cat", "dog", "fly", "jump")
        val newWords = tracker.findNewWords(storyWords)
        assertEquals(setOf("fly", "jump"), newWords.toSet())
    }

    @Test
    fun `zero ratio when all words are mastered`() {
        val storyWords = listOf("cat", "dog", "run")
        val ratio = tracker.calculateNewWordRatio(storyWords)
        assertEquals(0f, ratio, 0.01f)
    }

    @Test
    fun `handles empty word list`() {
        val ratio = tracker.calculateNewWordRatio(emptyList())
        assertEquals(0f, ratio, 0.01f)
    }

    @Test
    fun `should promote after 2 correct answers`() {
        assertTrue(tracker.shouldPromote(correctCount = 2, wrongCount = 0))
    }

    @Test
    fun `should not promote with 1 correct answer`() {
        assertFalse(tracker.shouldPromote(correctCount = 1, wrongCount = 0))
    }

    @Test
    fun `should demote after 2 wrong answers`() {
        assertTrue(tracker.shouldDemote(correctCount = 0, wrongCount = 2))
    }

    @Test
    fun `should not demote with 1 wrong answer`() {
        assertFalse(tracker.shouldDemote(correctCount = 0, wrongCount = 1))
    }
}
