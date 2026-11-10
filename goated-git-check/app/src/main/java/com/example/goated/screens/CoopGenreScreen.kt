package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.goated.R
import com.example.goated.components.*
import com.example.goated.navigation.Routes
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.GenreGamesViewModel

@Composable
fun CoopGenreScreen(navController: NavHostController, genreId: Int) {
    val vm: GenreGamesViewModel = viewModel(key = "genre_$genreId") { GenreGamesViewModel(genreId) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        GradientTopBar(
            subtitle = state.genreName.ifEmpty { " " },
            onBack = { navController.popBackStack() }
        )
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = vm::load)
            state.games.isEmpty() -> MessageView("В этом жанре пока нет игр")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(Dimens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                verticalArrangement = Arrangement.spacedBy(Dimens.GameRowSpacing)
            ) {
                item { BackTile { navController.popBackStack() } }
                items(state.games, key = { it.id }) { game ->
                    GameCard(
                        title = game.title,
                        coverUrl = game.coverUrl,
                        rating = game.averageRating,
                        onClick = { navController.navigate(Routes.gameCard(game.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BackTile(onClick: () -> Unit) {
    val shape = RoundedCornerShape(Dimens.GameCardRadius)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(Dimens.CoverAspectRatio)
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.ic_back), contentDescription = "Назад", tint = SecondaryText)
    }
}
