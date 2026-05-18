package com.example.englishreader.domain.quiz

import com.example.englishreader.domain.model.QuizQuestion
import com.example.englishreader.domain.model.QuizType

class QuizGenerator(private val getMeaning: suspend (String) -> String) {

    suspend fun generate(
        newWords: List<String>,
        storyContent: String,
        maxQuestions: Int = 5
    ): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val words = newWords.take(maxQuestions)

        for (word in words) {
            val meaning = getMeaning(word)
            if (meaning.isBlank()) continue

            when (questions.size % 2) {
                0 -> questions.add(makeEnglishToChinese(word, meaning, newWords))
                1 -> questions.add(makeFillBlank(word, storyContent, newWords))
            }
        }

        return questions.take(maxQuestions)
    }

    private suspend fun makeEnglishToChinese(
        word: String,
        correctMeaning: String,
        allWords: List<String>
    ): QuizQuestion {
        val distractors = allWords.filter { it != word }
            .shuffled().take(2)
            .map { getMeaning(it) }
            .filter { it.isNotBlank() && it != correctMeaning }

        val fillerMeanings = listOf("a young dog", "very small", "to move fast", "to look at", "to speak words")
        val options = (listOf(correctMeaning) + distractors +
            fillerMeanings.filter { it != correctMeaning })
            .distinct().take(3).shuffled()

        return QuizQuestion(
            type = QuizType.ENGLISH_TO_CHINESE,
            question = word,
            options = options,
            correctAnswer = correctMeaning
        )
    }

    private fun makeFillBlank(
        word: String,
        storyContent: String,
        allWords: List<String>
    ): QuizQuestion {
        val sentences = storyContent.split(Regex("(?<=[.!?])\\s+"))
        val sentence = sentences.firstOrNull {
            it.lowercase().contains(word.lowercase())
        } ?: "$word is a new word."

        val blanked = sentence.replace(
            Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE), "___"
        )

        val distractors = allWords.filter { it != word }.shuffled().take(2)
        val options = (listOf(word) + distractors).distinct().take(3).shuffled()

        return QuizQuestion(
            type = QuizType.FILL_BLANK,
            question = blanked,
            options = options,
            correctAnswer = word
        )
    }
}
