package com.example.goated.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.goated.R
import com.example.goated.ui.theme.AccentBlue
import com.example.goated.ui.theme.StarEmpty
import kotlin.math.roundToInt

/** Округление до шага 0.5: 4.1 -> 4.0, 4.3 -> 4.5. */
fun roundToHalf(value: Float): Float = (value * 2f).roundToInt() / 2f

/**
 * Пять звёзд с поддержкой половинок.
 * В интерактивном режиме нажатие на левую половину звезды ставит x.5, на правую — целое значение.
 */
@Composable
fun StarRating(
    rating: Float?,
    modifier: Modifier = Modifier,
    starSize: Dp = 18.dp,
    spacing: Dp = 2.dp,
    activeColor: Color = AccentBlue,
    interactive: Boolean = false,
    onRatingChange: (Float) -> Unit = {}
) {
    val value = rating?.let(::roundToHalf) ?: 0f
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        repeat(5) { index ->
            val fill = (value - index).coerceIn(0f, 1f)
            Box(
                Modifier
                    .size(starSize)
                    .then(
                        if (interactive) Modifier.pointerInput(index) {
                            detectTapGestures { point ->
                                onRatingChange(index + if (point.x < size.width / 2f) 0.5f else 1f)
                            }
                        } else Modifier
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = StarEmpty,
                    modifier = Modifier.fillMaxSize()
                )
                if (fill > 0f) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                clipRect(right = size.width * fill) { this@drawWithContent.drawContent() }
                            }
                    )
                }
            }
        }
    }
}

/** Число оценки в формате «4,5». */
fun formatRating(value: Float): String = String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", value)

/** «1 оценка», «3 оценки», «12 оценок». */
fun ratingsCountText(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "оценка"
        mod10 in 2..4 && mod100 !in 12..14 -> "оценки"
        else -> "оценок"
    }
    return "$count $word"
}
