package com.example.englishreader.domain.vocabulary

import com.example.englishreader.data.db.entity.WordEntity
import kotlin.math.max
import kotlin.math.roundToInt

enum class ReviewQuality { AGAIN, GOOD, EASY }

data class ReviewResult(
    val nextReviewAt: Long,
    val newInterval: Int,
    val newEaseFactor: Float
)

object SpacedRepetition {
    private const val DAY_MS = 24 * 60 * 60 * 1000L

    fun calculateNext(word: WordEntity, quality: ReviewQuality): ReviewResult {
        val now = System.currentTimeMillis()
        var interval = word.reviewInterval
        var ease = word.easeFactor

        when (quality) {
            ReviewQuality.AGAIN -> {
                interval = 1
                ease = max(1.3f, ease - 0.2f)
            }
            ReviewQuality.GOOD -> {
                interval = when {
                    interval == 0 -> 1
                    interval == 1 -> 3
                    else -> (interval * ease).roundToInt()
                }
            }
            ReviewQuality.EASY -> {
                interval = when {
                    interval == 0 -> 3
                    interval <= 1 -> 7
                    else -> (interval * ease * 1.3f).roundToInt()
                }
                ease += 0.1f
            }
        }

        val nextReview = now + interval * DAY_MS
        return ReviewResult(nextReview, interval, ease)
    }
}
