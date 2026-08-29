package com.thanhbinh.englishaiapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Unified Brand Colors (Matches XML Semantic Palette) ---
val AppLightBackground = Color(0xFFF4F7FB)
val AppLightSurface = Color(0xFFFFFFFF)
val AppLightSurfaceVariant = Color(0xFFF1F5F9)
val AppLightPrimary = Color(0xFF0D6EFD)
val AppLightOnBackground = Color(0xFF0F172A)
val AppLightOnSurface = Color(0xFF0F172A)
val AppLightOnSurfaceVariant = Color(0xFF64748B)

val AppDarkBackground = Color(0xFF121824)
val AppDarkSurface = Color(0xFF1E293B)
val AppDarkSurfaceVariant = Color(0xFF2A374A)
val AppDarkPrimary = Color(0xFF3B82F6)
val AppDarkOnBackground = Color(0xFFF8FAFC)
val AppDarkOnSurface = Color(0xFFF8FAFC)
val AppDarkOnSurfaceVariant = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = AppDarkPrimary,
    secondary = Color(0xFF60A5FA),
    tertiary = Color(0xFF34D399),
    background = AppDarkBackground,
    surface = AppDarkSurface,
    surfaceVariant = AppDarkSurfaceVariant,
    onBackground = AppDarkOnBackground,
    onSurface = AppDarkOnSurface,
    onSurfaceVariant = AppDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = AppLightPrimary,
    secondary = Color(0xFF2563EB),
    tertiary = Color(0xFF10B981),
    background = AppLightBackground,
    surface = AppLightSurface,
    surfaceVariant = AppLightSurfaceVariant,
    onBackground = AppLightOnBackground,
    onSurface = AppLightOnSurface,
    onSurfaceVariant = AppLightOnSurfaceVariant
)

@Composable
fun EnglishAIAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
