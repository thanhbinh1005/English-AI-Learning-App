package com.thanhbinh.englishaiapp.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.thanhbinh.englishaiapp.presentation.navigation.bottomNavItems

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute?.substringBefore("?") == screen.route.substringBefore("?")
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(text = screen.label) },
                icon = {
                    screen.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = screen.label
                        )
                    }
                }
            )
        }
    }
}