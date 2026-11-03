package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.goated.R
import com.example.goated.components.*
import com.example.goated.navigation.Routes
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.SearchViewModel

/** Поиск игр по названию. До ввода запроса — подсказки: жанры и популярные игры. */
@Composable
fun SearchScreen(navController: NavHostController, vm: SearchViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val openGame: (String) -> Unit = { navController.navigate(Routes.gameCard(it)) }

    Column(Modifier.fillMaxSize()) {
        GradientTopBar(subtitle = "Поиск игр")
        SearchField(
            value = state.query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier.padding(Dimens.ScreenPadding)
        )

        LazyColumn(
            contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.query.isBlank()) {
                item {
                    Text(
                        "Введите название игры — результаты появятся сразу",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
                if (state.genres.isNotEmpty()) {
                    item {
                        SectionLabel("Жанры")
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.genres, key = { it.id }) { genre ->
                                GenreChip(genre.name) { navController.navigate(Routes.genreGames(genre.id)) }
                            }
                        }
                    }
                }
                if (state.popular.isNotEmpty()) {
                    item {
                        SectionLabel("Популярные игры")
                        Spacer(Modifier.height(10.dp))
                        GameGrid(state.popular) { openGame(it.id) }
                    }
                }
            } else {
                when {
                    state.error != null -> item { ErrorView(state.error!!, onRetry = vm::retry, Modifier.height(240.dp)) }
                    state.searching && state.results.isEmpty() -> item { LoadingView(Modifier.height(200.dp)) }
                    state.results.isEmpty() -> item { MessageView("По запросу «${state.query.trim()}» ничего не найдено", Modifier.height(200.dp)) }
                    else -> item {
                        SectionLabel("Результаты", trailing = "${state.results.size}")
                        Spacer(Modifier.height(10.dp))
                        GameGrid(state.results) { openGame(it.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focus = LocalFocusManager.current
    val shape = RoundedCornerShape(10.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
        cursorBrush = SolidColor(AccentBlue),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                Modifier
                    .clip(shape)
                    .background(CardBackground)
                    .border(1.dp, CardBorder, shape)
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_search), null, tint = MutedText, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text("Название игры", style = MaterialTheme.typography.bodyLarge, color = MutedText)
                    inner()
                }
                if (value.isNotEmpty()) {
                    Icon(
                        painterResource(R.drawable.ic_close), "Очистить", tint = MutedText,
                        modifier = Modifier.size(20.dp).clickable { onValueChange("") }
                    )
                }
            }
        }
    )
}

@Composable
private fun GenreChip(name: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        name,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}
