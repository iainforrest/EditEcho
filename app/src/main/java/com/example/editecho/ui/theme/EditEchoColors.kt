package com.example.editecho.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * EditEcho light color scheme.
 */
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF1976D2),
    secondary = Color(0xFF03A9F4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF0288D1),
    tertiary = Color(0xFF00BCD4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF0097A7),
    error = Color(0xFFF44336),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFD32F2F),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
    scrim = Color(0x52000000)
)

/**
 * EditEcho dark color scheme.
 */
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF81D4FA),
    onSecondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF0288D1),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = Color(0xFF80DEEA),
    onTertiary = Color(0xFF006064),
    tertiaryContainer = Color(0xFF0097A7),
    onTertiaryContainer = Color(0xFFB2EBF2),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF616161),
    scrim = Color(0x52000000)
)

/**
 * Extension function to get a specific color from the ColorScheme.
 */
fun ColorScheme.getColor(colorType: ColorType): Color {
    return when (colorType) {
        ColorType.Primary -> primary
        ColorType.OnPrimary -> onPrimary
        ColorType.PrimaryContainer -> primaryContainer
        ColorType.OnPrimaryContainer -> onPrimaryContainer
        ColorType.Secondary -> secondary
        ColorType.OnSecondary -> onSecondary
        ColorType.SecondaryContainer -> secondaryContainer
        ColorType.OnSecondaryContainer -> onSecondaryContainer
        ColorType.Tertiary -> tertiary
        ColorType.OnTertiary -> onTertiary
        ColorType.TertiaryContainer -> tertiaryContainer
        ColorType.OnTertiaryContainer -> onTertiaryContainer
        ColorType.Error -> error
        ColorType.OnError -> onError
        ColorType.ErrorContainer -> errorContainer
        ColorType.OnErrorContainer -> onErrorContainer
        ColorType.Background -> background
        ColorType.OnBackground -> onBackground
        ColorType.Surface -> surface
        ColorType.OnSurface -> onSurface
        ColorType.SurfaceVariant -> surfaceVariant
        ColorType.OnSurfaceVariant -> onSurfaceVariant
        ColorType.Outline -> outline
        ColorType.OutlineVariant -> outlineVariant
        ColorType.Scrim -> scrim
    }
}

/**
 * Enum class representing the different color types available in the app.
 */
enum class ColorType {
    Primary,
    OnPrimary,
    PrimaryContainer,
    OnPrimaryContainer,
    Secondary,
    OnSecondary,
    SecondaryContainer,
    OnSecondaryContainer,
    Tertiary,
    OnTertiary,
    TertiaryContainer,
    OnTertiaryContainer,
    Error,
    OnError,
    ErrorContainer,
    OnErrorContainer,
    Background,
    OnBackground,
    Surface,
    OnSurface,
    SurfaceVariant,
    OnSurfaceVariant,
    Outline,
    OutlineVariant,
    Scrim
}

/**
 * Color scheme for the EditEcho app.
 */
object EditEchoColors {
    // Primary colors
    val Primary = Color(0xFF2196F3) // Blue
    val Secondary = Color(0xFF03A9F4) // Light Blue
    val Accent = Color(0xFF00BCD4) // Cyan
    
    // Background colors
    val Background = Color(0xFFF5F5F5) // Light Gray
    val Surface = Color(0xFFFFFFFF) // White
    
    // Text colors
    val PrimaryText = Color(0xFF212121) // Dark Gray
    val SecondaryText = Color(0xFF757575) // Medium Gray
    
    // Status colors
    val Error = Color(0xFFF44336) // Red
    val Success = Color(0xFF4CAF50) // Green
    val Warning = Color(0xFFFFC107) // Amber
    val Info = Color(0xFF2196F3) // Blue
    
    // Tone button colors
    val ToneButtonActive = Primary
    val ToneButtonInactive = Color(0xFFE0E0E0)
    val ToneButtonText = Color(0xFF000000)
    val ToneButtonBorder = Color(0xFFBDBDBD)
} 