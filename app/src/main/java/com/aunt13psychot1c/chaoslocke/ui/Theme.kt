package com.aunt13psychot1c.chaoslocke.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Imageboard colors: maroon on cream by day, rose on near-black by night.
private val Light = lightColorScheme(
    primary = Color(0xFF800000), onPrimary = Color(0xFFFFFFEE),
    secondary = Color(0xFF117743), onSecondary = Color.White,
    background = Color(0xFFFFFFEE), onBackground = Color(0xFF800000),
    surface = Color(0xFFF0E0D6), onSurface = Color(0xFF5A1010),
    surfaceVariant = Color(0xFFF0E0D6), onSurfaceVariant = Color(0xFF8A5A4A),
    surfaceContainer = Color(0xFFF0E0D6), surfaceContainerHigh = Color(0xFFEAD3C6),
    outline = Color(0xFFD9BFB7), error = Color(0xFFC0262D)
)
private val Dark = darkColorScheme(
    primary = Color(0xFFFF8FA8), onPrimary = Color(0xFF2A0A0E),
    secondary = Color(0xFF5BD38E), onSecondary = Color(0xFF0B2416),
    background = Color(0xFF1A1112), onBackground = Color(0xFFF2D6CE),
    surface = Color(0xFF2A1C1C), onSurface = Color(0xFFF2D6CE),
    surfaceVariant = Color(0xFF2A1C1C), onSurfaceVariant = Color(0xFFC4A097),
    surfaceContainer = Color(0xFF2A1C1C), surfaceContainerHigh = Color(0xFF362424),
    outline = Color(0xFF4A3230), error = Color(0xFFFF7A7F)
)

val Mono = FontFamily.Monospace

@Composable
fun ChaosLockeTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    // Everything a size up: easy to read on a phone at arm's length.
    val big = base.copy(
        bodyLarge = base.bodyLarge.copy(fontSize = 19.sp, lineHeight = 28.sp),
        bodyMedium = base.bodyMedium.copy(fontSize = 17.sp, lineHeight = 25.sp),
        labelLarge = base.labelLarge.copy(fontSize = 17.sp),
        titleLarge = base.titleLarge.copy(fontSize = 24.sp)
    )
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, typography = big, content = content)
}

val CodeStyle = TextStyle(fontFamily = Mono, fontSize = 18.sp, lineHeight = 26.sp)
