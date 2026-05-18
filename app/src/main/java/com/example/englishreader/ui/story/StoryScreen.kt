package com.example.englishreader.ui.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.englishreader.navigation.Screen
import com.example.englishreader.ui.components.WordPopup
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun StoryScreen(
    storyId: Long,
    navController: NavController,
    viewModel: StoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storyId) { viewModel.loadStory(storyId) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        uiState.story?.let { story ->
            Text(
                text = story.title,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(24.dp)
            )

            if (story.coverImagePath.isNotBlank() && File(story.coverImagePath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(story.coverImagePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Story cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(256.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            IconButton(onClick = { viewModel.toggleTts() }) {
                Icon(
                    if (uiState.ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle TTS"
                )
            }
        }

        uiState.sentenceGroups.forEachIndexed { index, group ->
            val words = group.split(Regex("\\s+"))
            FlowRow(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                words.forEach { token ->
                    Text(
                        text = "$token ",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.combinedClickable(
                            onClick = { viewModel.speakSentence(group) },
                            onLongClick = {
                                val cleanWord = token.replace(Regex("[^a-zA-Z']"), "")
                                if (cleanWord.isNotBlank()) {
                                    viewModel.onWordLongPress(cleanWord, group)
                                }
                            }
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                uiState.story?.let { navController.navigate(Screen.Quiz.createRoute(it.id)) }
            },
            modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp)
        ) {
            Text("Done Reading - Take Quiz")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (uiState.selectedWord != null) {
        WordPopup(
            word = uiState.selectedWord!!,
            meaning = uiState.selectedWordMeaning,
            exampleSentence = uiState.selectedWordExample,
            isInVocab = uiState.selectedWordInVocab,
            onDismiss = { viewModel.dismissWordPopup() },
            onAddToVocabulary = { viewModel.addToVocabulary(uiState.selectedWord!!) },
            onMarkKnown = { viewModel.markWordAsKnown(uiState.selectedWord!!) }
        )
    }
}
