package com.parentalcare.parent.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.core.common.model.ActivityEvent
import com.parentalcare.core.common.model.EventType
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ActivityLogScreen(
    onBack: () -> Unit,
    viewModel: ActivityLogViewModel = hiltViewModel(),
) {
    val events by viewModel.activityEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
                "Activity Log",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = SharedColors.DarkTextPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Spacer(modifier = Modifier.weight(1f))
            androidx.compose.material3.CircularProgressIndicator(
                color = SharedColors.ParentPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(align = Alignment.Center)
            )
            Spacer(modifier = Modifier.weight(1f))
        } else if (error != null) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    tint = SharedColors.StatusError,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedColors.DarkTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Retry")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        } else if (events.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = SharedColors.DarkTextSecondary,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No activity yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = SharedColors.DarkTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Recent Activity",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SharedColors.DarkTextSecondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(events) { event ->
                    ActivityRow(event)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(event: ActivityEvent) {
    val (time, icon, iconColor, iconBg, text) = formatEvent(event)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            time,
            style = MaterialTheme.typography.labelMedium,
            color = SharedColors.DarkTextSecondary,
            modifier = Modifier.width(72.dp),
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
    }
}

private fun formatEvent(event: ActivityEvent): EventUiModel {
    val time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(event.timestamp))
    return when (event.type) {
        EventType.SCREENSHOT_CAPTURED, EventType.SCREENSHOT_UPLOADED, EventType.SCREENSHOT_DELIVERED -> {
            EventUiModel(time, Icons.Outlined.CheckCircle, SharedColors.StatusSuccessDark, SharedColors.StatusSuccessDark.copy(alpha = 0.2f), event.message)
        }
        EventType.REQUEST_SENT -> {
            EventUiModel(time, Icons.Outlined.Send, SharedColors.ParentPrimary, SharedColors.ParentPrimary.copy(alpha = 0.2f), event.message)
        }
        EventType.REQUEST_ACCEPTED -> {
            EventUiModel(time, Icons.Outlined.CheckCircle, SharedColors.StatusSuccessDark, SharedColors.StatusSuccessDark.copy(alpha = 0.2f), event.message)
        }
        EventType.REQUEST_REJECTED, EventType.REQUEST_EXPIRED -> {
            EventUiModel(time, Icons.Outlined.Error, SharedColors.StatusError, SharedColors.StatusError.copy(alpha = 0.2f), event.message)
        }
        EventType.DEVICE_ONLINE -> {
            EventUiModel(time, Icons.Outlined.Info, SharedColors.ParentPrimary, SharedColors.ParentPrimary.copy(alpha = 0.2f), event.message)
        }
        EventType.DEVICE_OFFLINE -> {
            EventUiModel(time, Icons.Outlined.Error, SharedColors.StatusWarning, SharedColors.StatusWarning.copy(alpha = 0.2f), event.message)
        }
        else -> {
            EventUiModel(time, Icons.Outlined.Info, SharedColors.DarkTextSecondary, SharedColors.DarkTextSecondary.copy(alpha = 0.2f), event.message)
        }
    }
}


data class EventUiModel(
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val iconBg: Color,
    val text: String
)
