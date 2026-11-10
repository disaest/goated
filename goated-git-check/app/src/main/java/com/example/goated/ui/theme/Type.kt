package com.example.goated.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.goated.R

val Geologica = FontFamily(
    Font(R.font.geologica_light, FontWeight.Light),
    Font(R.font.geologica_regular, FontWeight.Normal),
    Font(R.font.geologica_medium, FontWeight.Medium),
    Font(R.font.geologica_semibold, FontWeight.SemiBold),
    Font(R.font.geologica_bold, FontWeight.Bold),
    Font(R.font.geologica_extrabold, FontWeight.ExtraBold),
    Font(R.font.geologica_black, FontWeight.Black),
)

private fun style(weight: FontWeight, size: Int, color: androidx.compose.ui.graphics.Color = White, lineHeight: Int? = null) =
    TextStyle(
        fontFamily = Geologica,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (lineHeight ?: (size + 4)).sp,
        color = color
    )

val AppTypography = Typography(
    displaySmall = style(FontWeight.Bold, 34, lineHeight = 40),     // логотип Goated
    headlineSmall = style(FontWeight.SemiBold, 22),
    titleLarge = style(FontWeight.SemiBold, 18),
    titleMedium = style(FontWeight.Medium, 15),
    titleSmall = style(FontWeight.Medium, 13),
    bodyLarge = style(FontWeight.Normal, 15, SecondaryText, 21),
    bodyMedium = style(FontWeight.Normal, 13, SecondaryText, 18),
    bodySmall = style(FontWeight.Normal, 11, MutedText, 14),
    labelLarge = style(FontWeight.Medium, 14),
    labelMedium = style(FontWeight.Medium, 12, SecondaryText),
    labelSmall = style(FontWeight.Medium, 10, SecondaryText, 13),
)
