package com.example.englishreader.domain.story

import com.example.englishreader.domain.model.ORT_LEVELS
import com.example.englishreader.domain.vocabulary.VocabularyTracker
import com.example.englishreader.inference.LlamaInference

class StoryGenerator(
    private val llama: LlamaInference,
    private val vocabTracker: VocabularyTracker,
    private val parser: StoryParser = StoryParser()
) {
    private val themes = listOf(
        "a child finds a lost puppy in the park",
        "two friends build a treehouse together",
        "a family goes camping and sees wild animals",
        "a boy helps his grandmother bake a cake",
        "children discover a secret path in the woods",
        "a girl learns to ride a bicycle",
        "a class pet escapes from the classroom",
        "siblings have a race to the big oak tree",
        "a child makes friends with the new kid at school",
        "a rainy day turns into an indoor adventure",
        "a trip to the farm to see baby animals",
        "children put on a play for their parents",
        "a boy finds a strange egg in the garden",
        "a girl writes a letter to her pen pal",
        "the family cat gets stuck up a tree",
        "children go on a nature walk and collect leaves",
        "a birthday party with a surprise guest",
        "a child learns to swim at the beach",
        "two friends share their lunch at school",
        "a snowy day and building a snowman"
    )

    private val storyStructures = listOf(
        "Start with a problem, show the character trying to solve it, end with success.",
        "Begin with a normal day, introduce something unexpected, show how the character reacts.",
        "Two characters disagree, they talk about it, they find a way to work together.",
        "A character wants something, faces an obstacle, finds a creative solution.",
        "Start with a question, take the reader on a journey to find the answer."
    )

    suspend fun generate(currentLevel: Int): ParsedStory {
        vocabTracker.refreshCache()
        val masteredWords = vocabTracker.getMasteredList()
        val levelInfo = ORT_LEVELS[currentLevel] ?: ORT_LEVELS[5]!!
        val theme = themes.random()
        val structure = storyStructures.random()

        val vocabList = if (masteredWords.size >= 50) {
            masteredWords.shuffled().take(200)
        } else {
            ORT5_BASE_VOCAB.shuffled().take(200)
        }

        val maxNewWords = (vocabList.size * 0.1).toInt().coerceAtLeast(5).coerceAtMost(15)
        val prompt = buildPrompt(currentLevel, levelInfo.grammarDescription, vocabList, maxNewWords, theme, structure)

        val output = llama.generate(prompt, maxTokens = 800)
        android.util.Log.i("StoryGen", "RAW_OUTPUT:\n$output")
        val parsed = parser.parse(output)
        val filteredWords = parsed.newWords.filter { it !in ORT5_BASE_VOCAB && it.length > 2 }
        return parsed.copy(newWords = filteredWords)
    }

    private fun buildPrompt(
        level: Int,
        grammarDesc: String,
        vocabList: List<String>,
        maxNewWords: Int,
        theme: String,
        structure: String
    ): String {
        val vocabSample = vocabList.joinToString(", ")
        val userContent = """Write a short story for a child learning English at ORT Level $level.

Topic: $theme
Structure: $structure

Language rules:
- Use only: $grammarDesc
- Keep sentences short (6-10 words each)
- Story length: 150-200 words total
- Use mostly these words: $vocabSample
- You may introduce up to $maxNewWords new words not in the list above
- Include natural dialogue with "said" tags

Output format:
TITLE: [a fun title]
STORY: [the story]
NEW_WORDS: [comma-separated new words you used]
IMAGE_PROMPTS: [one short scene description for a cover image]"""

        return "<|im_start|>system\nYou are a children's story writer for early readers. Write natural, engaging stories with simple vocabulary. /no_think<|im_end|>\n<|im_start|>user\n$userContent<|im_end|>\n<|im_start|>assistant\n<think>\n</think>\n"
    }

    companion object {
        val ORT5_BASE_VOCAB = listOf(
            "a", "an", "the", "is", "are", "was", "were", "am",
            "I", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "my", "your", "his", "her", "its", "our", "their",
            "this", "that", "these", "those",
            "and", "but", "or", "so", "because", "if", "when", "then",
            "in", "on", "at", "to", "from", "with", "for", "of", "up", "down", "out", "off",
            "go", "went", "come", "came", "get", "got", "put", "take", "took",
            "see", "saw", "look", "looked", "find", "found",
            "say", "said", "tell", "told", "ask", "asked",
            "make", "made", "do", "did", "have", "had", "can", "could", "will", "would",
            "want", "wanted", "like", "liked", "love", "need",
            "run", "ran", "walk", "walked", "jump", "jumped", "play", "played",
            "eat", "ate", "drink", "give", "gave", "help", "helped",
            "think", "thought", "know", "knew", "feel", "felt",
            "open", "opened", "close", "closed", "stop", "stopped", "start", "started",
            "big", "little", "small", "long", "new", "old", "good", "bad",
            "happy", "sad", "funny", "nice", "pretty", "fast", "slow",
            "red", "blue", "green", "yellow", "white", "black",
            "hot", "cold", "wet", "dry", "hard", "soft",
            "house", "home", "room", "door", "window", "garden", "tree", "park",
            "school", "class", "book", "pen", "bag", "desk",
            "mum", "dad", "boy", "girl", "man", "woman", "child", "children", "friend",
            "dog", "cat", "bird", "fish", "rabbit",
            "car", "bus", "bike", "boat",
            "day", "night", "morning", "time", "today",
            "food", "water", "milk", "cake", "bread",
            "hand", "head", "eye", "face", "foot", "feet",
            "name", "thing", "place", "way", "end",
            "yes", "no", "not", "very", "too", "just", "now", "here", "there",
            "all", "some", "many", "much", "more", "other", "every",
            "one", "two", "three", "four", "five", "first", "last", "next",
            "back", "again", "away", "still", "also", "only", "about",
            "what", "where", "who", "how", "why", "which",
            "well", "right", "oh", "please", "thank", "sorry",
            "let", "try", "tried", "call", "called", "turn", "turned",
            "sit", "sat", "stand", "stood", "wait", "waited",
            "read", "write", "draw", "sing", "sang", "laugh", "laughed", "cry", "cried",
            "sleep", "slept", "wake", "woke", "fall", "fell",
            "bring", "brought", "keep", "kept", "hold", "held",
            "door", "floor", "wall", "bed", "table", "chair",
            "sun", "rain", "wind", "snow", "sky", "cloud",
            "river", "hill", "road", "field", "farm",
            "shop", "town", "village", "bridge",
            "hat", "coat", "shoe", "dress",
            "ball", "game", "toy", "box", "picture",
            "morning", "afternoon", "evening", "week",
            "spring", "summer", "winter",
            "bear", "horse", "cow", "pig", "duck", "frog",
            "flower", "grass", "leaf", "stick", "stone",
            "fire", "light", "dark", "noise", "sound",
            "happy", "angry", "scared", "tired", "hungry",
            "inside", "outside", "behind", "under", "over", "between", "near", "far",
            "before", "after", "soon", "always", "never", "sometimes",
            "together", "alone", "ready", "sure", "enough"
        )
    }
}
