package de.mcisolutions.app.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MCIDarkColorScheme = darkColorScheme(
    primary = MCIOrange,
    onPrimary = MCIWhite,
    primaryContainer = MCIOrange.copy(alpha = 0.2f),
    onPrimaryContainer = MCIOrangeLight,
    secondary = MCIAmber,
    onSecondary = MCIBlack,
    secondaryContainer = MCIAmber.copy(alpha = 0.2f),
    onSecondaryContainer = MCIAmberLight,
    background = MCIBlack,
    onBackground = MCIWhite,
    surface = MCIDarkSurface,
    onSurface = MCIWhite,
    surfaceVariant = MCIDarkCard,
    onSurfaceVariant = MCIGrayLight,
    error = MCIRed,
    onError = MCIWhite,
    outline = MCIGrayMedium
)

@Composable
fun MCIFitnessTheme(content: @Composable () -> Unit) {
    val colorScheme = MCIDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MCIBlack.toArgb()
            window.navigationBarColor = MCIBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MCITypography,
        content = content
    )
}
