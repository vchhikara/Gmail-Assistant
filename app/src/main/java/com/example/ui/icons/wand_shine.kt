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
public val wand_shine: ImageVector
  get() {
    if (_wand_shine != null) {
      return _wand_shine!!
    }
    _wand_shine =
      ImageVector.Builder(
          name = "wand_shine",
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
            moveTo(8.28f, 7.72f)
            lineToRelative(-3f, -3f)
            lineTo(6.7f, 3.3f)
            lineToRelative(3f, 3f)
            lineTo(8.28f, 7.72f)
            close()
            moveTo(12f, 5.35f)
            verticalLineTo(1.1f)
            horizontalLineToRelative(2f)
            verticalLineTo(5.35f)
            horizontalLineTo(12f)
            close()
            moveToRelative(7.28f, 13.38f)
            lineToRelative(-3f, -3f)
            lineTo(17.7f, 14.3f)
            lineToRelative(3f, 3f)
            lineToRelative(-1.43f, 1.43f)
            close()
            moveTo(17.7f, 7.72f)
            lineTo(16.28f, 6.3f)
            lineToRelative(3f, -3f)
            lineTo(20.7f, 4.72f)
            lineToRelative(-3f, 3f)
            close()
            moveTo(18.65f, 12f)
            verticalLineTo(10f)
            horizontalLineTo(22.9f)
            verticalLineToRelative(2f)
            horizontalLineTo(18.65f)
            close()
            moveTo(5.13f, 21.7f)
            lineTo(2.3f, 18.88f)
            quadTo(2f, 18.58f, 2f, 18.18f)
            quadToRelative(0f, -0.4f, 0.3f, -0.7f)
            lineToRelative(9.07f, -9.1f)
            quadTo(12.25f, 7.5f, 13.5f, 7.5f)
            reflectiveQuadToRelative(2.13f, 0.88f)
            reflectiveQuadTo(16.5f, 10.5f)
            reflectiveQuadToRelative(-0.88f, 2.13f)
            lineTo(6.53f, 21.7f)
            quadTo(6.23f, 22f, 5.83f, 22f)
            reflectiveQuadTo(5.13f, 21.7f)
            close()
            moveTo(12.1f, 13.33f)
            quadToRelative(0f, 0f, -0.36f, -0.35f)
            reflectiveQuadTo(11.38f, 12.63f)
            reflectiveQuadTo(11.03f, 12.27f)
            quadTo(10.68f, 11.93f, 10.68f, 11.93f)
            lineToRelative(0.7f, 0.7f)
            lineToRelative(0.73f, 0.7f)
            close()
            moveTo(5.83f, 19.6f)
            lineTo(12.1f, 13.33f)
            lineToRelative(-1.43f, -1.4f)
            lineTo(4.43f, 18.18f)
            lineToRelative(1.4f, 1.43f)
            close()
          }
        }
        .build()
    return _wand_shine!!
  }

private var _wand_shine: ImageVector? = null

