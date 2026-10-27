package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun KitColors.toScheme() =
    if (isDark) {
        darkColorScheme(
            primary = accent, onPrimary = onAccent,
            primaryContainer = card, onPrimaryContainer = text,
            secondary = accent, onSecondary = onAccent,
            background = bg, onBackground = text,
            surface = panel, onSurface = text,
            surfaceVariant = card, onSurfaceVariant = muted,
            outline = fillStrong, error = danger,
        )
    } else {
        lightColorScheme(
            primary = accent, onPrimary = onAccent,
            primaryContainer = card, onPrimaryContainer = text,
            secondary = accent, onSecondary = onAccent,
            background = bg, onBackground = text,
            surface = panel, onSurface = text,
            surfaceVariant = card, onSurfaceVariant = muted,
            outline = fillStrong, error = danger,
        )
    }

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val kit = if (darkTheme) DarkKit else LightKit
    CompositionLocalProvider(LocalKit provides kit) {
        MaterialTheme(colorScheme = kit.toScheme(), typography = Typography, content = content)
    }
}

/** Shorthand for the current kit tokens. */
val kit: KitColors
    @Composable get() = LocalKit.current
