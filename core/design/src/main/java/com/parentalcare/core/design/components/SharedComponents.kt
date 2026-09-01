package com.parentalcare.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalcare.core.design.theme.ButtonTextStyle
import com.parentalcare.core.design.theme.SharedColors

/**
 * Primary CTA button. Full-width, 50dp tall, 12dp radius.
 * Used for "Get Started", "Continue", "Done", "Take Screenshot", "Send Request" etc.
 */
@Composable
fun PrimaryCta(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text = text, style = ButtonTextStyle)
    }
}

/**
 * Destructive full-width button — used for "Stop Monitoring", "Delete Permanently".
 */
@Composable
fun DestructiveCta(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
    ) {
        Text(text = text, style = ButtonTextStyle.copy(fontWeight = FontWeight.SemiBold))
    }
}

/**
 * Status badge — small pill with colored text + dot.
 *   - Active  (green)  — Monitoring Active, Online, Connected
 *   - Pending (yellow)  — Pending
 *   - Cancelled (red)   — Cancelled, Failed
 *   - Sent    (green)
 */
enum class StatusKind { ACTIVE, PENDING, CANCELLED, SENT, OFFLINE }

@Composable
fun StatusBadge(
    label: String,
    kind: StatusKind,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when (kind) {
        StatusKind.ACTIVE, StatusKind.SENT ->
            SharedColors.StatusSuccessBg to SharedColors.StatusSuccessDark
        StatusKind.PENDING ->
            SharedColors.StatusWarningBg to SharedColors.StatusWarning
        StatusKind.CANCELLED ->
            SharedColors.StatusErrorBg to SharedColors.StatusErrorDark
        StatusKind.OFFLINE ->
            SharedColors.LightBg to SharedColors.LightTextSecondary
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .background(color = fg, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

/**
 * Standard info card — rounded 16dp, elevation 1dp.
 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

/**
 * Row item with leading icon + label + trailing status text/chevron.
 * Used in Settings, Permissions, About.
 */
@Composable
fun ListItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 14.dp)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
