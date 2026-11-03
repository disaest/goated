package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.goated.ui.theme.*

@Composable
fun GradientTopBar(title: String = "Goated") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
            .background(
                Brush.verticalGradient(colors = listOf(PrimaryBlue, Black))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )
    }
}