package com.example.englishreader.domain.model

enum class Achievement(val title: String, val description: String, val icon: String) {
    FIRST_STORY("First Story", "Read your first story", "📖"),
    STREAK_7("Week Warrior", "7 day streak", "🔥"),
    STREAK_30("Monthly Master", "30 day streak", "⭐"),
    VOCAB_100("Word Collector", "Learn 100 words", "💯"),
    VOCAB_300("Vocabulary Builder", "Learn 300 words", "📚"),
    VOCAB_500("Word Expert", "Learn 500 words", "🏆"),
    VOCAB_1000("Word Master", "Learn 1000 words", "👑"),
    STORIES_10("Bookworm", "Read 10 stories", "🐛"),
    STORIES_50("Story Explorer", "Read 50 stories", "🗺️"),
    STORIES_100("Story Champion", "Read 100 stories", "🏅"),
    LEVEL_UP_6("Level 6", "Reach ORT Level 6", "⬆️"),
    LEVEL_UP_7("Level 7", "Reach ORT Level 7", "⬆️"),
    LEVEL_UP_8("Level 8", "Reach ORT Level 8", "⬆️")
}
