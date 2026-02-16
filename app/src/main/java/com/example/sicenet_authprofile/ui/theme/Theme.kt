package com.example.sicenet_authprofile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SicenetColorScheme = lightColorScheme(
    primary = SicenetGeneralGreen,    // #4cb050 - Verde general
    secondary = SicenetButtonGreen,  // #378e3d - Verde botones
    onPrimary = SicenetOnPrimary,
    onSecondary = SicenetOnSecondary,
    background = SicenetBackground,  // Blanco
    surface = SicenetSurface,        // Blanco
    onSurface = SicenetOnSurface,
    error = SicenetError
)

@Composable
fun SICENETAuthProfileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SicenetColorScheme,
        typography = Typography,
        content = content
    )
}