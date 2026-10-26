package com.spdev.litehindi.ui

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spdev.litehindi.data.BgPattern
import com.spdev.litehindi.data.KeyboardTheme

/**
 * Every theme background is a code-drawn gradient plus an optional code-drawn
 * pattern. There is not a single image asset in this app.
 */
fun Modifier.themedBackground(theme: KeyboardTheme): Modifier = this
    .background(Brush.verticalGradient(listOf(theme.bgTop, theme.bgBottom)))
    .drawBehind {
        when (theme.pattern) {
            BgPattern.NONE -> Unit

            BgPattern.DOTS -> {
                val step = 22.dp.toPx()
                val radius = 1.1.dp.toPx()
                val paint = theme.accent.copy(alpha = 0.06f)
                var y = step / 2f
                while (y < size.height) {
                    var x = step / 2f
                    while (x < size.width) {
                        drawCircle(color = paint, radius = radius, center = Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }

            BgPattern.DIAGONAL -> {
                val step = 18.dp.toPx()
                val paint = theme.accent.copy(alpha = 0.05f)
                val stroke = 1.dp.toPx()
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = paint,
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = stroke
                    )
                    x += step
                }
            }

            BgPattern.GLOW -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(theme.accent.copy(alpha = 0.10f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.45f
                    )
                )
            }
        }
    }
