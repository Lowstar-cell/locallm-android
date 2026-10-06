package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LocalLmColorScheme = darkColorScheme(
    primary = SignalBlue,
    onPrimary = Ink,
    primaryContainer = Color(0xFF1C3558),
    onPrimaryContainer = Color(0xFFDCEBFF),

    secondary = Cyan,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF123B43),
    onSecondaryContainer = Color(0xFFC6F6FC),

    tertiary = Success,
    onTertiary = Ink,
    tertiaryContainer = Color(0xFF173B2A),
    onTertiaryContainer = Color(0xFFCDF4DC),

    error = Danger,
    onError = Color(0xFF3A0808),
    errorContainer = Color(0xFF4A1818),
    onErrorContainer = Color(0xFFFFDADA),

    background = DeepBackground,
    onBackground = TextBright,
    surface = MidBackground,
    onSurface = TextBright,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextMuted,
    surfaceContainerLowest = Ink,
    surfaceContainerLow = DeepBackground,
    surfaceContainer = MidBackground,
    surfaceContainerHigh = ElevatedSurface,
    surfaceContainerHighest = RaisedSurface,

    outline = Border,
    outlineVariant = SoftBorder,
    inverseSurface = TextBright,
    inverseOnSurface = Ink,
    inversePrimary = StrongBlue,
    scrim = Color(0xFF000000)
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LocalLmColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
