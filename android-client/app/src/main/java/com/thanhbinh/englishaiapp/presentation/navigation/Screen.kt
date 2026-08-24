package com.thanhbinh.englishaiapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String = "",
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Translate : Screen("translate", "Dịch", Icons.Default.Language)
    object Scan : Screen("scan", "Quét", Icons.Default.QrCodeScanner)
    object ChatAI : Screen("ChatAI", "Chat AI", Icons.Default.Mic)
    object Library : Screen("vocabulary", "Từ vựng", Icons.Default.Book)

    object Camera : Screen("camera", "Camera", Icons.Default.QrCodeScanner)
    object ScanResult : Screen("scan_result", "Kết quả quét", Icons.Default.Description)

    object AddFolder : Screen("add_folder")
    object FolderDetail : Screen("folder_detail/{folderId}") {
        fun passFolderId(id: Int) = "folder_detail/$id"
    }
    object FlashcardLearn : Screen("flashcard_learn/{folderId}") {
        fun passFolderId(id: Int) = "flashcard_learn/$id"
    }
    object AddWord : Screen("add_word/{folderId}") {
        fun passFolderId(id: Int) = "add_word/$id"
    }
}

// Khai báo biến bottomNavItems ở cuối file để BottomNavigationBar import được
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Translate,
    Screen.Scan,
    Screen.ChatAI,
    Screen.Library
)