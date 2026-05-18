package com.example.englishreader.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.englishreader.domain.model.QuizType
import com.example.englishreader.ui.theme.SuccessGreen

@Composable
fun QuizScreen(
    storyId: Long,
    navController: NavController,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storyId) { viewModel.loadQuiz(storyId) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isFinished) {
            Spacer(modifier = Modifier.height(64.dp))
            Text("Quiz Complete!", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "${uiState.correctCount} / ${uiState.questions.size} correct",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Home")
            }
        } else {
            val question = uiState.questions.getOrNull(uiState.currentIndex)
            if (question != null) {
                LinearProgressIndicator(
                    progress = { (uiState.currentIndex + 1f) / uiState.questions.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = when (question.type) {
                        QuizType.ENGLISH_TO_CHINESE -> "What does this word mean?"
                        QuizType.FILL_BLANK -> "Fill in the blank:"
                        QuizType.IMAGE_WORD -> "Which word matches?"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(question.question, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))

                question.options.forEach { option ->
                    val isSelected = uiState.selectedAnswer == option
                    val isCorrectAnswer = option == question.correctAnswer
                    val containerColor = when {
                        uiState.selectedAnswer == null -> MaterialTheme.colorScheme.surface
                        isCorrectAnswer -> SuccessGreen
                        isSelected -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surface
                    }

                    OutlinedButton(
                        onClick = {
                            if (uiState.selectedAnswer == null) viewModel.submitAnswer(option, storyId)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor)
                    ) {
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (uiState.selectedAnswer != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.nextQuestion() }) {
                        Text("Next")
                    }
                }
            }
        }
    }
}
