package com.thanhbinh.englishaiapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanhbinh.englishaiapp.presentation.components.BottomNavigationBar
import com.thanhbinh.englishaiapp.presentation.navigation.NavGraph
import com.thanhbinh.englishaiapp.presentation.navigation.Screen
import com.thanhbinh.englishaiapp.ui.theme.EnglishAIAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnglishAIAppTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Kiem tra ban phim ao (IME) co dang bat khong
    val isImeVisible = WindowInsets.isImeVisible
    // Khong hien BottomBar o cac man hinh toan man hinh (Camera, ScanResult)
    val isFullScreen = currentRoute?.startsWith(Screen.Camera.route) == true ||
            currentRoute?.startsWith(Screen.ScanResult.route) == true

    // An thanh BottomBar khi ban phim dang mo hoac o man hinh full-screen
    val shouldShowBottomBar = !isImeVisible && !isFullScreen

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    BottomNavigationBar(navController)
                }
            }
        ) { innerPadding ->
            NavGraph(navController, innerPadding)
        }
    }
}
