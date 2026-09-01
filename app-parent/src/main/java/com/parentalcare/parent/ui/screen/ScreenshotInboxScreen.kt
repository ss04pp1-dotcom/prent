package com.parentalcare.parent.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.core.design.theme.SharedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotInboxScreen(
    viewModel: ScreenshotInboxViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onScreenshotClick: (String) -> Unit
) {
    val docs by viewModel.screenshots.collectAsState()
    var filterUnread by remember { mutableStateOf(false) }
    
    val shown = if (filterUnread) docs.filter { it.isUnread } else docs
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

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
            Text("Inbox", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.FilterList, contentDescription = null, tint = SharedColors.DarkTextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            val unreadCount = docs.count { it.isUnread }
            BadgedBox(badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = SharedColors.DarkTextPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !filterUnread,
                onClick = { filterUnread = false },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SharedColors.DarkSurface,
                    labelColor = SharedColors.DarkTextPrimary,
                    selectedContainerColor = SharedColors.ParentPrimary,
                    selectedLabelColor = Color.White,
                ),
            )
            FilterChip(
                selected = filterUnread,
                onClick = { filterUnread = true },
                label = { Text("Unread") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SharedColors.DarkSurface,
                    labelColor = SharedColors.DarkTextPrimary,
                    selectedContainerColor = SharedColors.ParentPrimary,
                    selectedLabelColor = Color.White,
                ),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            items(shown) { item ->
                val timeStr = timeFormat.format(Date(item.capturedAt ?: 0L))
                val bmp = item.thumbnailBase64?.let {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                InboxCard(
                    time = timeStr,
                    deviceName = item.childDeviceId,
                    unread = item.isUnread,
                    bitmap = bmp,
                    onClick = { onScreenshotClick(item.screenshotId) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InboxCard(
    time: String,
    deviceName: String,
    unread: Boolean,
    bitmap: Bitmap?,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SharedColors.DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SharedColors.ParentPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (unread) {
                        Box(modifier = Modifier.size(8.dp).background(SharedColors.ParentPrimary, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        "New Screenshot",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = SharedColors.DarkTextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(deviceName, style = MaterialTheme.typography.bodySmall, color = SharedColors.DarkTextSecondary)
                Text(time, style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextTertiary)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SharedColors.DarkTextTertiary)
        }
    }
}
