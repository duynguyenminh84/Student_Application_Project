package com.duy842.student_application_project.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg = Brush.verticalGradient(
        colors = listOf(
            cs.surface,                                  // top
            cs.surface.copy(alpha = 0.97f),              // soften
            cs.surfaceVariant.copy(alpha = 0.60f),       // subtle band
            cs.background                                 // bottom
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // top-right decorative light (super subtle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            cs.primary.copy(alpha = 0.055f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}
