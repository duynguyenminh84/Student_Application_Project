package com.duy842.student_application_project.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A90E2),         // Vibrant blue
    onPrimary = Color.White,
    secondary = Color(0xFF50E3C2),       // Aqua green
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFC107),        // Amber
    onTertiary = Color.Black,
    background = Color(0xFFF5F7FA),      // Soft gray
    surface = Color.White,
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFE0E0E0),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    secondary = Color(0xFF80CBC4),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    error = Color(0xFFEF5350),
    onError = Color.Black
)

private val AppTypography = Typography(
    displaySmall = Typography().displaySmall.copy(fontSize = 28.sp),
    headlineSmall = Typography().headlineSmall.copy(fontSize = 22.sp),
    titleMedium = Typography().titleMedium.copy(fontSize = 18.sp),
    bodyMedium = Typography().bodyMedium.copy(fontSize = 16.sp),
    labelSmall = Typography().labelSmall.copy(fontSize = 12.sp)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(0.dp)
)

@Composable
fun Student_Application_ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to force custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
