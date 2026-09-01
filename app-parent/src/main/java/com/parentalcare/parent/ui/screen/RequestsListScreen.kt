package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.core.design.theme.SharedColors

private data class RequestItem(
    val time: String,
    val deviceName: String,
    val status: String,
    val pending: Boolean,
)

@Composable
fun RequestsListScreen(onBack: () -> Unit) {
    val today = listOf(
        RequestItem("5:41 PM", "Rahim's Phone", "Pending", pending = true),
        RequestItem("4:09 PM", "Ayesha's Phone", "Completed", pending = false),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.DarkTextPrimary)
            }
            Text("Requests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SharedColors.ParentPrimary,
            ) {
                Text("Sent", color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SharedColors.DarkSurface,
            ) {
                Text("Received", color = SharedColors.DarkTextSecondary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Today",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = SharedColors.DarkTextSecondary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(today) { item -> RequestRow(item) }
        }
    }
}

@Composable
private fun RequestRow(item: RequestItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SharedColors.DarkSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.time, style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary, modifier = Modifier.width(64.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SharedColors.DarkSurfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = SharedColors.DarkTextTertiary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(item.deviceName, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (item.pending) SharedColors.StatusWarning.copy(alpha = 0.18f) else SharedColors.StatusSuccessDark.copy(alpha = 0.18f),
            ) {
                Text(
                    item.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (item.pending) SharedColors.StatusWarning else SharedColors.StatusSuccessDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
