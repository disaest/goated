
package com.example.goated.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.goated.R
import com.example.goated.components.BottomBar
import com.example.goated.screens.*
import com.example.goated.ui.theme.AccentBlue
import com.example.goated.ui.theme.DarkBackground

object Routes {
    const val MAIN = "main"
    const val SEARCH = "search"
    const val GENRES = "genres"
    const val PROFILE = "profile"
    const val GENRE_GAMES = "genre_games/{genreId}"
    const val GAME_CARD = "game_card/{gameId}"
    const val COMPLETED_GAMES = "completed_games"

    fun genreGames(genreId: Int) = "genre_games/$genreId"
    fun gameCard(gameId: String) = "game_card/$gameId"
}

enum class Tab(
    val route: String,
    @param:DrawableRes val icon: Int,
    val label: String
) {
    Main(Routes.MAIN, R.drawable.ic_nav_home, "Главная"),
    Search(Routes.SEARCH, R.drawable.ic_nav_search, "Поиск"),
    Genres(Routes.GENRES, R.drawable.ic_nav_genres, "Жанры"),
    Profile(Routes.PROFILE, R.drawable.ic_nav_profile, "Профиль");

    companion object {
        fun forRoute(route: String?): Tab? = when (route) {
            Routes.MAIN -> Main
            Routes.SEARCH -> Search
            Routes.GENRES, Routes.GENRE_GAMES -> Genres
            Routes.PROFILE, Routes.COMPLETED_GAMES -> Profile
            else -> null
        }
    }
}

val LocalSnackbar = staticCompositionLocalOf { SnackbarHostState() }

fun NavHostController.navigateToTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val snackbar = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    CompositionLocalProvider(LocalSnackbar provides snackbar) {
        Scaffold(
            containerColor = DarkBackground,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = {
                SnackbarHost(snackbar) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = DarkBackground,
                        contentColor = AccentBlue,
                        actionColor = AccentBlue,
                        dismissActionContentColor = AccentBlue
                    )
                }
            },
            bottomBar = {
                BottomBar(
                    selectedTab = Tab.forRoute(currentRoute),
                    onSelect = navController::navigateToTab
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.MAIN,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
            ) {
                composable(Routes.MAIN) {
                    MainScreen(navController)
                }
                composable(Routes.SEARCH) {
                    SearchScreen(navController)
                }
                composable(Routes.GENRES) {
                    GenresScreen(navController)
                }
                composable(
                    Routes.GENRE_GAMES,
                    arguments = listOf(
                        navArgument("genreId") {
                            type = NavType.IntType
                        }
                    )
                ) { entry ->
                    CoopGenreScreen(
                        navController,
                        entry.arguments?.getInt("genreId") ?: 0
                    )
                }
                composable(Routes.GAME_CARD) { entry ->
                    GameCardScreen(
                        navController,
                        entry.arguments?.getString("gameId").orEmpty()
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileRoute(navController)
                }
                composable(Routes.COMPLETED_GAMES) {
                    GamesScreen(navController)
                }
            }
        }
    }
}
