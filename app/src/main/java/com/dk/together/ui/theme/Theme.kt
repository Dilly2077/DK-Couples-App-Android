package com.dk.together.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val EveluneInk = Color(0xFF2F2329)
val EveluneRoseDeep = Color(0xFF8B3346)
val EveluneRose = Color(0xFFD96878)
val EveluneRosePale = Color(0xFFF8D9D8)
val EveluneCard = Color(0xFFFFEAE7)
val EveluneBackground = Color(0xFFFFF7F3)
val EveluneMuted = Color(0xFF7E6A70)
val EveluneWhite = Color(0xFFFFFFFF)
val EvelunePeach = Color(0xFFFFD8C7)

// Compatibility aliases for older, currently-unused prototype files.
val Ink = EveluneInk
val Purple = EveluneRose
val PurpleDeep = EveluneRoseDeep
val Lavender = EveluneRosePale
val LavenderSoft = Color(0xFFFFF1EF)
val Blush = EveluneRosePale
val Peach = EvelunePeach
val Butter = Color(0xFFFFEDC7)
val Cream = EveluneBackground
val White = EveluneWhite
val MutedInk = EveluneMuted
val Border = Color(0xFFF0D5D2)

private val EveluneColors = lightColorScheme(
    primary = EveluneRoseDeep,
    onPrimary = EveluneWhite,
    primaryContainer = EveluneRosePale,
    onPrimaryContainer = EveluneInk,
    secondary = EveluneRose,
    onSecondary = EveluneWhite,
    secondaryContainer = Color(0xFFFFE7E2),
    onSecondaryContainer = EveluneInk,
    tertiary = Color(0xFFB96E5B),
    tertiaryContainer = EvelunePeach,
    onTertiaryContainer = EveluneInk,
    background = EveluneBackground,
    onBackground = EveluneInk,
    surface = EveluneWhite,
    onSurface = EveluneInk,
    surfaceVariant = Color(0xFFFFEFEC),
    onSurfaceVariant = EveluneMuted,
    outline = Color(0xFFC5AEB1),
    outlineVariant = Color(0xFFF0D5D2)
)

private val EveluneTypography = Typography(
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = EveluneInk),
    headlineLarge = TextStyle(fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = EveluneInk),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, color = EveluneInk),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, color = EveluneInk),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, color = EveluneInk),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal, color = EveluneInk),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal, color = EveluneInk),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, color = EveluneInk),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = EveluneInk)
)

@Composable
fun DKTogetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EveluneColors, typography = EveluneTypography, content = content)
}
