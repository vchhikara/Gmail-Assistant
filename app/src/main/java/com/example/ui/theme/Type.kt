package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Montserrat (variable, wght axis). Weight axis applies on API 26+; older devices use Regular. */
@OptIn(ExperimentalTextApi::class)
val KitFont: FontFamily = FontFamily(
    listOf(300, 400, 500, 600, 700).map { w ->
        Font(
            R.font.montserrat_variable,
            weight = FontWeight(w),
            variationSettings = FontVariation.Settings(FontVariation.weight(w)),
        )
    }
)

val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    titleLarge = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = KitFont, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp),
)
