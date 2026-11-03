package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.goated.components.*
import com.example.goated.data.Collection
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.data.Genre
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.*

@Composable
fun MainScreen(navController: NavHostController) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            GradientTopBar()
            MainTabs(selected = tab, onSelect = { tab = it })

            when (tab) {
                0 -> GamesTab(navController)
                1 -> GenresTab(navController)
            }
        }
    }
}

@Composable
private fun GamesTab(navController: NavHostController) {
    var collections by remember { mutableStateOf<List<Collection>>(emptyList()) }
    var gamesByCollection by remember { mutableStateOf<Map<String, List<Game>>>(emptyMap()) }
    var userRatings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val cols = GameRepository.getCollections()
        val map = mutableMapOf<String, List<Game>>()
        cols.forEach { c ->
            map[c.id] = GameRepository.getGamesByCollection(c.id)
        }
        val reviews = GameRepository.getUserReviews()
        val ratings = reviews.mapNotNull { r -> r.rating?.let { r.gameId to it } }.toMap()
        collections = cols
        gamesByCollection = map
        userRatings = ratings
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Загрузка...", color = GrayText)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
        contentPadding = PaddingValues(vertical = Dimens.SectionSpacing)
    ) {
        collections.forEach { collection ->
            val games = gamesByCollection[collection.id].orEmpty()
            if (games.isEmpty()) return@forEach

            item {
                CategoryTitle(collection.title)
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                    games.chunked(4).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                        ) {
                            row.forEach { game ->
                                GameCard(
                                    title = game.title,
                                    coverUrl = game.coverUrl,
                                    modifier = Modifier.weight(1f),
                                    showTitle = true,
                                    showStars = true,
                                    userRating = userRatings[game.id],
                                    onClick = {
                                        navController.navigate(Screen.GameCard.createRoute(game.id))
                                    }
                                )
                            }
                            repeat(4 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenresTab(navController: NavHostController) {
    var genres by remember { mutableStateOf<List<Genre>>(emptyList()) }
    var gamesByGenre by remember { mutableStateOf<Map<Int, List<Game>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val gs = GameRepository.getGenres()
        val map = mutableMapOf<Int, List<Game>>()
        gs.forEach { g ->
            map[g.id] = GameRepository.getGamesByGenre(g.id, limit = 3)
        }
        genres = gs
        gamesByGenre = map
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Загрузка...", color = GrayText)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
        contentPadding = PaddingValues(vertical = Dimens.SectionSpacing)
    ) {
        genres.forEach { genre ->
            val games = gamesByGenre[genre.id].orEmpty()
            if (games.isEmpty()) return@forEach

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                ) {
                    GenreCard(
                        name = genre.name,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate(Screen.GenreGames.createRoute(genre.name))
                        }
                    )
                    games.forEach { game ->
                        GameCard(
                            title = game.title,
                            coverUrl = game.coverUrl,
                            modifier = Modifier.weight(1f),
                            showTitle = false,
                            showStars = false,
                            onClick = {
                                navController.navigate(Screen.GameCard.createRoute(game.id))
                            }
                        )
                    }
                    repeat(3 - games.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreCard(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(Dimens.GameCardRadius))
            .background(PrimaryBlue)
            .border(
                width = Dimens.GameCardBorder,
                color = CardBorder,
                shape = RoundedCornerShape(Dimens.GameCardRadius)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(6.dp)
        )
    }
}