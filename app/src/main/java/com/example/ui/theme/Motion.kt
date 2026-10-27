package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Kit motion: press 150ms, fill 250ms, slide 300ms, spring 300ms. */
object KitMotion {
    val SpringEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    fun <T> press() = tween<T>(150, easing = FastOutSlowInEasing)
    fun <T> fill() = tween<T>(250, easing = FastOutSlowInEasing)
    fun <T> slide() = tween<T>(300, easing = LinearOutSlowInEasing)
    fun <T> spring() = tween<T>(300, easing = SpringEasing)
}

/** Kit radii, as built in the approved mockup. */
object KitShapes {
    val check = RoundedCornerShape(8.dp)
    val control = RoundedCornerShape(16.dp)
    val controlThumb = RoundedCornerShape(12.dp)
    val nav = RoundedCornerShape(20.dp)
    val navThumb = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(24.dp)
    val panel = RoundedCornerShape(topStart = 48.dp)
    val sheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
}
