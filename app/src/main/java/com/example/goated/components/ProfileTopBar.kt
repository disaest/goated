package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.goated.R
import com.example.goated.data.Profile
import com.example.goated.ui.theme.*

/** Заранее подготовленные варианты оформления профиля (значения хранятся в profiles). */
object ProfileStyles {
    val avatarColors: List<Pair<String, Color>> = listOf(
        "blue" to AccentBlue,
        "violet" to Color(0xFF8066B3),
        "green" to Color(0xFF568B78),
        "orange" to Color(0xFFC27A4B)
    )

    val banners: List<Pair<String, List<Color>>> = listOf(
        "blue" to listOf(GradientTop, Black),
        "violet" to listOf(Color(0xFF51406F), Black),
        "green" to listOf(Color(0xFF315B50), Black),
        "ember" to listOf(Color(0xFF78452F), Black)
    )

    fun avatarColor(key: String?): Color =
        avatarColors.firstOrNull { it.first == key }?.second ?: avatarColors.first().second

    fun banner(key: String?): List<Color> =
        banners.firstOrNull { it.first == key }?.second ?: banners.first().second
}

/** Аватар — круг выбранного цвета с иконкой пользователя. */
@Composable
fun Avatar(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_avatar),
            contentDescription = "Аватар",
            tint = White,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

/** Шапка профиля: баннер-градиент, аватар, никнейм и био. */
@Composable
fun ProfileTopBar(
    profile: Profile?,
    modifier: Modifier = Modifier,
    avatarColorKey: String? = profile?.avatarColor,
    bannerKey: String? = profile?.bannerStyle,
    action: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(ProfileStyles.banner(bannerKey)))
            .statusBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(ProfileStyles.avatarColor(avatarColorKey), 72.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                "@${profile?.username ?: ""}",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val bio = profile?.bio
            if (!bio.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(bio, style = MaterialTheme.typography.bodySmall, color = SecondaryText, textAlign = TextAlign.Center)
            }
        }
        action()
    }
}
