package com.example.englishreader.ui.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.englishreader.domain.vocabulary.ReviewQuality

@Composable
fun ReviewScreen(navController: NavController, viewModel: ReviewViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isComplete) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Review Complete!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${uiState.reviewedCount} words reviewed",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Home")
            }
            Spacer(modifier = Modifier.weight(1f))
            return
        }

        val word = uiState.words.getOrNull(uiState.currentIndex) ?: return

        Text(
            "${uiState.currentIndex + 1} / ${uiState.words.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (uiState.currentIndex.toFloat()) / uiState.words.size },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    word.word,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                AnimatedVisibility(visible = uiState.isRevealed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            word.meaning.ifBlank { "No definition yet" },
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        if (word.exampleSentence.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                word.exampleSentence,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!uiState.isRevealed) {
            Button(
                onClick = { viewModel.reveal() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Show Answer")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.rate(ReviewQuality.AGAIN) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Again")
                }
                Button(
                    onClick = { viewModel.rate(ReviewQuality.GOOD) },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("Good")
                }
                Button(
                    onClick = { viewModel.rate(ReviewQuality.EASY) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Easy")
                }
            }
        }
    }
}
