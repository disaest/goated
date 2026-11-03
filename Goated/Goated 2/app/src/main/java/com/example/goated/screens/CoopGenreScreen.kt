package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.goated.components.BottomBar
import com.example.goated.components.GameCard
import com.example.goated.components.GradientTopBar
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.*

@Composable
fun CoopGenreScreen(navController: NavHostController, genreName: String = "Coop") {
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(genreName) {
        val genre = GameRepository.getGenreByName(genreName)
        games = if (genre != null) GameRepository.getAllGamesByGenre(genre.id) else emptyList()
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
            GradientTopBar(title = genreName)
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Загрузка...", color = GrayText)
                }
            } else if (games.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("В этом жанре пока нет игр", color = GrayText)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                    verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                    contentPadding = PaddingValues(vertical = Dimens.SectionSpacing)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.68f)
                                .clip(RoundedCornerShape(Dimens.GameCardRadius))
                                .background(CardBackground)
                                .border(
                                    width = Dimens.GameCardBorder,
                                    color = CardBorder,
                                    shape = RoundedCornerShape(Dimens.GameCardRadius)
                                )
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    items(games) { g ->
                        GameCard(
                            title = g.title,
                            coverUrl = g.coverUrl,
                            showTitle = false,
                            showStars = false,
                            onClick = { navController.navigate(Screen.GameCard.createRoute(g.id)) }
                        )
                    }
                }
            }
        }
    }
}