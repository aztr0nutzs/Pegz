package com.neon.peggame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PegzColors = darkColorScheme(
    primary = Color(0xFF00FF7A),
    onPrimary = Color(0xFF00140A),
    secondary = Color(0xFF00C8FF),
    onSecondary = Color(0xFF001018),
    background = Color(0xFF040407),
    onBackground = Color(0xFFE8FFE8),
    surface = Color(0xFF07070B),
    onSurface = Color(0xFFE8FFE8),
    error = Color(0xFFFF6A00),
    onError = Color(0xFF220900)
)

@Composable
fun PegzTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PegzColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
