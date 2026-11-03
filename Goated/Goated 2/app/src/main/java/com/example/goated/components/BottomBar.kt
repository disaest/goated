package com.example.goated.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.AccentBlue
import com.example.goated.ui.theme.Black
import com.example.goated.ui.theme.Dimens
import com.example.goated.ui.theme.GrayText

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        Triple(Icons.Default.Home, Screen.Main.route, Screen.Genres.route),
        Triple(Icons.Default.Person, Screen.Profile.route, null)
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Row(
        Modifier
            .fillMaxWidth()
            .background(Black)
            .height(Dimens.BottomBarHeight),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (icon, route, altRoute) ->
            val isSelected = currentRoute == route || currentRoute == altRoute
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AccentBlue else GrayText,
                modifier = Modifier
                    .size(Dimens.BottomIconSize)
                    .clickable {
                        navController.navigate(route) {
                            popUpTo(Screen.Main.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
            )
        }
    }
}