package com.example.englishreader.domain.model

data class OrtLevel(
    val level: Int,
    val grammarDescription: String,
    val targetVocabSize: Int
)

val ORT_LEVELS = mapOf(
    5 to OrtLevel(5, "Simple sentences, common verb tenses, basic adjectives", 500),
    6 to OrtLevel(6, "Compound sentences, past tense, comparative adjectives", 750),
    7 to OrtLevel(7, "Complex dialogue, simple clauses, varied sentence length", 1000),
    8 to OrtLevel(8, "Subordinate clauses, passive voice, richer vocabulary", 1300),
    9 to OrtLevel(9, "Chapter-book complexity, figurative language, inference required", 1600)
)
