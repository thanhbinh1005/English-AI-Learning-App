package com.thanhbinh.englishaiapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thanhbinh.englishaiapp.presentation.screens.HomeScreen.HomeScreen
import com.thanhbinh.englishaiapp.presentation.screens.Screens_Chat.ChatAIScreen
import com.thanhbinh.englishaiapp.presentation.screens.Screens_Scan.CameraScreen
import com.thanhbinh.englishaiapp.presentation.screens.Screens_Scan.ScanResultScreen
import com.thanhbinh.englishaiapp.presentation.screens.Screens_Scan.ScanScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // --- 1. MÀN HÌNH CHÍNH (HOME) ---
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTranslate = { navController.navigate(Screen.Translate.route) },
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToVocabulary = { navController.navigate(Screen.Library.route) },
                onNavigateToChatAi = { navController.navigate(Screen.ChatAI.route) }
            )
        }

        // --- 2. CÁC MÀN HÌNH CHỨC NĂNG ---
        composable(Screen.Translate.route) { PlaceholderScreen("Màn hình Dịch") }
        composable(Screen.ChatAI.route) {
            ChatAIScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Library.route) { PlaceholderScreen("Màn hình Từ vựng") }

        // --- 3. MÀN HÌNH DANH SÁCH CÁC FILE ĐÃ QUÉT ---
        composable(Screen.Scan.route) {
            ScanScreen(
                navController = navController,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) }
            )
        }

        // --- 4. MÀN HÌNH CAMERA (QUÉT OCR) ---
        composable(Screen.Camera.route) {
            CameraScreen(
                onTextScanned = { resultText ->
                    val encodedText = java.net.URLEncoder.encode(resultText, "UTF-8")
                    navController.navigate(Screen.ScanResult.route + "/$encodedText") {
                        popUpTo(Screen.Scan.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- 5. MÀN HÌNH XEM KẾT QUẢ & LƯU FILE ---
        composable(
            route = Screen.ScanResult.route + "/{scannedText}?docId={docId}",
            arguments = listOf(
                navArgument("scannedText") { type = NavType.StringType },
                navArgument("docId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val rawText = backStackEntry.arguments?.getString("scannedText") ?: ""
            val docId = backStackEntry.arguments?.getInt("docId") ?: 0

            val decodedText = try {
                java.net.URLDecoder.decode(rawText, "UTF-8")
            } catch (e: Exception) {
                rawText
            }

            ScanResultScreen(
                scannedText = decodedText,
                docId = docId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}