package com.parentalcare.core.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Shared color palette. Hex values are pulled directly from the supplied
 * Parental Care design mockups.
 *
 * - Child app uses [ChildColors] (light theme, purple accent #5B4DFF).
 * - Parent app uses [ParentColors] (dark premium, #7C3AED on #0F172A).
 */
object SharedColors {
    // Brand purples
    val ChildPrimary = Color(0xFF5B4DFF)
    val ChildPrimaryDark = Color(0xFF4538CC)
    val ChildPrimaryLight = Color(0xFF8B83FF)

    val ParentPrimary = Color(0xFF7C3AED)
    val ParentPrimaryVariant = Color(0xFF8B5CF6)
    val ParentPrimaryGlow = Color(0x557C3AED)

    // Status
    val StatusSuccess = Color(0xFF22C55E)
    val StatusSuccessBg = Color(0xFFDCFCE7)
    val StatusSuccessDark = Color(0xFF10B981)

    val StatusError = Color(0xFFEF4444)
    val StatusErrorBg = Color(0xFFFEE2E2)
    val StatusErrorDark = Color(0xFFDC2626)

    val StatusWarning = Color(0xFFF59E0B)
    val StatusWarningBg = Color(0xFFFEF3C7)

    // Neutrals — light (child)
    val LightBg = Color(0xFFF5F6FA)
    val LightSurface = Color(0xFFFFFFFF)
    val LightTextPrimary = Color(0xFF1A1C1E)
    val LightTextSecondary = Color(0xFF6B7280)
    val LightTextTertiary = Color(0xFF9CA3AF)
    val LightBorder = Color(0xFFE5E7EB)

    // Neutrals — dark (parent)
    val DarkBg = Color(0xFF0F172A)
    val DarkSurface = Color(0xFF1E293B)
    val DarkSurfaceVariant = Color(0xFF1F2937)
    val DarkTextPrimary = Color(0xFFF8FAFC)
    val DarkTextSecondary = Color(0xFF94A3B8)
    val DarkTextTertiary = Color(0xFF64748B)
    val DarkBorder = Color(0xFF334155)
}
