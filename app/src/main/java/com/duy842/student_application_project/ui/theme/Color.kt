package com.duy842.student_application_project.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Brand (banger-friendly but accessible) ----
val Primary40 = Color(0xFF6750FF)   // Vibrant indigo
val Primary80 = Color(0xFFB9A7FF)

val Secondary40 = Color(0xFF00BFA6) // Mint teal
val Secondary80 = Color(0xFF6CF1DB)

val Tertiary40 = Color(0xFFFF7A59)  // Coral pop
val Tertiary80 = Color(0xFFFFB8A7)

// ---- Neutrals & surfaces ----
val Neutral10 = Color(0xFF121316)
val Neutral20 = Color(0xFF1C1E22)
val Neutral90 = Color(0xFFEDEFF3)
val Neutral98 = Color(0xFFF8F9FC)

val SurfaceVariantLight = Color(0xFFE7E7F2)
val SurfaceVariantDark = Color(0xFF3B3B48)
val OutlineLight = Color(0xFF757680)
val OutlineDark = Color(0xFF8C8D97)

// ---- Status colors (used in chips/snackbars if needed) ----
val Success = Color(0xFF34C759)
val Warning = Color(0xFFFFC107)
val Danger  = Color(0xFFFF4D4F)

// ---- Optional gradient accents for banners/charts ----
val GradientStart = Color(0xFF6A5BFF)
val GradientEnd   = Color(0xFF00D1B2)

// Back-compat for any older references that might still exist
val Purple80 = Primary80
val Purple40 = Primary40
val PurpleGrey80 = SurfaceVariantLight
val PurpleGrey40 = SurfaceVariantDark
val Pink80 = Tertiary80
val Pink40 = Tertiary40
