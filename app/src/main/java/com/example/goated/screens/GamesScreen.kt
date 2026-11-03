package com.example.goated.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.goated.R
import com.example.goated.components.*
import com.example.goated.data.AuthRepository
import com.example.goated.navigation.Routes
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.CompletedGamesViewModel

/** Пройденные игры пользователя (reviews.is_completed = true) с его оценками. */
@Composable
fun GamesScreen(navController: NavHostController) {
    val userId = AuthRepository.currentUserId()
    if (userId == null) {
        MessageView("Войдите в аккаунт, чтобы видеть пройденные игры")
        return
    }
    val vm: CompletedGamesViewModel = viewModel(key = "completed_$userId") { CompletedGamesViewModel(userId) }
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize()) {
        ProfileTopBar(state.profile)
        Row(
            Modifier.fillMaxWidth()
                .clickable { navController.popBackStack() }
                .padding(horizontal = Dimens.ScreenPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(painterResource(R.drawable.ic_back), "Назад", tint = White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ПРОЙДЕННЫЕ ИГРЫ", style = MaterialTheme.typography.titleSmall)
        }
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = vm::refresh)
            state.games.isEmpty() -> MessageView("Пока нет пройденных игр. Отметьте игру как «Пройдено» в её карточке")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = Dimens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                verticalArrangement = Arrangement.spacedBy(Dimens.GameRowSpacing)
            ) {
                items(state.games, key = { it.game.id }) { item ->
                    GameCard(
                        title = item.game.title,
                        coverUrl = item.game.coverUrl,
                        rating = item.userRating,
                        onClick = { navController.navigate(Routes.gameCard(item.game.id)) }
                    )
                }
            }
        }
    }
}
