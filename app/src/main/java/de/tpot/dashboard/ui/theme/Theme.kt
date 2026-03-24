package de.tpot.dashboard.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TpotDarkColorScheme = darkColorScheme(
    primary = TpotGreen,
    onPrimary = TpotBackground,
    primaryContainer = TpotGreenDark,
    onPrimaryContainer = TpotGreenLight,

    secondary = TpotCyan,
    onSecondary = TpotBackground,
    secondaryContainer = TpotSurfaceVariant,
    onSecondaryContainer = TpotCyan,

    tertiary = TpotAmber,
    onTertiary = TpotBackground,

    error = TpotRed,
    onError = TpotBackground,

    background = TpotBackground,
    onBackground = TpotTextPrimary,

    surface = TpotSurface,
    onSurface = TpotTextPrimary,
    surfaceVariant = TpotSurfaceVariant,
    onSurfaceVariant = TpotTextSecondary,

    outline = TpotBorder,
    outlineVariant = TpotTextTertiary,
)

@Composable
fun TpotDashboardTheme(content: @Composable () -> Unit) {
    val colorScheme = TpotDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TpotBackground.toArgb()
            window.navigationBarColor = TpotBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TpotTypography,
        content = content
    )
}
