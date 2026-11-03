package com.example.goated

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.goated.navigation.AppNavigation
import com.example.goated.ui.theme.GoatedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        super.onCreate(savedInstanceState)
        setContent {
            GoatedTheme {
                AppNavigation()
            }
        }
    }
}
