package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

// Google Light Brand Colors
val LightBackground = Color(0xFFFFFFFF)
val LightSurfaceStructure = Color(0xFFF5F7FA)
val LightGoogleBlue = Color(0xFF4285F4)
val LightGoogleRed = Color(0xFFEA4335)
val LightGoogleYellow = Color(0xFFFBBC05)
val LightGoogleGreen = Color(0xFF34A853)
val LightTextPrimary = Color(0xFF202124)
val LightTextSecondary = Color(0xFF5F6368)
val LightCyanAI = Color(0xFF5EC9FF)
val LightBorder = Color(0xFFE1E3E1)

// Google Dark Brand Colors
val DarkBackground = Color(0xFF121417)
val DarkSurfaceSecondary = Color(0xFF1B1F24)
val DarkSurfaceElevated = Color(0xFF232A31)
val DarkBorderSubtle = Color(0xFF2D333B)
val DarkTextPrimary = Color(0xFFE8EAED)
val DarkTextSecondary = Color(0xFF9AA0A6)
val DarkGoogleBlue = Color(0xFF8AB4F8)
val DarkGoogleRed = Color(0xFFEE675C)
val DarkGoogleYellow = Color(0xFFFDD663)
val DarkGoogleGreen = Color(0xFF81C995)
val DarkCyanAI = Color(0xFF7DD3FC)

// Compat delegates to avoid breaking compilation and to dynamically resolve colors based on theme preference
val BrandPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val BrandSecondary: Color
    @Composable
    get() = MaterialTheme.colorScheme.primaryContainer

val BrandTertiary: Color
    @Composable
    get() = MaterialTheme.colorScheme.tertiary

val BackgroundPurple: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val OnBackgroundPurple: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val SurfaceBorder: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val DarkPurpleText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onPrimaryContainer

val MediumGrayText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val ActivePillBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondaryContainer

val ActivePillText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSecondaryContainer

val LightSurfaceBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val LightRippleBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant // Fallback to surfaceVariant
