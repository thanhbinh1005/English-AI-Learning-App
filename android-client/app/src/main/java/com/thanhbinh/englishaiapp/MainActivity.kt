package com.thanhbinh.englishaiapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.thanhbinh.englishaiapp.presentation.components.BottomNavigationBar
import com.thanhbinh.englishaiapp.presentation.navigation.NavGraph
import com.thanhbinh.englishaiapp.ui.theme.EnglishAIAppTheme
// import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnglishAIAppTheme {
                val navController = rememberNavController()

                MaterialTheme {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = { BottomNavigationBar(navController) }
                    ) { innerPadding ->
                        NavGraph(navController, innerPadding)
                    }
                }
            }
        }
    }
}