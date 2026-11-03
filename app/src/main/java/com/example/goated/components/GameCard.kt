package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.goated.ui.theme.*

/**
 * Карточка игры: вертикальная обложка (без растягивания, ContentScale.Crop),
 * название под обложкой и звёзды рейтинга под названием.
 */
@Composable
fun GameCard(
    title: String,
    coverUrl: String?,
    modifier: Modifier = Modifier,
    rating: Float? = null,
    showTitle: Boolean = true,
    showRating: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        GameCover(title = title, coverUrl = coverUrl)
        if (showTitle) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = White,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showRating) {
            Spacer(Modifier.height(3.dp))
            CardRating(rating)
        }
    }
}

@Composable
fun GameCover(title: String, coverUrl: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.GameCardRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(Dimens.CoverAspectRatio)
            .clip(shape)
            .background(CardBackground)
            .border(1.dp, CardBorder, shape),
        contentAlignment = Alignment.Center
    ) {
        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

/** Звёзды + число средней оценки. Размер звёзд подстраивается под ширину карточки. */
@Composable
private fun CardRating(rating: Float?) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val numberWidth = 18.dp
        val starSize = min((maxWidth - numberWidth - 6.dp) / 5, 16.dp)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            StarRating(rating = rating, starSize = starSize, spacing = 0.dp)
            Spacer(Modifier.width(3.dp))
            Text(
                text = rating?.let(::formatRating) ?: "—",
                color = if (rating != null) SecondaryText else MutedText,
                fontSize = 10.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
