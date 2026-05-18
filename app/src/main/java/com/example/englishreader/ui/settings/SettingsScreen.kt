package com.example.englishreader.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var ttsSpeed by remember { mutableFloatStateOf(1.0f) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Reading Speed", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = ttsSpeed,
            onValueChange = { ttsSpeed = it },
            valueRange = 0.5f..1.5f,
            steps = 4
        )
        Text("${String.format("%.1f", ttsSpeed)}x", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Parent Zone", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = { }) {
            Text("Set Parent PIN")
        }
    }
}
