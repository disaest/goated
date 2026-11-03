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
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Geologica,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        color = White
    ),
    titleLarge = TextStyle(
        fontFamily = Geologica,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = White
    ),
    titleMedium = TextStyle(
        fontFamily = Geologica,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = White
    ),
    bodyMedium = TextStyle(
        fontFamily = Geologica,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = White
    ),
    bodySmall = TextStyle(
        fontFamily = Geologica,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = GrayText
    ),
    labelLarge = TextStyle(
        fontFamily = Geologica,
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        color = CardBorder
    )
)