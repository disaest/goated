package com.example.goated.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.goated.R
import com.example.goated.components.*
import com.example.goated.data.AuthRepository
import com.example.goated.data.UserRepository
import com.example.goated.navigation.LocalSnackbar
import com.example.goated.navigation.Routes
import com.example.goated.navigation.Tab
import com.example.goated.navigation.navigateToTab
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.ProfileViewModel
import io.github.jan.supabase.auth.status.SessionStatus

/**
 * Раздел «Профиль»: пока сессия загружается — индикатор,
 * без входа — экран регистрации/входа, после входа — профиль.
 */
@Composable
fun ProfileRoute(navController: NavHostController) {
    val status by AuthRepository.sessionStatus.collectAsStateWithLifecycle()
    when (val s = status) {
        is SessionStatus.Initializing -> LoadingView()
        is SessionStatus.Authenticated -> ProfileScreen(navController, s.session.user?.id ?: return)
        else -> RegistrationScreen()
    }
}

@Composable
fun ProfileScreen(navController: NavHostController, userId: String) {
    val vm: ProfileViewModel = viewModel(key = "profile_$userId") { ProfileViewModel(userId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    var editingStyle by remember { mutableStateOf(false) }
    var avatarKey by remember(state.profile) { mutableStateOf(state.profile?.avatarColor ?: "blue") }
    var bannerKey by remember(state.profile) { mutableStateOf(state.profile?.bannerStyle ?: "blue") }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); vm.messageShown() }
    }

    when {
        state.loading -> LoadingView()
        state.profile == null -> Column(Modifier.fillMaxSize()) {
            ErrorView(state.error ?: "Профиль не найден", onRetry = vm::refresh, Modifier.weight(1f))
            TextButton(onClick = vm::signOut, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)) {
                Text("Выйти из аккаунта", color = AccentBlue)
            }
        }
        else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ProfileTopBar(
                profile = state.profile,
                avatarColorKey = avatarKey,
                bannerKey = bannerKey
            ) {
                IconButton(
                    onClick = { editingStyle = !editingStyle },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        painterResource(if (editingStyle) R.drawable.ic_close else R.drawable.ic_edit),
                        contentDescription = "Оформление профиля",
                        tint = White
                    )
                }
            }

            Column(Modifier.padding(Dimens.ScreenPadding)) {
                if (editingStyle) {
                    StyleEditor(
                        avatarKey = avatarKey,
                        bannerKey = bannerKey,
                        saving = state.savingStyle,
                        onAvatar = { avatarKey = it },
                        onBanner = { bannerKey = it },
                        onSave = { vm.saveStyle(avatarKey, bannerKey); editingStyle = false }
                    )
                    Spacer(Modifier.height(Dimens.SectionSpacing))
                }

                SectionLabel("Избранное", trailing = "${state.favorites.size} / ${UserRepository.FAVORITES_LIMIT}")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                    state.favorites.take(UserRepository.FAVORITES_LIMIT).forEach { game ->
                        GameCard(
                            title = game.title,
                            coverUrl = game.coverUrl,
                            modifier = Modifier.weight(1f),
                            showTitle = false,
                            showRating = false,
                            onClick = { navController.navigate(Routes.gameCard(game.id)) }
                        )
                    }
                    repeat(UserRepository.FAVORITES_LIMIT - state.favorites.size.coerceAtMost(UserRepository.FAVORITES_LIMIT)) {
                        AddTile(Modifier.weight(1f)) { navController.navigateToTab(Tab.Search) }
                    }
                }
                ProfileDivider()

                SectionLabel("Недавняя активность")
                Spacer(Modifier.height(10.dp))
                if (state.recent.isEmpty()) {
                    Text("Оцените игру — она появится здесь", style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                        state.recent.take(4).forEach { item ->
                            GameCard(
                                title = item.game.title,
                                coverUrl = item.game.coverUrl,
                                rating = item.userRating,
                                modifier = Modifier.weight(1f),
                                showTitle = false,
                                onClick = { navController.navigate(Routes.gameCard(item.game.id)) }
                            )
                        }
                        repeat(4 - state.recent.size.coerceAtMost(4)) { Spacer(Modifier.weight(1f)) }
                    }
                }
                ProfileDivider()

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.COMPLETED_GAMES) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ПРОЙДЕННЫЕ ИГРЫ", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    Text("${state.completedCount}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.ic_chevron_right), null, tint = SecondaryText, modifier = Modifier.size(18.dp))
                }
                ProfileDivider()

                OutlinedButton(
                    onClick = vm::signOut,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryText),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(painterResource(R.drawable.ic_logout), null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из аккаунта", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** Выбор цвета аватара и баннера из готовых вариантов. */
@Composable
private fun StyleEditor(
    avatarKey: String,
    bannerKey: String,
    saving: Boolean,
    onAvatar: (String) -> Unit,
    onBanner: (String) -> Unit,
    onSave: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(CardBackground).border(1.dp, CardBorder, shape).padding(14.dp)
    ) {
        SectionLabel("Цвет аватара")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ProfileStyles.avatarColors.forEach { (key, color) ->
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(color)
                        .border(2.dp, if (key == avatarKey) White else color, CircleShape)
                        .clickable { onAvatar(key) },
                    contentAlignment = Alignment.Center
                ) {
                    if (key == avatarKey) Icon(painterResource(R.drawable.ic_check), null, tint = White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        SectionLabel("Баннер")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileStyles.banners.forEach { (key, colors) ->
                val itemShape = RoundedCornerShape(6.dp)
                Box(
                    Modifier.weight(1f).height(36.dp).clip(itemShape)
                        .background(Brush.verticalGradient(colors))
                        .border(2.dp, if (key == bannerKey) White else CardBorder, itemShape)
                        .clickable { onBanner(key) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        AccentButton(if (saving) "Сохранение…" else "Сохранить", Modifier.fillMaxWidth(), enabled = !saving, onClick = onSave)
    }
}

/** Пустой слот избранного: переход к поиску, чтобы выбрать игру. */
@Composable
private fun AddTile(modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(Dimens.GameCardRadius)
    Box(
        modifier.aspectRatio(Dimens.CoverAspectRatio).clip(shape).border(1.dp, CardBorder, shape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.ic_plus), "Добавить в избранное", tint = MutedText, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ProfileDivider() {
    Spacer(Modifier.height(16.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(AccentBlue.copy(alpha = 0.45f)))
    Spacer(Modifier.height(16.dp))
}
