package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Design kit tokens (RQuickShare): soft green on neutral, one accent. */
@Immutable
data class KitColors(
    val bg: Color,
    val panel: Color,
    val card: Color,
    val cardPlain: Color,
    val mist: Color,
    val accent: Color,
    val onAccent: Color,
    val text: Color,
    val muted: Color,
    val fill: Color,
    val fillStrong: Color,
    val danger: Color,
    val isDark: Boolean,
)

private val Green200 = Color(0xFFBBF7D0)
private val Gray = Color(0xFF6B7280)

val LightKit = KitColors(
    bg = Color(0xFFFAF9F6),
    panel = Color(0xFFF1F2EF),
    card = Color(0xFFE3F1E7),
    cardPlain = Color.White,
    mist = Color(0xFFEAF6EE),
    accent = Color(0xFFB8F1D0),
    onAccent = Color.Black,
    text = Color.Black,
    muted = Color.Black.copy(alpha = 0.7f),
    fill = Gray.copy(alpha = 0.10f),
    fillStrong = Gray.copy(alpha = 0.20f),
    danger = Color(0xFFB94E58),
    isDark = false,
)

val DarkKit = KitColors(
    bg = Color(0xFF171717),
    panel = Color(0xFF262626),
    card = Color(0xFF404040),
    cardPlain = Color(0xFF2D2E2F),
    mist = Color(0xFF404040),
    accent = Green200,
    onAccent = Color.Black,
    text = Color(0xFFF5F5F5),
    muted = Color(0xFFF5F5F5).copy(alpha = 0.7f),
    fill = Gray.copy(alpha = 0.10f),
    fillStrong = Gray.copy(alpha = 0.20f),
    danger = Color(0xFFED9499),
    isDark = true,
)

val LocalKit = staticCompositionLocalOf { LightKit }
