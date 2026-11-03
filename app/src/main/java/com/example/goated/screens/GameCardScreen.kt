package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.goated.R
import com.example.goated.components.*
import com.example.goated.data.Game
import com.example.goated.data.ReviewWithAuthor
import com.example.goated.navigation.LocalSnackbar
import com.example.goated.navigation.Tab
import com.example.goated.navigation.navigateToTab
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.GameCardViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Карточка игры. Порядок блоков: баннер → обложка и название → разработчик и дата →
 * описание → рейтинг → скриншоты → рецензии.
 */
@Composable
fun GameCardScreen(navController: NavHostController, gameId: String) {
    val vm: GameCardViewModel = viewModel(key = "game_$gameId") { GameCardViewModel(gameId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(it)
            vm.messageShown()
        }
    }

    val game = state.game
    when {
        state.loading -> LoadingView()
        game == null -> Column(Modifier.fillMaxSize().statusBarsPadding()) {
            BackButton(Modifier.padding(12.dp)) { navController.popBackStack() }
            ErrorView(state.error ?: "Игра не найдена", onRetry = vm::load)
        }
        else -> Box(Modifier.fillMaxSize()) {
          Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Banner(
                game = game,
                isFavorite = state.isFavorite,
                showFavorite = state.isLoggedIn,
                onBack = { navController.popBackStack() },
                onFavorite = vm::toggleFavorite
            )
            Column(Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                GameInfo(game)
                BlueDivider()
                RatingBlock(
                    game = game,
                    myRating = state.myRating,
                    isCompleted = state.myReview?.isCompleted == true,
                    isLoggedIn = state.isLoggedIn,
                    saving = state.saving,
                    onRate = vm::setRating,
                    onToggleCompleted = vm::toggleCompleted,
                    onLogin = { navController.navigateToTab(Tab.Profile) }
                )
                if (state.screenshots.isNotEmpty()) {
                    BlueDivider()
                    ScreenshotPager(state.screenshots, game.title)
                }
                BlueDivider()
                ReviewsBlock(
                    state = state,
                    onSave = vm::saveComment,
                    onDelete = vm::deleteComment,
                    onLogin = { navController.navigateToTab(Tab.Profile) }
                )
                Spacer(Modifier.height(24.dp))
            }
          }
          // затемнение под статус-баром, чтобы текст не накладывался на системные иконки
          Box(
              Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
                  .background(Black.copy(alpha = 0.6f))
          )
        }
    }
}

// ---------------- Баннер, обложка, информация ----------------

@Composable
private fun Banner(
    game: Game,
    isFavorite: Boolean,
    showFavorite: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit
) {
    Box(Modifier.fillMaxWidth().height(260.dp)) {
        AsyncImage(
            model = game.bannerUrl ?: game.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.verticalGradient(listOf(Black.copy(alpha = 0.35f), Color.Transparent, DarkBackground)))
        )
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BackButton(onClick = onBack)
            if (showFavorite) {
                RoundIconButton(
                    icon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                    description = if (isFavorite) "Убрать из избранного" else "В избранное",
                    tint = if (isFavorite) AccentBlue else White,
                    onClick = onFavorite
                )
            }
        }
        Row(
            Modifier.align(Alignment.BottomStart).padding(horizontal = Dimens.ScreenPadding),
            verticalAlignment = Alignment.Bottom
        ) {
            GameCover(game.title, game.coverUrl, Modifier.width(96.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.padding(bottom = 4.dp)) {
                Text(game.title, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun GameInfo(game: Game) {
    Spacer(Modifier.height(14.dp))
    InfoLine("Разработчик", game.developer ?: "—")
    Spacer(Modifier.height(6.dp))
    InfoLine("Дата выхода", formatDate(game.releaseDate))
    Spacer(Modifier.height(14.dp))
    Text(
        game.description?.takeIf { it.isNotBlank() } ?: "Описание пока отсутствует",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = MutedText)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = White)
    }
}

// ---------------- Рейтинг ----------------

@Composable
private fun RatingBlock(
    game: Game,
    myRating: Float?,
    isCompleted: Boolean,
    isLoggedIn: Boolean,
    saving: Boolean,
    onRate: (Float?) -> Unit,
    onToggleCompleted: () -> Unit,
    onLogin: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Средний рейтинг по оценкам всех пользователей
        Text("Средняя оценка", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarRating(game.averageRating, starSize = 26.dp, spacing = 3.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                game.averageRating?.let(::formatRating) ?: "—",
                style = MaterialTheme.typography.titleLarge
            )
        }
        val countText = when {
            game.ratingCount > 0 -> ratingsCountText(game.ratingCount)
            game.averageRating == null -> "Пока нет оценок"
            else -> null
        }
        if (countText != null) {
            Spacer(Modifier.height(4.dp))
            Text(countText, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(22.dp))

        // Собственная оценка пользователя
        Text("Ваша оценка", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(10.dp))
        if (!isLoggedIn) {
            Text(
                "Войдите в аккаунт, чтобы оценивать игры и писать рецензии",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            AccentButton("Войти", onClick = onLogin)
            return@Column
        }
        StarRating(
            rating = myRating,
            starSize = 44.dp,
            spacing = 6.dp,
            interactive = !saving,
            onRatingChange = { onRate(it) }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            myRating?.let { "Ваша оценка: ${formatRating(it)} из 5" }
                ?: "Нажмите на звезду. Левая половина — половина балла",
            style = MaterialTheme.typography.bodySmall,
            color = if (myRating != null) AccentBlue else MutedText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip(
                text = "Пройдено",
                checked = isCompleted,
                enabled = !saving,
                onClick = onToggleCompleted
            )
            if (myRating != null) {
                ToggleChip(
                    text = "Удалить оценку",
                    checked = false,
                    enabled = !saving,
                    icon = R.drawable.ic_close,
                    onClick = { onRate(null) }
                )
            }
        }
    }
}

// ---------------- Скриншоты ----------------

/** Один квадратный контейнер, скриншоты листаются влево-вправо. */
@Composable
private fun ScreenshotPager(urls: List<String>, title: String) {
    val pagerState = rememberPagerState { urls.size }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        SectionLabel("Скриншоты", trailing = "${pagerState.currentPage + 1} / ${urls.size}")
        Spacer(Modifier.height(10.dp))
        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Black)
        ) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = "Скриншот $title ${page + 1}",
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (urls.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(urls.size) { i ->
                    Box(
                        Modifier.size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (i == pagerState.currentPage) AccentBlue else StarEmpty)
                    )
                }
            }
        }
    }
}

