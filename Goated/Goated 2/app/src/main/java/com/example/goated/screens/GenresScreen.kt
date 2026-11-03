package com.example.goated.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.goated.components.*
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.*

@Composable
fun GenresScreen(navController: NavHostController) {
    val genres = listOf(
        "Coop" to listOf("G1", "G2", "G3"),
        "Horror" to listOf("H1", "H2", "H3"),
        "Quest" to listOf("Q1", "Q2", "Q3"),
        "Action" to listOf("A1", "A2", "A3")
    )
    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            contentPadding = PaddingValues(vertical = Dimens.SectionSpacing)
        ) {
            genres.forEach { pair ->
                item {
                    CategoryTitle(pair.first)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                    ) {
                        pair.second.forEach { name ->
                            GameCard(
                                title = name,
                                modifier = Modifier.weight(1f),
                                showTitle = false,
                                showStars = false,
                                onClick = {
                                    navController.navigate(Screen.GenreGames.createRoute(pair.first))
                                }
                            )
                        }
                        repeat(4 - pair.second.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}