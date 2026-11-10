package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.goated.R
import com.example.goated.navigation.Tab
import com.example.goated.ui.theme.*

@Composable
fun BottomBar(selectedTab: Tab?, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Black)
            .navigationBarsPadding()
            .height(Dimens.BottomBarHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(tab) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(tab.icon),
                    contentDescription = tab.label,
                    tint = if (selected) AccentBlue else MutedText,
                    modifier = Modifier.size(Dimens.BottomIconSize)
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .background(if (selected) AccentBlue else Black)
                )
            }
        }
    }
}

