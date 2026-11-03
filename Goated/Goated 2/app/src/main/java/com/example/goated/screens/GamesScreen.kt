package com.example.goated.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.goated.components.BottomBar
import com.example.goated.components.GameCard
import com.example.goated.components.ProfileTopBar
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.data.Profile
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.*

@Composable
fun GamesScreen(navController: NavHostController) {
    var profile by remember { mutableStateOf<Profile?>(null) }
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var userRatings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        profile = GameRepository.getProfile()
        games = GameRepository.getPlayedGames()
        val reviews = GameRepository.getUserReviews()
        userRatings = reviews.mapNotNull { r -> r.rating?.let { r.gameId to it } }.toMap()
        loading = false
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ProfileTopBar(profile = profile)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 14.dp)
                    .clickable { navController.popBackStack() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "GAMES",
                    color = White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Загрузка...", color = GrayText)
                }
            } else if (games.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Пока нет пройденных игр", color = GrayText)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                    verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                    contentPadding = PaddingValues(bottom = Dimens.SectionSpacing)
                ) {
                    items(games) { g ->
                        GameCard(
                            title = g.title,
                            coverUrl = g.coverUrl,
                            showTitle = false,
                            showStars = true,
                            userRating = userRatings[g.id],
                            onClick = { navController.navigate(Screen.GameCard.createRoute(g.id)) }
                        )
                    }
                }
            }
        }
    }
}