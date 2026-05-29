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
public val newspaper: ImageVector
  get() {
    if (_newspaper != null) {
      return _newspaper!!
    }
    _newspaper =
      ImageVector.Builder(
          name = "newspaper",
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
            moveTo(4f, 21f)
            quadTo(3.18f, 21f, 2.59f, 20.41f)
            reflectiveQuadTo(2f, 19f)
            verticalLineTo(3f)
            lineTo(3.68f, 4.67f)
            lineTo(5.33f, 3f)
            lineTo(7f, 4.67f)
            lineTo(8.68f, 3f)
            lineToRelative(1.65f, 1.67f)
            lineTo(12f, 3f)
            lineToRelative(1.68f, 1.67f)
            lineTo(15.33f, 3f)
            lineTo(17f, 4.67f)
            lineTo(18.68f, 3f)
            lineToRelative(1.65f, 1.67f)
            lineTo(22f, 3f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 21f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 19f)
            horizontalLineToRelative(7f)
            verticalLineTo(13f)
            horizontalLineTo(4f)
            verticalLineToRelative(6f)
            close()
            moveToRelative(9f, 0f)
            horizontalLineToRelative(7f)
            verticalLineTo(17f)
            horizontalLineTo(13f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(0f, -4f)
            horizontalLineToRelative(7f)
            verticalLineTo(13f)
            horizontalLineTo(13f)
            verticalLineToRelative(2f)
            close()
            moveTo(4f, 11f)
            horizontalLineTo(20f)
            verticalLineTo(8f)
            horizontalLineTo(4f)
            verticalLineToRelative(3f)
            close()
          }
        }
        .build()
    return _newspaper!!
  }

private var _newspaper: ImageVector? = null

