package com.dk.together.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Midnight = Color(0xFF17132B)
val SurfacePurple = Color(0xFF2D2255)
val SurfacePurple2 = Color(0xFF392C68)
val Lavender = Color(0xFFA777FF)
val SoftPink = Color(0xFFF3A9C7)
val Peach = Color(0xFFF1BEA7)
val Cream = Color(0xFFF4EEFF)
val Muted = Color(0xFFBEB3D3)

private val DkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = Color(0xFF1C1232),
    secondary = SoftPink,
    tertiary = Peach,
    background = Midnight,
    onBackground = Cream,
    surface = SurfacePurple,
    onSurface = Cream,
    surfaceVariant = SurfacePurple2,
    onSurfaceVariant = Muted,
    outline = Color(0xFF685C88)
)

@Composable
fun DKTogetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DkColors, typography = MaterialTheme.typography, content = content)
}
