package com.parentalcare.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shared shape system. Matches the design mockups:
 *   - Large cards: 16dp
 *   - Buttons / input fields: 12dp
 *   - Small badges: 8dp
 *   - Avatars: 50% (CircleShape applied at call site)
 */
val AppShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
