package com.example.englishreader.domain.model

enum class QuizType { ENGLISH_TO_CHINESE, FILL_BLANK, IMAGE_WORD }

data class QuizQuestion(
    val type: QuizType,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val imageIndex: Int? = null
)
