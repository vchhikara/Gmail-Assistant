package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val currency_rupee: ImageVector
  get() {
    if (_currency_rupee != null) {
      return _currency_rupee!!
    }
    _currency_rupee =
      ImageVector.Builder(
          name = "currency_rupee",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(13.73f, 21f)
            lineTo(7f, 14f)
            verticalLineTo(12f)
            horizontalLineToRelative(3.5f)
            quadToRelative(1.33f, 0f, 2.29f, -0.86f)
            reflectiveQuadTo(13.95f, 9f)
            horizontalLineTo(6f)
            verticalLineTo(7f)
            horizontalLineToRelative(7.65f)
            quadTo(13.23f, 6.13f, 12.39f, 5.56f)
            reflectiveQuadTo(10.5f, 5f)
            horizontalLineTo(6f)
            verticalLineTo(3f)
            horizontalLineTo(18f)
            verticalLineTo(5f)
            horizontalLineTo(14.75f)
            quadToRelative(0.35f, 0.43f, 0.63f, 0.93f)
            reflectiveQuadTo(15.8f, 7f)
            horizontalLineTo(18f)
            verticalLineTo(9f)
            horizontalLineTo(15.98f)
            quadToRelative(-0.2f, 2.13f, -1.75f, 3.56f)
            reflectiveQuadTo(10.5f, 14f)
            horizontalLineTo(9.78f)
            lineToRelative(6.73f, 7f)
            horizontalLineTo(13.73f)
            close()
          }
        }
        .build()
    return _currency_rupee!!
  }

private var _currency_rupee: ImageVector? = null

