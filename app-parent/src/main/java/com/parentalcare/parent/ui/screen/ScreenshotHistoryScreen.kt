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

@Composable
fun ScreenshotHistoryScreen(onBack: () -> Unit, onCalendar: () -> Unit) {
    val today = listOf(
        "5:42 PM" to "Rahim's Phone",
        "4:20 PM" to "Rahim's Phone",
        "2:15 PM" to "Ayesha's Phone",
    )
    val yesterday = listOf(
        "8:30 PM" to "Rahim's Phone",
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
            Text(
                "History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = SharedColors.DarkTextPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
            onClick = onCalendar,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Aug 25, 2024", color = SharedColors.DarkTextPrimary, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = SharedColors.DarkTextTertiary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                SectionLabel("Today")
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(today) { (time, device) -> HistoryItem(time, device) }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel("Aug 24, 2024")
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(yesterday) { (time, device) -> HistoryItem(time, device) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = SharedColors.DarkTextSecondary,
    )
}

@Composable
private fun HistoryItem(time: String, device: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SharedColors.DarkSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(time, style = MaterialTheme.typography.labelLarge, color = SharedColors.DarkTextSecondary, modifier = Modifier.width(80.dp))
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
            Text(device, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
            Text("View", color = SharedColors.ParentPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
