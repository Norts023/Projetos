package com.skytycoon.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SkyDarkColorScheme = darkColorScheme(
    primary = SkyAccentBlue,
    secondary = SkyAccentGreen,
    tertiary = SkyAccentPurple,
    background = SkyBlack,
    surface = SkyDarkBlue,
    error = SkyAccentRed,
    surfaceVariant = SkyCardBg,
    outline = SkyDivider,
    onPrimary = SkyTextPrimary,
    onSecondary = SkyTextPrimary,
    onBackground = SkyTextPrimary,
    onSurface = SkyTextPrimary,
    onError = SkyTextPrimary
)

@Composable
fun SkyTycoonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SkyDarkColorScheme,
        typography = SkyTypography,
        content = content
    )
}
