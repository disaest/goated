package com.example.goated.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.goated.components.*
import com.example.goated.data.Game
import com.example.goated.navigation.Routes
import com.example.goated.ui.theme.Dimens
import com.example.goated.viewmodel.MainViewModel

/** Главная: подборки Popular, New, Recommended сеткой по четыре карточки. */
@Composable
fun MainScreen(navController: NavHostController, vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        GradientTopBar()
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = vm::load)
            state.items.isEmpty() -> MessageView("Подборки пока пусты")
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                items(state.items, key = { it.collection.id }) { section ->
                    Column {
                        CategoryTitle(section.collection.title)
                        GameGrid(section.games) { navController.navigate(Routes.gameCard(it.id)) }
                    }
                }
            }
        }
    }
}

/**
 * Сетка карточек по четыре в ряд для использования внутри прокручиваемого списка
 * (вложенный LazyVerticalGrid внутри LazyColumn использовать нельзя).
 */
@Composable
fun GameGrid(
    games: List<Game>,
    ratingOf: (Game) -> Float? = { it.averageRating },
    onClick: (Game) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.GameRowSpacing)) {
        games.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                row.forEach { game ->
                    GameCard(
                        title = game.title,
                        coverUrl = game.coverUrl,
                        rating = ratingOf(game),
                        modifier = Modifier.weight(1f),
                        onClick = { onClick(game) }
                    )
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

