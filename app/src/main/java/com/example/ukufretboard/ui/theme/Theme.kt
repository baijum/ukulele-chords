package com.example.ukufretboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Calm, warm palette inspired by natural wood tones and teal accents
private val DeepTeal = Color(0xFF00695C)
private val LightTeal = Color(0xFF80CBC4)
private val Cream = Color(0xFFFFF8F0)
private val DarkBrown = Color(0xFF3E2723)
private val WarmBrown = Color(0xFF5D4037)
private val MutedBrown = Color(0xFF8D6E63)
private val SoftAmber = Color(0xFFFFB74D)
private val LightGray = Color(0xFFF5F5F5)

private val AppColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = LightTeal,
    onPrimaryContainer = DarkBrown,
    secondary = SoftAmber,
    onSecondary = DarkBrown,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = WarmBrown,
    surface = Cream,
    onSurface = DarkBrown,
    surfaceVariant = LightGray,
    onSurfaceVariant = MutedBrown,
    background = Cream,
    onBackground = DarkBrown,
    outline = Color(0xFFBCAAA4),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.5.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        letterSpacing = 0.25.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    ),
)

/**
 * App theme for the Ukulele Chord Explorer.
 *
 * Uses a warm, calm color palette suitable for an educational tool,
 * with teal primary accents and wood-inspired neutral tones.
 */
@Composable
fun UkuFretboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
