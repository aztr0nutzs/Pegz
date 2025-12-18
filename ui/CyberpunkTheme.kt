// ui/CyberpunkTheme.kt

package com.neon.peggame.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- PEGZ SLIME PALETTE ---
// To better reflect the concept art provided in the imgs folder, the theme has been
// adjusted from a pure neon palette to one that blends neon with a toxic, ooze‑like
// green and rich dark surfaces.  These values are derived from the project’s
// PegzColor.kt definitions to keep colours consistent across files without
// introducing an import cycle.  See docs/PEGZ_Asset_List_v1.0.md for references.

// Primary glow: Toxic Green (0x39FF14) reminiscent of the ooze dripping off the board.
val NeonCyan = Color(0xFF39FF14)

// Secondary accent: Electric Blue (0x00E5FF) used for pegs and highlights in the
// neon bio‑lab variant of the board.
val NeonMagenta = Color(0xFF00E5FF)

// Selection/highlight: Cyber Pink (0xFF00FF) adds a vibrant contrast and is used
// for the selected peg state and error/warning messages.
val NeonRed = Color(0xFFFF00FF)

// Dark/Void surfaces.  The board frame and background are darker to
// contrast with the glowing elements.  Rusted Iron approximates the
// corroded metal look in the slime art.
val DarkVoidBlack = Color(0xFF080808)
val CyberDarkGray = Color(0xFF2B2B2B) // RustedIron analogue
val BoardFrameGray = Color(0xFF424242) // SlateGrey analogue

// Custom Color Palette for the Theme
@Immutable
data class PegNeonColors(
    val primary: Color = NeonCyan,
    val onPrimary: Color = DarkVoidBlack,
    val secondary: Color = NeonMagenta,
    val background: Color = DarkVoidBlack,
    val surface: Color = CyberDarkGray,
    val error: Color = NeonRed,
    val onBackground: Color = NeonCyan,
    // Pegs default to electric blue to stand out against the toxic board.
    val peg: Color = NeonMagenta,
    // Selected pegs glow pink to indicate active selection.
    val pegSelected: Color = NeonRed,
    // Holes are dark voids contrasted with the frame.
    val hole: Color = CyberDarkGray,
    // The triangular frame uses a slightly lighter grey for visual separation.
    val boardFrame: Color = BoardFrameGray
)

// Material3 Color Scheme using the custom colors
private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkVoidBlack,
    secondary = NeonMagenta,
    background = DarkVoidBlack,
    surface = CyberDarkGray,
    error = NeonRed,
    onBackground = NeonCyan
)

// CompositionLocal to access the custom colors easily
val LocalPegNeonColors = staticCompositionLocalOf { PegNeonColors() }

object CyberpunkTheme {
    val colors: PegNeonColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPegNeonColors.current
}

@Composable
fun CyberpunkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = PegNeonColors(
        primary = NeonCyan,
        onPrimary = DarkVoidBlack,
        secondary = NeonMagenta,
        background = DarkVoidBlack,
        surface = CyberDarkGray,
        onBackground = NeonCyan,
        peg = NeonCyan,
        pegSelected = NeonRed,
        hole = CyberDarkGray
    )

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography, // Use Material3 typography
        content = content
    )
}
