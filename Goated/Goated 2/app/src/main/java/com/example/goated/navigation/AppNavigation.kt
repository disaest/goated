package com.example.goated.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.goated.screens.*

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Genres : Screen("genres")
    object GenreGames : Screen("genre_games/{genreName}") {
        fun createRoute(genreName: String) = "genre_games/$genreName"
    }
    object GameCard : Screen("game_card/{gameId}") {
        fun createRoute(gameId: String) = "game_card/$gameId"
    }
    object Profile : Screen("profile")
    object Games : Screen("games")
    object Registration : Screen("registration")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) { MainScreen(navController) }
        composable(Screen.Genres.route) { GenresScreen(navController) }
        composable(Screen.GenreGames.route) { backStack ->
            val genre = backStack.arguments?.getString("genreName") ?: "Coop"
            CoopGenreScreen(navController, genre)
        }
        composable(Screen.GameCard.route) { backStack ->
            val gameId = backStack.arguments?.getString("gameId") ?: ""
            GameCardScreen(navController, gameId)
        }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Games.route) { GamesScreen(navController) }
        composable(Screen.Registration.route) { RegistrationScreen(navController) }
    }
}