package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ChildProfileScreen(
    onBack: () -> Unit,
    onRequestScreenshot: () -> Unit,
    onSeeAll: () -> Unit,
) {
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
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = SharedColors.DarkTextPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SharedColors.ParentPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ChildCare, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(56.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rahim", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = SharedColors.DarkTextSecondary, modifier = Modifier.size(16.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(SharedColors.StatusSuccessDark, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Online", style = MaterialTheme.typography.labelMedium, color = SharedColors.StatusSuccessDark)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Stats list
        Surface(shape = RoundedCornerShape(14.dp), color = SharedColors.DarkSurface) {
            Column {
                StatRow(icon = Icons.Outlined.Image, label = "Last Screenshot", value = "Today, 5:42 PM")
                Divider(color = SharedColors.DarkBorder)
                StatRow(icon = Icons.Outlined.Collections, label = "Total Screenshots", value = "26")
                Divider(color = SharedColors.DarkBorder)
                StatRow(icon = Icons.Outlined.List, label = "Total Requests", value = "12")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryCta(text = "Request Screenshot", onClick = onRequestScreenshot)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Recent Screenshots",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SharedColors.DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onSeeAll) { Text("See All", color = SharedColors.ParentPrimary) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecentRow("5:42 PM")
            RecentRow("4:10 PM")
            RecentRow("2:30 PM")
        }
    }
}

@Composable
private fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
        Text(value, color = SharedColors.DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(4.dp))
        Text(">", color = SharedColors.DarkTextTertiary)
    }
}

@Composable
private fun RecentRow(time: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SharedColors.DarkSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SharedColors.DarkSurfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = SharedColors.DarkTextTertiary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(time, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
            Text(">", color = SharedColors.DarkTextTertiary)
        }
    }
}
