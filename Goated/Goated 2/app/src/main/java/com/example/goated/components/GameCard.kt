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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.goated.ui.theme.*

@Composable
fun GameCard(
    title: String,
    coverUrl: String? = null,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showStars: Boolean = false,
    userRating: Int? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(Dimens.GameCardRadius))
                .background(CardBackground)
                .border(
                    width = Dimens.GameCardBorder,
                    color = CardBorder,
                    shape = RoundedCornerShape(Dimens.GameCardRadius)
                ),
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
                    color = GrayText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        if (showTitle) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showStars) {
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.Start) {
                for (i in 1..5) {
                    val filled = userRating != null && i <= userRating
                    Text(
                        text = "★",
                        color = if (filled) AccentBlue else StarGray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}