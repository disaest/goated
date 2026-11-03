package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.goated.data.Profile
import com.example.goated.ui.theme.*

@Composable
fun ProfileTopBar(profile: Profile? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "@${profile?.username ?: "username"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                profile?.bio ?: "",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}