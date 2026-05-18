package com.example.englishreader.domain.story

import com.example.englishreader.domain.model.Language
import com.example.englishreader.domain.model.ORT_LEVELS
import com.example.englishreader.domain.vocabulary.VocabularyTracker
import com.example.englishreader.inference.LlamaInference

class StoryGenerator(
    private val llama: LlamaInference,
    private val vocabTracker: VocabularyTracker,
    private val parser: StoryParser = StoryParser()
) {
    private val englishThemes = listOf(
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

    private val japaneseThemes = listOf(
        "子供が公園で迷子の子犬を見つける",
        "友達と一緒にツリーハウスを作る",
        "家族でキャンプに行って動物を見る",
        "おばあちゃんとケーキを焼く",
        "森の中で秘密の道を見つける",
        "自転車に乗る練習をする",
        "教室のペットが逃げ出す",
        "兄弟で大きな木まで競争する",
        "転校生と友達になる",
        "雨の日の室内冒険",
        "農場で赤ちゃん動物を見に行く",
        "お父さんとお母さんのために劇をする",
        "庭で不思議な卵を見つける",
        "ペンパルに手紙を書く",
        "猫が木に登って降りられなくなる",
        "自然散歩で葉っぱを集める",
        "サプライズゲストが来る誕生日パーティー",
        "海で泳ぎを覚える",
        "学校でお弁当を分け合う",
        "雪の日に雪だるまを作る"
    )

    private val storyStructures = listOf(
        "Start with a problem, show the character trying to solve it, end with success.",
        "Begin with a normal day, introduce something unexpected, show how the character reacts.",
        "Two characters disagree, they talk about it, they find a way to work together.",
        "A character wants something, faces an obstacle, finds a creative solution.",
        "Start with a question, take the reader on a journey to find the answer."
    )

    suspend fun generate(currentLevel: Int, language: Language = Language.ENGLISH): ParsedStory {
        vocabTracker.refreshCache()
        val masteredWords = vocabTracker.getMasteredList()
        val levelInfo = ORT_LEVELS[currentLevel] ?: ORT_LEVELS[5]!!
        val structure = storyStructures.random()

        return when (language) {
            Language.ENGLISH -> generateEnglish(currentLevel, levelInfo.grammarDescription, masteredWords, structure)
            Language.JAPANESE -> generateJapanese(currentLevel, masteredWords, structure)
        }
    }

    private suspend fun generateEnglish(
        level: Int, grammarDesc: String, masteredWords: List<String>, structure: String
    ): ParsedStory {
        val theme = englishThemes.random()
        val vocabList = if (masteredWords.size >= 50) {
            masteredWords.shuffled().take(200)
        } else {
            ORT5_BASE_VOCAB.shuffled().take(200)
        }
        val maxNewWords = (vocabList.size * 0.1).toInt().coerceAtLeast(5).coerceAtMost(15)
        val prompt = buildEnglishPrompt(level, grammarDesc, vocabList, maxNewWords, theme, structure)

        val output = llama.generate(prompt, maxTokens = 800)
        android.util.Log.i("StoryGen", "RAW_OUTPUT:\n$output")
        val parsed = parser.parse(output)
        val filteredWords = parsed.newWords.filter { it !in ORT5_BASE_VOCAB && it.length > 2 }
        return parsed.copy(newWords = filteredWords)
    }

    private suspend fun generateJapanese(
        level: Int, masteredWords: List<String>, structure: String
    ): ParsedStory {
        val theme = japaneseThemes.random()
        val vocabList = if (masteredWords.size >= 20) {
            masteredWords.shuffled().take(100)
        } else {
            JLPT5_BASE_VOCAB.shuffled().take(100)
        }
        val maxNewWords = (vocabList.size * 0.1).toInt().coerceAtLeast(3).coerceAtMost(10)
        val prompt = buildJapanesePrompt(level, vocabList, maxNewWords, theme, structure)

        val output = llama.generate(prompt, maxTokens = 800)
        android.util.Log.i("StoryGen", "RAW_OUTPUT_JA:\n$output")
        val parsed = parser.parse(output)
        return parsed
    }

    private fun buildEnglishPrompt(
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

    private fun buildJapanesePrompt(
        level: Int,
        vocabList: List<String>,
        maxNewWords: Int,
        theme: String,
        structure: String
    ): String {
        val grammarDesc = when {
            level <= 5 -> "です/ます form, simple て-form, basic particles (は、が、を、に、で、と)"
            level <= 6 -> "て-form connections, たい form, adjective conjugation, から/ので"
            level <= 7 -> "plain form, と思う, relative clauses, ている for ongoing state"
            level <= 8 -> "passive, causative, conditional (たら/ば), てもらう/てあげる"
            else -> "complex sentences, honorifics, embedded clauses, various conjunctions"
        }
        val vocabSample = vocabList.take(50).joinToString("、")
        val jlptLevel = 10 - level
        val userContent = """Write a short story in Japanese for a child learning Japanese at JLPT N$jlptLevel level.

Topic: $theme
Structure: $structure

Language rules:
- Grammar: $grammarDesc
- Keep sentences short (10-15 characters each)
- Story length: 100-150 characters total
- Use mostly these words: $vocabSample
- You may introduce up to $maxNewWords new words
- Include natural dialogue
- Write the story entirely in Japanese (hiragana, katakana, simple kanji)

Output format (use these exact English labels):
TITLE: [a fun title in Japanese]
STORY: [the story in Japanese]
NEW_WORDS: [comma-separated new Japanese words you used]
IMAGE_PROMPTS: [one short scene description in English for a cover image]"""

        return "<|im_start|>system\nYou are a children's story writer. Write stories in Japanese with simple vocabulary. /no_think<|im_end|>\n<|im_start|>user\n$userContent<|im_end|>\n<|im_start|>assistant\n<think>\n</think>\n"
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

        val JLPT5_BASE_VOCAB = listOf(
            "わたし", "あなた", "かれ", "かのじょ", "これ", "それ", "あれ",
            "ここ", "そこ", "あそこ", "いつ", "どこ", "だれ", "なに", "なぜ",
            "はい", "いいえ", "おはよう", "こんにちは", "さようなら", "ありがとう", "すみません",
            "いく", "くる", "かえる", "たべる", "のむ", "みる", "きく", "よむ", "かく",
            "はなす", "あそぶ", "ねる", "おきる", "あるく", "はしる", "およぐ",
            "つくる", "かう", "うる", "おしえる", "ならう", "まつ", "わかる",
            "おおきい", "ちいさい", "たかい", "やすい", "あたらしい", "ふるい",
            "いい", "わるい", "おいしい", "たのしい", "うれしい", "かなしい",
            "あつい", "さむい", "ひろい", "せまい", "ながい", "みじかい",
            "がっこう", "いえ", "へや", "まち", "みせ", "えき", "びょういん",
            "こうえん", "やま", "うみ", "かわ", "はな", "き", "そら",
            "ひと", "こども", "おとこ", "おんな", "ともだち", "せんせい", "かぞく",
            "おとうさん", "おかあさん", "おにいさん", "おねえさん",
            "いぬ", "ねこ", "とり", "さかな",
            "くるま", "でんしゃ", "じてんしゃ", "バス",
            "あさ", "ひる", "よる", "きょう", "あした", "きのう",
            "ごはん", "みず", "おちゃ", "パン", "くだもの",
            "て", "あし", "め", "みみ", "くち", "あたま",
            "ほん", "えんぴつ", "かばん", "つくえ", "いす",
            "いち", "に", "さん", "し", "ご", "ろく", "しち", "はち", "きゅう", "じゅう",
            "とても", "すこし", "もう", "まだ", "いつも", "ときどき",
            "うえ", "した", "なか", "そと", "まえ", "うしろ", "となり"
        )
    }
}

