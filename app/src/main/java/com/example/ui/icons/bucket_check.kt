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
public val bucket_check: ImageVector
  get() {
    if (_bucket_check != null) {
      return _bucket_check!!
    }
    _bucket_check =
      ImageVector.Builder(
          name = "bucket_check",
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
            moveTo(6.73f, 21f)
            quadTo(5.98f, 21f, 5.43f, 20.5f)
            reflectiveQuadTo(4.75f, 19.27f)
            lineTo(3f, 7f)
            horizontalLineTo(6f)
            verticalLineTo(5f)
            quadTo(6f, 4.17f, 6.59f, 3.59f)
            reflectiveQuadTo(8f, 3f)
            horizontalLineToRelative(8f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(18f, 5f)
            verticalLineTo(7f)
            horizontalLineToRelative(3f)
            lineTo(19.25f, 19.27f)
            quadTo(19.13f, 20f, 18.56f, 20.5f)
            reflectiveQuadTo(17.25f, 21f)
            horizontalLineTo(6.73f)
            close()
            moveTo(5.3f, 9f)
            lineTo(6.73f, 19f)
            horizontalLineTo(17.28f)
            lineTo(18.7f, 9f)
            horizontalLineTo(5.3f)
            close()
            moveToRelative(5.65f, 8f)
            lineTo(15.9f, 12.05f)
            lineToRelative(-1.42f, -1.4f)
            lineToRelative(-3.53f, 3.53f)
            lineTo(9.53f, 12.75f)
            lineTo(8.1f, 14.18f)
            lineTo(10.95f, 17f)
            close()
            moveTo(8f, 7f)
            horizontalLineToRelative(8f)
            verticalLineTo(5f)
            horizontalLineTo(8f)
            verticalLineTo(7f)
            close()
            moveToRelative(4f, 7f)
            close()
          }
        }
        .build()
    return _bucket_check!!
  }

private var _bucket_check: ImageVector? = null

