package com.travelai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.prefs.ThemeMode
import com.travelai.ui.MainViewModel
import com.travelai.ui.navigation.NavGraph
import com.travelai.ui.theme.TravelAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val prefs = mainViewModel.prefs.collectAsStateWithLifecycle().value
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (prefs.themeMode) {
                ThemeMode.SystemDefault -> systemDark
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            TravelAITheme(
                darkTheme = darkTheme,
                fontScale = prefs.fontScale
            ) {
                NavGraph()
            }
        }
    }
}
