package com.example.englishreader.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.englishreader.navigation.Screen

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToStory.collect { storyId ->
            navController.navigate(Screen.Story.createRoute(storyId))
        }
    }

    val backStackEntry = navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry.value) {
        if (backStackEntry.value?.destination?.route == Screen.Home.route) {
            viewModel.onReturnFromReading()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (uiState.currentLanguage.code == "ja") "日本語リーダー" else "English Reader",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Level ${uiState.currentLevel} | ${uiState.masteredWordCount} words learned",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.isGenerating && uiState.unreadCount == 0) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Creating your story...", style = MaterialTheme.typography.bodyLarge)
        } else {
            Button(
                onClick = { viewModel.readNextStory() },
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text("Read Story", style = MaterialTheme.typography.labelLarge)
            }

            if (uiState.isGenerating) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    "Preparing more stories...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.reviewDueCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Review.route) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Review (${uiState.reviewDueCount} words due)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "${uiState.unreadCount} stories ready",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
