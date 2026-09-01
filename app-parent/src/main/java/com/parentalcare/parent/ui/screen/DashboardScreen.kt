package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.core.design.theme.SharedColors
import com.parentalcare.parent.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onChildClick: (String) -> Unit,
    onActivityLog: () -> Unit,
    onSettings: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    val devices by viewModel.devices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val todayScreenshots by viewModel.todayScreenshots.collectAsState()
    val todayRequests by viewModel.todayRequests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDevices()
    }

    val children = devices.map {
        ChildSummary(it.childDisplayName, it.isOnline, it.deviceName, "N/A", it.deviceId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onMenuClick) { Icon(Icons.Outlined.Menu, contentDescription = null, tint = SharedColors.DarkTextPrimary) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.dash_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = SharedColors.DarkTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(badge = { Badge() }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = SharedColors.DarkTextPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.dash_my_children),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SharedColors.DarkTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SharedColors.StatusSuccessDark.copy(alpha = 0.18f)
                ) {
                    Text(
                        "${children.count { it.online }} Active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SharedColors.StatusSuccessDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(children) { c ->
            ChildCard(c, onClick = { onChildClick(c.deviceId) })
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Stats grid
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(icon = Icons.Outlined.Image, label = "Today's Screenshots", value = todayScreenshots.toString(), modifier = Modifier.weight(1f))
                StatCard(icon = Icons.Outlined.Schedule, label = "Today's Requests", value = todayRequests.toString(), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Menu list
            MenuRow(icon = Icons.Outlined.Image, title = "Activity Log", subtitle = "View all activities", onClick = onActivityLog)
            Spacer(modifier = Modifier.height(8.dp))
            MenuRow(icon = Icons.Outlined.Settings, title = "Settings", subtitle = "Manage app settings", onClick = onSettings)
        }
    }
}

@Composable
private fun ChildCard(child: ChildSummary, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SharedColors.DarkSurface,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SharedColors.ParentPrimary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ChildCare, contentDescription = null, tint = SharedColors.ParentPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(child.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (child.online) SharedColors.StatusSuccessDark else SharedColors.DarkTextTertiary,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (child.online) "Online" else "Offline",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (child.online) SharedColors.StatusSuccessDark else SharedColors.DarkTextTertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Last screenshot: ${child.lastScreenshot}", style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary)
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SharedColors.ParentPrimary
            ) {
                Text(
                    stringResource(R.string.dash_view),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SharedColors.DarkSurface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SharedColors.ParentPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = SharedColors.DarkTextSecondary)
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
        }
    }
}

@Composable
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SharedColors.DarkSurface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SharedColors.ParentPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = SharedColors.DarkTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SharedColors.DarkTextSecondary)
            }
            Text(">", color = SharedColors.DarkTextTertiary)
        }
    }
}

data class ChildSummary(
    val name: String,
    val online: Boolean,
    val device: String,
    val lastScreenshot: String,
    val deviceId: String
)
