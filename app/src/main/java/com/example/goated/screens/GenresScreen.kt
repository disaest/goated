package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.goated.R
import com.example.goated.components.*
import com.example.goated.navigation.Routes
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.GenresViewModel

@Composable
fun GenresScreen(navController: NavHostController, vm: GenresViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        GradientTopBar(subtitle = "Жанры")
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = vm::load)
            state.items.isEmpty() -> MessageView("Жанры пока не добавлены")
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.GameRowSpacing)
            ) {
                items(state.items, key = { it.genre.id }) { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                        GenreCard(
                            name = item.genre.name,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Routes.genreGames(item.genre.id)) }
                        )
                        item.games.forEach { game ->
                            GameCard(
                                title = game.title,
                                coverUrl = game.coverUrl,
                                modifier = Modifier.weight(1f),
                                showTitle = false,
                                showRating = false,
                                onClick = { navController.navigate(Routes.gameCard(game.id)) }
                            )
                        }
                        repeat(3 - item.games.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreCard(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(Dimens.GameCardRadius)
    Box(
        modifier
            .aspectRatio(Dimens.CoverAspectRatio)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(GradientTop, GenreGradientMiddle, Black)))
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(6.dp))
            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = SecondaryText, modifier = Modifier.size(16.dp))
        }
    }
}

