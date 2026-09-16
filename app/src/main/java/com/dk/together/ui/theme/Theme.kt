package com.dk.together.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF241B38)
val Purple = Color(0xFF6E45D8)
val PurpleDeep = Color(0xFF4D2EB1)
val Lavender = Color(0xFFE9DEFF)
val LavenderSoft = Color(0xFFF4EEFF)
val Blush = Color(0xFFFFE6EF)
val Peach = Color(0xFFFFE8D8)
val Butter = Color(0xFFFFF2B8)
val Cream = Color(0xFFFFFBF7)
val White = Color(0xFFFFFFFF)
val MutedInk = Color(0xFF6E647C)
val Border = Color(0xFFE2D8EE)

private val DkColors = lightColorScheme(
    primary = Purple,
    onPrimary = White,
    primaryContainer = Lavender,
    onPrimaryContainer = Ink,
    secondary = Color(0xFFD55E91),
    onSecondary = White,
    secondaryContainer = Blush,
    onSecondaryContainer = Ink,
    tertiary = Color(0xFFB86639),
    tertiaryContainer = Peach,
    onTertiaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = LavenderSoft,
    onSurfaceVariant = MutedInk,
    outline = Color(0xFF998CA9),
    outlineVariant = Border
)

private val DkTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.ExtraBold, color = Ink),
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, color = Ink),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold, color = Ink),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = Ink),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal, color = Ink),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal, color = Ink),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
)

@Composable
fun DKTogetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DkColors, typography = DkTypography, content = content)
}