// ---------------- Рецензии ----------------

@Composable
private fun ReviewsBlock(
    state: GameCardViewModel.UiState,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onLogin: () -> Unit
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable(state.myComment) { mutableStateOf(state.myComment.orEmpty()) }

    SectionLabel("Рецензии", trailing = if (state.reviews.isNotEmpty()) "${state.reviews.size}" else null)
    Spacer(Modifier.height(12.dp))

    when {
        !state.isLoggedIn -> TextButton(onClick = onLogin) {
            Text("Войдите, чтобы написать рецензию", color = AccentBlue, style = MaterialTheme.typography.labelLarge)
        }
        state.myComment != null && !editing -> {
            ReviewCard(
                author = "Ваша рецензия",
                rating = state.myRating,
                text = state.myComment!!,
                date = state.myReview?.createdAt,
                highlighted = true,
                actions = {
                    SmallIconButton(R.drawable.ic_edit, "Редактировать") { editing = true }
                    SmallIconButton(R.drawable.ic_close, "Удалить") { onDelete() }
                }
            )
        }
        else -> Column {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 1000) text = it },
                placeholder = { Text("Поделитесь впечатлениями об игре…", color = MutedText) },
                minLines = 3,
                maxLines = 8,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = FieldBorder,
                    cursorColor = AccentBlue,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (state.myRating != null) "Оценка ${formatRating(state.myRating!!)} будет показана рядом с рецензией"
                    else "${text.length} / 1000",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                if (editing) {
                    TextButton(onClick = { editing = false; text = state.myComment.orEmpty() }) {
                        Text("Отмена", color = SecondaryText)
                    }
                }
                AccentButton(
                    "Сохранить",
                    enabled = text.isNotBlank() && !state.saving,
                    onClick = { onSave(text); editing = false }
                )
            }
        }
    }

    val others = state.otherReviews
    if (others.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        others.forEach { review ->
            ReviewItem(review)
            Spacer(Modifier.height(8.dp))
        }
    } else if (state.myComment == null) {
        Spacer(Modifier.height(8.dp))
        Text("Рецензий пока нет — станьте первым", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReviewItem(review: ReviewWithAuthor) {
    ReviewCard(
        author = "@${review.authorName}",
        rating = review.rating,
        text = review.comment.orEmpty(),
        date = review.createdAt,
        highlighted = false
    )
}

@Composable
private fun ReviewCard(
    author: String,
    rating: Float?,
    text: String,
    date: String?,
    highlighted: Boolean,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, if (highlighted) AccentBlue.copy(alpha = 0.5f) else CardBorder, shape)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(StarEmpty, 26.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(author, style = MaterialTheme.typography.labelLarge)
                if (rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StarRating(rating, starSize = 13.dp, spacing = 1.dp)
                        Spacer(Modifier.width(4.dp))
                        Text(formatRating(rating), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            actions()
        }
        Spacer(Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
        formatDateTime(date)?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ---------------- Мелкие элементы ----------------

@Composable
private fun BlueDivider() {
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(AccentBlue.copy(alpha = 0.45f)))
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) =
    RoundIconButton(R.drawable.ic_back, "Назад", White, onClick, modifier)

@Composable
private fun RoundIconButton(
    icon: Int,
    description: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.size(40.dp).clip(CircleShape).background(Black.copy(alpha = 0.55f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), description, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SmallIconButton(icon: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(painterResource(icon), description, tint = SecondaryText, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToggleChip(text: String, checked: Boolean, enabled: Boolean, icon: Int = R.drawable.ic_check, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .background(if (checked) AccentBlue.copy(alpha = 0.18f) else CardBackground)
            .border(1.dp, if (checked) AccentBlue else CardBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (checked || icon != R.drawable.ic_check) {
            Icon(painterResource(icon), null, tint = if (checked) AccentBlue else SecondaryText, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (checked) White else SecondaryText)
    }
}

@Composable
fun AccentButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentBlue,
            contentColor = White,
            disabledContainerColor = AccentBlue.copy(alpha = 0.35f),
            disabledContentColor = White.copy(alpha = 0.6f)
        ),
        modifier = modifier.heightIn(min = 38.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun formatDate(value: String?): String =
    value?.let { runCatching { LocalDate.parse(it.take(10)).format(dateFormat) }.getOrNull() ?: it } ?: "—"

private fun formatDateTime(value: String?): String? =
    value?.let { runCatching { OffsetDateTime.parse(it).toLocalDate().format(dateFormat) }.getOrNull() }
