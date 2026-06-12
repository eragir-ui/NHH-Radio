package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.Black,
    secondary = AmberSecondary,
    onSecondary = Color.Black,
    tertiary = EmeraldAccent,
    background = SlateDarkBg,
    onBackground = TextPrimary,
    surface = SlateCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SlateCardBorder,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce our beautiful Dark Music Studio theme by default to offer a consistent, professional media appearance
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}
