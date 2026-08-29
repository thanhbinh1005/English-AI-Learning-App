package com.thanhbinh.englishaiapp

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanhbinh.englishaiapp.presentation.components.BottomNavigationBar
import com.thanhbinh.englishaiapp.presentation.navigation.NavGraph
import com.thanhbinh.englishaiapp.presentation.navigation.Screen
import com.thanhbinh.englishaiapp.ui.theme.EnglishAIAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EnglishAIAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun rememberIsKeyboardOpen(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            val insets = ViewCompat.getRootWindowInsets(view)
            val imeVisibleByInsets = insets?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
            val imeVisibleByHeight = keypadHeight > screenHeight * 0.15
            keyboardState.value = imeVisibleByInsets || imeVisibleByHeight
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return keyboardState
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Kiem tra trang thai ban phim qua ca ViewTreeObserver va Compose IME Insets
    val isKeyboardOpenByView by rememberIsKeyboardOpen()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpenByInsets = imeBottomPadding > 20.dp
    val isKeyboardOpen = isKeyboardOpenByView || isKeyboardOpenByInsets

    // Khong hien BottomBar o cac man hinh toan man hinh (Camera, ScanResult)
    val isFullScreen = currentRoute?.startsWith(Screen.Camera.route) == true ||
            currentRoute?.startsWith(Screen.ScanResult.route) == true

    // An hoan toan BottomNavigationBar khi ban phim dang mo
    val shouldShowBottomBar = !isKeyboardOpen && !isFullScreen

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
