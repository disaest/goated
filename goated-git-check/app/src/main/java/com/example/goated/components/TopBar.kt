package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.goated.R
import com.example.goated.ui.theme.*

@Composable
fun GradientTopBar(
    subtitle: String? = null,
    onBack: (() -> Unit)? = null
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GradientTop, Black)))
            .statusBarsPadding()
            .height(Dimens.TopBarHeight)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "Назад", tint = White)
            }
        }
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Goated", style = MaterialTheme.typography.displaySmall)
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle, style = MaterialTheme.typography.titleMedium, color = SecondaryText)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(48.dp).height(2.dp).background(AccentBlue))
            }
        }
    }
}
