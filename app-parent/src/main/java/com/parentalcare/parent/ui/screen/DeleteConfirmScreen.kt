package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalcare.parent.R
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun DeleteConfirmScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.DarkTextPrimary)
            }
            Text(
                "Delete Screenshot",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        // Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SharedColors.StatusError.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = null, tint = SharedColors.StatusError, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.delete_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = SharedColors.DarkTextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.delete_body),
            style = MaterialTheme.typography.bodyMedium,
            color = SharedColors.DarkTextSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Screenshot taken on Aug 25, 2024 at 5:42 PM",
            style = MaterialTheme.typography.labelSmall,
            color = SharedColors.DarkTextTertiary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        // Preview thumbnail
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = SharedColors.DarkTextTertiary, modifier = Modifier.size(48.dp))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.delete_cancel), color = SharedColors.DarkTextSecondary)
            }
            Button(
                onClick = onDeleted,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SharedColors.StatusError, contentColor = Color.White),
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Text(stringResource(R.string.delete_confirm), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
