package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goated.ui.theme.AccentBlue

/** Заголовок подборки по центру с тонкой голубой полоской под ним. */
@Composable
fun CategoryTitle(title: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AccentBlue.copy(alpha = 0.6f))
        )
        Spacer(Modifier.height(12.dp))
    }
}

/** Небольшая подпись раздела в стиле профиля Figma: «ИЗБРАННОЕ», «НЕДАВНЯЯ АКТИВНОСТЬ». */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium)
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(trailing, style = MaterialTheme.typography.bodySmall)
        }
    }
}
