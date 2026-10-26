package com.spdev.litehindi.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.spdev.litehindi.data.KeyboardTheme

@Composable
fun AppTheme(theme: KeyboardTheme, content: @Composable () -> Unit) {
    val scheme = if (theme.isDark) {
        darkColorScheme(
            primary = theme.accent,
            onPrimary = Color.Black,
            secondary = theme.actionBg,
            onSecondary = Color.White,
            background = theme.bgBottom,
            onBackground = theme.textPrimary,
            surface = theme.keyBg,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.keyBg,
            onSurfaceVariant = theme.textSecondary,
            outline = theme.keyBorder,
            error = Color(0xFFEF5350)
        )
    } else {
        lightColorScheme(
            primary = theme.accent,
            onPrimary = Color.White,
            secondary = theme.actionBg,
            onSecondary = Color.White,
            background = theme.bgBottom,
            onBackground = theme.textPrimary,
            surface = theme.keyBg,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.keyBg,
            onSurfaceVariant = theme.textSecondary,
            outline = theme.keyBorder,
            error = Color(0xFFC62828)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
