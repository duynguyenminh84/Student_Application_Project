package com.duy842.student_application_project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tip: if you add a custom font later (e.g., Inter/PlusJakartaSans), just
// replace FontFamily.Default below with your FontFamily.
private val AppFont = FontFamily.Default

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
    )
)
