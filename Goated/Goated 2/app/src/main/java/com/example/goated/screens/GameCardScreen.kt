package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.goated.components.BottomBar
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GameCardScreen(navController: NavHostController, gameId: String) {
    var game by remember { mutableStateOf<Game?>(null) }
    var userRating by remember { mutableStateOf<Int?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            game = GameRepository.getGameById(gameId)
            userRating = GameRepository.getUserRatingForGame(gameId)
            isFavorite = GameRepository.isFavorite(gameId)
            loading = false
        }
    }

    LaunchedEffect(gameId) { reload() }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        val g = game
        if (loading || g == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                if (!g.bannerUrl.isNullOrBlank() || !g.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = g.bannerUrl ?: g.coverUrl,
                        contentDescription = g.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(0xFF3A3A3A))
                    )
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Black.copy(alpha = 0.85f))
                            )
                        )
                )

                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable {
                            scope.launch {
                                GameRepository.toggleFavorite(g.id)
                                isFavorite = GameRepository.isFavorite(g.id)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) AccentBlue else White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        Modifier
                            .width(70.dp)
                            .aspectRatio(0.68f)
                            .clip(RoundedCornerShape(Dimens.GameCardRadius))
                            .background(CardBackground)
                            .border(
                                width = Dimens.GameCardBorder,
                                color = CardBorder,
                                shape = RoundedCornerShape(Dimens.GameCardRadius)
                            )
                    ) {
                        if (!g.coverUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = g.coverUrl,
                                contentDescription = g.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = g.title,
                        color = White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(Modifier.padding(Dimens.ScreenPadding)) {
                Spacer(Modifier.height(16.dp))
                Text("Описание", style = MaterialTheme.typography.titleMedium)
                Text(g.description ?: "—", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(16.dp))
                Text("Разработчик", style = MaterialTheme.typography.titleMedium)
                Text(g.developer ?: "—", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(16.dp))
                Text("Дата выхода", style = MaterialTheme.typography.titleMedium)
                Text(g.releaseDate ?: "—", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(20.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AccentBlue.copy(alpha = 0.5f))
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val filled = userRating != null && i <= userRating!!
                        Text(
                            text = "★",
                            color = if (filled) AccentBlue else StarGray,
                            fontSize = 40.sp,
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        val wasSame = userRating == i
                                        if (wasSame) {
                                            GameRepository.submitRating(g.id, 0)
                                        } else {
                                            GameRepository.submitRating(g.id, i)
                                        }
                                        reload()
                                    }
                                }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        val count = when (i) {
                            1 -> g.rating1Count
                            2 -> g.rating2Count
                            3 -> g.rating3Count
                            4 -> g.rating4Count
                            5 -> g.rating5Count
                            else -> 0
                        }
                        val isUser = userRating == i
                        Text(
                            text = "$count",
                            color = if (isUser) AccentBlue else GrayText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}