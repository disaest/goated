package com.example.goated

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.goated.data.Game
import com.example.goated.data.supabase
import com.example.goated.navigation.AppNavigation
import com.example.goated.ui.theme.GoatedTheme
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val games = supabase.from("games").select().decodeList<Game>()
                Log.d("SUPABASE_TEST", "Игр загружено: ${games.size}")
                games.take(3).forEach {
                    Log.d("SUPABASE_TEST", "  → ${it.title} (${it.averageRating})")
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_TEST", "Ошибка: ${e.message}")
            }
        }

        setContent {
            GoatedTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}