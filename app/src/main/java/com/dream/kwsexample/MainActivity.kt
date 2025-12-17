package com.dream.kwsexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dream.kwsexample.ui.screens.HomeScreen
import com.dream.kwsexample.ui.screens.KwsScreen
import com.dream.kwsexample.ui.theme.KWSExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KWSExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    when (currentScreen) {
        Screen.Home -> {
            HomeScreen(
                onNavigateToJni = { currentScreen = Screen.JniKws }
            )
        }
        Screen.JniKws -> {
            KwsScreen(
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object JniKws : Screen()
}