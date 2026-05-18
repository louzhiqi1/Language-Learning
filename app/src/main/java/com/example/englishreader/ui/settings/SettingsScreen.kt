package com.example.englishreader.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.englishreader.domain.model.Language

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var ttsSpeed by remember { mutableFloatStateOf(prefs.getFloat("tts_speed", 1.0f)) }
    var selectedLanguage by remember {
        mutableStateOf(Language.fromCode(prefs.getString("language", "en") ?: "en"))
    }
    var currentLevel by remember { mutableIntStateOf(prefs.getInt("current_level_${selectedLanguage.code}", 5)) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Language", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Language.entries.forEach { lang ->
                FilterChip(
                    selected = selectedLanguage == lang,
                    onClick = {
                        selectedLanguage = lang
                        prefs.edit().putString("language", lang.code).apply()
                        currentLevel = prefs.getInt("current_level_${lang.code}", 5)
                    },
                    label = { Text("${lang.nativeName} (${lang.displayName})") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Current Level: $currentLevel", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = currentLevel.toFloat(),
            onValueChange = {
                currentLevel = it.toInt()
                prefs.edit().putInt("current_level_${selectedLanguage.code}", currentLevel).apply()
            },
            valueRange = 1f..9f,
            steps = 7
        )
        Text(
            when {
                currentLevel <= 5 -> "Beginner"
                currentLevel <= 7 -> "Intermediate"
                else -> "Advanced"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Reading Speed", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = ttsSpeed,
            onValueChange = {
                ttsSpeed = it
                prefs.edit().putFloat("tts_speed", ttsSpeed).apply()
            },
            valueRange = 0.5f..1.5f,
            steps = 4
        )
        Text("${String.format("%.1f", ttsSpeed)}x", style = MaterialTheme.typography.bodyMedium)
    }
}
