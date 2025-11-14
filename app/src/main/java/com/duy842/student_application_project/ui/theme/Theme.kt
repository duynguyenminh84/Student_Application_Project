// app/src/main/java/com/duy842/student_application_project/ui/theme/Theme.kt
package com.duy842.student_application_project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Brand palette (local, no external deps) ----------
private val mdPrimary               = Color(0xFF6750A4)
private val mdOnPrimary             = Color(0xFFFFFFFF)
private val mdPrimaryContainer      = Color(0xFFEADDFF)
private val mdOnPrimaryContainer    = Color(0xFF21005D)

private val mdSecondary             = Color(0xFF006D77)
private val mdOnSecondary           = Color(0xFFFFFFFF)
private val mdSecondaryContainer    = Color(0xFFB2F0F5)
private val mdOnSecondaryContainer  = Color(0xFF002022)

private val mdTertiary              = Color(0xFFEE6352)
private val mdOnTertiary            = Color(0xFFFFFFFF)
private val mdTertiaryContainer     = Color(0xFFFFDAD4)
private val mdOnTertiaryContainer   = Color(0xFF3B0904)

private val mdError                 = Color(0xFFB3261E)
private val mdOnError               = Color(0xFFFFFFFF)
private val mdErrorContainer        = Color(0xFFF9DEDC)
private val mdOnErrorContainer      = Color(0xFF410E0B)

private val mdBackground            = Color(0xFFFAFAFE)
private val mdOnBackground          = Color(0xFF1B1B1F)
private val mdSurface               = Color(0xFFFFFFFF)
private val mdOnSurface             = Color(0xFF1B1B1F)
private val mdSurfaceVariant        = Color(0xFFE7E0EC)
private val mdOnSurfaceVariant      = Color(0xFF49454F)
private val mdOutline               = Color(0xFF79747E)

// Optional “surface container” tones (use if/when needed in components)
private val surfaceContainerLow     = Color(0xFFF6F4FA)
private val surfaceContainer        = Color(0xFFF0EDF7)
private val surfaceContainerHigh    = Color(0xFFEAE7F3)

// ---------- Dark palette ----------
private val mdPrimaryDark               = Color(0xFFD0BCFF)
private val mdOnPrimaryDark             = Color(0xFF371E73)
private val mdPrimaryContainerDark      = Color(0xFF4F378B)
private val mdOnPrimaryContainerDark    = Color(0xFFEADDFF)

private val mdSecondaryDark             = Color(0xFF7DD9E0)
private val mdOnSecondaryDark           = Color(0xFF00363A)
private val mdSecondaryContainerDark    = Color(0xFF004F55)
private val mdOnSecondaryContainerDark  = Color(0xFFB2F0F5)

private val mdTertiaryDark              = Color(0xFFFFB4A6)
private val mdOnTertiaryDark            = Color(0xFF5C140D)
private val mdTertiaryContainerDark     = Color(0xFF7B2B21)
private val mdOnTertiaryContainerDark   = Color(0xFFFFDAD4)

private val mdBackgroundDark            = Color(0xFF101114)
private val mdOnBackgroundDark          = Color(0xFFE3E2E6)
private val mdSurfaceDark               = Color(0xFF121316)
private val mdOnSurfaceDark             = Color(0xFFE3E2E6)
private val mdSurfaceVariantDark        = Color(0xFF49454F)
private val mdOnSurfaceVariantDark      = Color(0xFFCAC4D0)
private val mdOutlineDark               = Color(0xFF938F99)

private val surfaceContainerLowDark     = Color(0xFF17181D)
private val surfaceContainerDark        = Color(0xFF1A1B21)
private val surfaceContainerHighDark    = Color(0xFF1E1F26)

// ---------- Color Schemes ----------
private val LightColors: ColorScheme = lightColorScheme(
    primary = mdPrimary,
    onPrimary = mdOnPrimary,
    primaryContainer = mdPrimaryContainer,
    onPrimaryContainer = mdOnPrimaryContainer,
    secondary = mdSecondary,
    onSecondary = mdOnSecondary,
    secondaryContainer = mdSecondaryContainer,
    onSecondaryContainer = mdOnSecondaryContainer,
    tertiary = mdTertiary,
    onTertiary = mdOnTertiary,
    tertiaryContainer = mdTertiaryContainer,
    onTertiaryContainer = mdOnTertiaryContainer,
    error = mdError,
    onError = mdOnError,
    errorContainer = mdErrorContainer,
    onErrorContainer = mdOnErrorContainer,
    background = mdBackground,
    onBackground = mdOnBackground,
    surface = mdSurface,
    onSurface = mdOnSurface,
    surfaceVariant = mdSurfaceVariant,
    onSurfaceVariant = mdOnSurfaceVariant,
    outline = mdOutline
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = mdPrimaryDark,
    onPrimary = mdOnPrimaryDark,
    primaryContainer = mdPrimaryContainerDark,
    onPrimaryContainer = mdOnPrimaryContainerDark,
    secondary = mdSecondaryDark,
    onSecondary = mdOnSecondaryDark,
    secondaryContainer = mdSecondaryContainerDark,
    onSecondaryContainer = mdOnSecondaryContainerDark,
    tertiary = mdTertiaryDark,
    onTertiary = mdOnTertiaryDark,
    tertiaryContainer = mdTertiaryContainerDark,
    onTertiaryContainer = mdOnTertiaryContainerDark,
    error = mdError,
    onError = mdOnError,
    errorContainer = mdErrorContainer,
    onErrorContainer = mdOnErrorContainer,
    background = mdBackgroundDark,
    onBackground = mdOnBackgroundDark,
    surface = mdSurfaceDark,
    onSurface = mdOnSurfaceDark,
    surfaceVariant = mdSurfaceVariantDark,
    onSurfaceVariant = mdOnSurfaceVariantDark,
    outline = mdOutlineDark
)

// ---------- Typography ----------
private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.SemiBold, lineHeight = 64.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.SemiBold, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.SemiBold, lineHeight = 44.sp),

    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),

    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),

    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, fontFamily = FontFamily.SansSerif),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),

    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

// ---------- Shapes ----------
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun Student_Application_ProjectTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // intentionally unused to avoid dependency on dynamic APIs
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
