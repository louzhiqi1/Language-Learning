package com.example.englishreader.domain.story

data class ParsedStory(
    val title: String,
    val content: String,
    val newWords: List<String>,
    val imagePrompts: List<String>
)

class StoryParser {
    fun parse(llmOutput: String): ParsedStory {
        val cleaned = llmOutput.replace(Regex("<think>[\\s\\S]*?</think>"), "").trim()
        val lines = cleaned.lines()
        var title = ""
        var story = ""
        var newWords = listOf<String>()
        val imagePrompts = mutableListOf<String>()
        var inStory = false
        var storyFound = false

        for (line in lines) {
            val trimmed = line.trim()
            when {
                matchesLabel(trimmed, "TITLE") && title.isEmpty() -> {
                    inStory = true
                    storyFound = true
                    title = extractValue(trimmed, "TITLE")
                }
                matchesLabel(trimmed, "STORY") -> {
                    val value = extractValue(trimmed, "STORY")
                    if (value == "[the story]" || value == "[full story text]") {
                        inStory = true
                        storyFound = true
                    } else if (!storyFound || story.isEmpty()) {
                        inStory = true
                        storyFound = true
                        if (value.isNotEmpty()) story = value
                    } else {
                        inStory = false
                    }
                }
                matchesLabel(trimmed, "NEW_WORDS") && newWords.isEmpty() -> {
                    inStory = false
                    val raw = extractValue(trimmed, "NEW_WORDS")
                    newWords = if (raw.equals("none", ignoreCase = true)) {
                        emptyList()
                    } else {
                        raw.split(",")
                            .map { it.trim().lowercase().removePrefix("- ") }
                            .filter { it.isNotEmpty() && it != "none" }
                            .distinct()
                    }
                }
                (matchesLabel(trimmed, "IMAGE_PROMPTS") || matchesLabel(trimmed, "IMAGE_PROMPT")) && imagePrompts.isEmpty() -> {
                    inStory = false
                    val label = if (trimmed.replace("*","").contains("IMAGE_PROMPTS", ignoreCase = true)) "IMAGE_PROMPTS" else "IMAGE_PROMPT"
                    val json = extractValue(trimmed, label)
                    if (json.isNotEmpty()) {
                        imagePrompts.addAll(parseJsonArray(json))
                        if (imagePrompts.isEmpty()) imagePrompts.add(json)
                    }
                }
                trimmed.startsWith("[\"") && imagePrompts.isEmpty() -> {
                    imagePrompts.addAll(parseJsonArray(trimmed))
                }
                inStory && trimmed.isNotEmpty() && !matchesLabel(trimmed, "NEW_WORDS") && !matchesLabel(trimmed, "IMAGE") -> {
                    story = if (story.isEmpty()) trimmed else "$story $trimmed"
                }
            }
        }
        return ParsedStory(cleanMarkdown(title), story.trim(), newWords, imagePrompts)
    }

    private fun matchesLabel(line: String, label: String): Boolean {
        val stripped = line.replace("*", "").replace("#", "").trim()
        return stripped.startsWith("$label:", ignoreCase = true) ||
               stripped.startsWith("$label :", ignoreCase = true)
    }

    private fun extractValue(line: String, label: String): String {
        val stripped = line.replace("*", "").replace("#", "").trim()
        val idx = stripped.indexOf(":", ignoreCase = true)
        return if (idx >= 0) stripped.substring(idx + 1).trim() else ""
    }

    private fun cleanMarkdown(text: String): String {
        return text.replace("*", "").replace("#", "").trim()
    }

    fun extractWords(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z'\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() && it.length > 1 }
            .distinct()
    }

    fun splitIntoGroups(text: String, sentencesPerGroup: Int): List<String> {
        val sentences = text.split(Regex("(?<=[.!?。！？])\\s*")).filter { it.isNotBlank() }
        return sentences.chunked(sentencesPerGroup).map { it.joinToString(" ") }
    }

    private fun parseJsonArray(json: String): List<String> {
        val content = json.trim().removePrefix("[").removeSuffix("]")
        if (content.isBlank()) return emptyList()
        return content.split(Regex("\",\\s*\""))
            .map { it.trim().removePrefix("\"").removeSuffix("\"") }
            .filter { it.isNotEmpty() }
    }
}
