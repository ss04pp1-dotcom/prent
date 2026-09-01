package com.parentalcare.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme

/* ---------- Color schemes ---------- */

val ChildLightColors = lightColorScheme(
    primary = SharedColors.ChildPrimary,
    onPrimary = SharedColors.LightSurface,
    primaryContainer = SharedColors.ChildPrimaryLight,
    onPrimaryContainer = SharedColors.LightSurface,
    secondary = SharedColors.ChildPrimaryDark,
    onSecondary = SharedColors.LightSurface,
    background = SharedColors.LightBg,
    onBackground = SharedColors.LightTextPrimary,
    surface = SharedColors.LightSurface,
    onSurface = SharedColors.LightTextPrimary,
    surfaceVariant = SharedColors.LightBg,
    onSurfaceVariant = SharedColors.LightTextSecondary,
    outline = SharedColors.LightBorder,
    outlineVariant = SharedColors.LightBorder,
    error = SharedColors.StatusError,
    onError = SharedColors.LightSurface,
    errorContainer = SharedColors.StatusErrorBg,
    onErrorContainer = SharedColors.StatusErrorDark,
)

val ParentDarkColors = darkColorScheme(
    primary = SharedColors.ParentPrimary,
    onPrimary = SharedColors.DarkTextPrimary,
    primaryContainer = SharedColors.ParentPrimaryVariant,
    onPrimaryContainer = SharedColors.DarkTextPrimary,
    secondary = SharedColors.ParentPrimaryVariant,
    onSecondary = SharedColors.DarkTextPrimary,
    background = SharedColors.DarkBg,
    onBackground = SharedColors.DarkTextPrimary,
    surface = SharedColors.DarkSurface,
    onSurface = SharedColors.DarkTextPrimary,
    surfaceVariant = SharedColors.DarkSurfaceVariant,
    onSurfaceVariant = SharedColors.DarkTextSecondary,
    outline = SharedColors.DarkBorder,
    outlineVariant = SharedColors.DarkBorder,
    error = SharedColors.StatusError,
    onError = SharedColors.DarkTextPrimary,
    errorContainer = SharedColors.StatusErrorDark,
    onErrorContainer = SharedColors.DarkTextPrimary,
)

/* ---------- Composable entry points ---------- */

/**
 * Use for the CHILD app — always light, purple accent.
 */
@Composable
fun ParentalCareChildTheme(content: @Composable () -> Unit) {
    val colors = ChildLightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = SharedColors.LightBg.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

/**
 * Use for the PARENT app — always dark premium, purple accent on navy.
 */
@Composable
fun ParentalCareParentTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = SharedColors.DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = ParentDarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

/**
 * Generic fallback (used by previews in shared modules).
 */
@Composable
fun ParentalCareDefaultTheme(
    useDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDark) ParentDarkColors else ChildLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
