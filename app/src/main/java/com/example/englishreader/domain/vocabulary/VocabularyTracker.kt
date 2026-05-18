package com.example.englishreader.domain.vocabulary

import com.example.englishreader.domain.model.WordStatus

data class WordInfo(
    val word: String,
    val status: WordStatus,
    val correctCount: Int,
    val wrongCount: Int
)

class VocabularyTracker(
    private val getMasteredSuspend: suspend () -> List<String>,
    private val getWordSuspend: suspend (String) -> WordInfo?
) {
    private var masteredCache: Set<String> = emptySet()

    suspend fun refreshCache() {
        masteredCache = getMasteredSuspend().map { it.lowercase() }.toSet()
    }

    fun getMasteredList(): List<String> = masteredCache.toList()

    fun calculateNewWordRatio(storyWords: List<String>): Float {
        val unique = storyWords.map { it.lowercase() }.distinct()
        if (unique.isEmpty()) return 0f
        val newCount = unique.count { it !in masteredCache }
        return newCount.toFloat() / unique.size
    }

    fun findNewWords(storyWords: List<String>): List<String> {
        return storyWords.map { it.lowercase() }.distinct()
            .filter { it !in masteredCache }
    }

    fun shouldPromote(correctCount: Int, wrongCount: Int): Boolean = correctCount >= 2

    fun shouldDemote(correctCount: Int, wrongCount: Int): Boolean = wrongCount >= 2

    companion object {
        fun createForTest(
            getMastered: () -> List<String>,
            getWord: (String) -> WordInfo?
        ): VocabularyTracker {
            val tracker = VocabularyTracker(
                getMasteredSuspend = { getMastered() },
                getWordSuspend = { getWord(it) }
            )
            tracker.masteredCache = getMastered().map { it.lowercase() }.toSet()
            return tracker
        }
    }
}
