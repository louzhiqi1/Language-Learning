package com.example.englishreader.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.englishreader.ui.checkin.CheckInScreen
import com.example.englishreader.ui.home.HomeScreen
import com.example.englishreader.ui.library.LibraryScreen
import com.example.englishreader.ui.quiz.QuizScreen
import com.example.englishreader.ui.review.ReviewScreen
import com.example.englishreader.ui.settings.SettingsScreen
import com.example.englishreader.ui.story.StoryScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Story : Screen("story/{storyId}") {
        fun createRoute(storyId: Long) = "story/$storyId"
    }
    data object Quiz : Screen("quiz/{storyId}") {
        fun createRoute(storyId: Long) = "quiz/$storyId"
    }
    data object Library : Screen("library")
    data object CheckIn : Screen("checkin")
    data object Settings : Screen("settings")
    data object Review : Screen("review")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Story.route) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId")?.toLongOrNull() ?: 0L
            StoryScreen(storyId, navController)
        }
        composable(Screen.Quiz.route) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId")?.toLongOrNull() ?: 0L
            QuizScreen(storyId, navController)
        }
        composable(Screen.Library.route) { LibraryScreen(navController) }
        composable(Screen.CheckIn.route) { CheckInScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Review.route) { ReviewScreen(navController) }
    }
}
