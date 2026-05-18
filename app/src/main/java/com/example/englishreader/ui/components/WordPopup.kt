package com.example.englishreader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun WordPopup(
    word: String,
    meaning: String?,
    exampleSentence: String?,
    isInVocab: Boolean,
    onDismiss: () -> Unit,
    onAddToVocabulary: () -> Unit,
    onMarkKnown: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(word, style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(12.dp))

                if (meaning != null) {
                    Text(meaning, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                } else {
                    Text(
                        "New word",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!exampleSentence.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        exampleSentence,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isInVocab) {
                        Button(onClick = onAddToVocabulary) {
                            Text("Add to Vocabulary")
                        }
                    } else {
                        OutlinedButton(onClick = onMarkKnown) {
                            Text("I know it")
                        }
                    }
                }
            }
        }
    }
}
