package com.example.goated.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goated.ui.theme.*

@Composable
fun MainTabs(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("Games", "Genres")
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Black)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEachIndexed { index, label ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (selected == index) White else GrayText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    val w = animateDpAsState(
                        targetValue = if (selected == index) 82.dp else 0.dp,
                        label = "u"
                    )
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(w.value)
                            .background(AccentBlue)
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AccentBlue.copy(alpha = 0.5f))
        )
    }
}