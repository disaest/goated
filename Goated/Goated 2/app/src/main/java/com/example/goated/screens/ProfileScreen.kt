package com.example.goated.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.goated.components.*
import com.example.goated.data.AuthRepository
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.data.Profile
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController) {
    val isLoggedIn = AuthRepository.isLoggedIn

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(Screen.Registration.route) {
                popUpTo(Screen.Main.route)
            }
        }
    }

    if (!isLoggedIn) {
        Box(Modifier.fillMaxSize())
        return
    }

    var profile by remember { mutableStateOf<Profile?>(null) }
    var favorites by remember { mutableStateOf<List<Game>>(emptyList()) }
    var recent by remember { mutableStateOf<List<Game>>(emptyList()) }
    var userRatings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        profile = GameRepository.getProfile()
        favorites = GameRepository.getFavorites()
        recent = GameRepository.getRecentActivity()
        val reviews = GameRepository.getUserReviews()
        userRatings = reviews.mapNotNull { r -> r.rating?.let { r.gameId to it } }.toMap()
        loading = false
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        if (loading) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Загрузка...", color = GrayText)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ProfileTopBar(profile = profile)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding)
            ) {
                Spacer(Modifier.height(Dimens.SectionSpacing))

                Text("FAVORITES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(Dimens.ItemSpacing))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                ) {
                    favorites.take(4).forEach { g ->
                        GameCard(
                            title = g.title,
                            coverUrl = g.coverUrl,
                            modifier = Modifier.weight(1f),
                            showTitle = false,
                            showStars = false,
                            onClick = { navController.navigate(Screen.GameCard.createRoute(g.id)) }
                        )
                    }
                    repeat(4 - favorites.take(4).size) {
                        Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(Dimens.SectionSpacing))

                Text("RECENT ACTIVITY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(Dimens.ItemSpacing))
                Row(
                    Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                ) {
                    recent.take(4).forEach { g ->
                        GameCard(
                            title = g.title,
                            coverUrl = g.coverUrl,
                            modifier = Modifier.weight(1f),
                            showTitle = false,
                            showStars = true,
                            userRating = userRatings[g.id],
                            onClick = { navController.navigate(Screen.GameCard.createRoute(g.id)) }
                        )
                    }
                    repeat(4 - recent.take(4).size) {
                        Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(Dimens.SectionSpacing))

                Text(
                    "GAMES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.clickable { navController.navigate(Screen.Games.route) }
                )

                Spacer(Modifier.height(Dimens.SectionSpacing))

                Text(
                    "ВЫЙТИ",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Light,
                    color = AccentBlue,
                    modifier = Modifier.clickable {
                        scope.launch {
                            AuthRepository.signOut()
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}