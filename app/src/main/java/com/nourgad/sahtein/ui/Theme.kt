package com.nourgad.sahtein.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF1F7A5A)
val Amber = Color(0xFFE59E1A)
val Red = Color(0xFFD64545)
val Blue = Color(0xFF3B7DD8)
val Purple = Color(0xFF8A5CD1)
val Grey = Color(0xFF9AA0A6)

private val Light = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F0E3),
    onPrimaryContainer = Color(0xFF00382A),
    secondary = Color(0xFF4F6F62),
    tertiary = Amber,
    background = Color(0xFFF8FAF8),
    surface = Color(0xFFF8FAF8),
    surfaceVariant = Color(0xFFE6EEEA),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF6FD3A8),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF0E5A41),
    onPrimaryContainer = Color(0xFFD5F0E3),
    secondary = Color(0xFFB3CCC0),
    tertiary = Color(0xFFF2C063),
)

@Composable
fun SahteinTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
