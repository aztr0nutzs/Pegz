package com.yourname.pegz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PegzColorScheme = darkColorScheme(
    primary = ToxicGreen,
    secondary = CyberPink,
    tertiary = ElectricBlue,
    background = VoidBlack,
    surface = RustedIron,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = ToxicGreen // Makes text on the board glow green
)

@Composable
fun PegzTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PegzColorScheme,
        typography = Typography, // Ensure you use a bold, industrial font here
        content = content
    )
}