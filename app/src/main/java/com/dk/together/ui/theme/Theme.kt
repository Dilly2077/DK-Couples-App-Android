package com.dk.together.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Midnight = Color(0xFF211A39)
val DeepPurple = Color(0xFF2A2148)
val SurfacePurple = Color(0xFF352A59)
val SurfacePurple2 = Color(0xFF46396F)
val SurfacePurple3 = Color(0xFF564681)
val Lavender = Color(0xFFC2A6FF)
val LavenderStrong = Color(0xFFA879FF)
val SoftPink = Color(0xFFF7B9D2)
val Peach = Color(0xFFFFCEB9)
val Cream = Color(0xFFFFFAFF)
val Muted = Color(0xFFDED5EC)

private val DkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = Color(0xFF261640),
    primaryContainer = SurfacePurple3,
    onPrimaryContainer = Cream,
    secondary = SoftPink,
    onSecondary = Color(0xFF38172A),
    tertiary = Peach,
    background = Midnight,
    onBackground = Cream,
    surface = DeepPurple,
    onSurface = Cream,
    surfaceVariant = SurfacePurple,
    onSurfaceVariant = Muted,
    outline = Color(0xFF82739F),
    outlineVariant = Color(0xFF554A70)
)

private val DkTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold),
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun DKTogetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DkColors, typography = DkTypography, content = content)
}
