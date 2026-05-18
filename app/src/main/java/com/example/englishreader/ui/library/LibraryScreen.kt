package com.example.englishreader.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.englishreader.navigation.Screen

@Composable
fun LibraryScreen(navController: NavController, viewModel: LibraryViewModel = viewModel()) {
    val stories by viewModel.stories.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("My Stories", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (stories.isEmpty()) {
            Text(
                "No stories yet. Go generate your first one!",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(stories) { story ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(Screen.Story.createRoute(story.id)) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(story.title, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Level ${story.level} | ${if (story.isRead) "Read" else "New"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
