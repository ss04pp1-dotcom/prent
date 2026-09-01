package com.parentalcare.parent.ui.screen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.parent.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun RequestScreenshotScreen(
    viewModel: RequestScreenshotViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSent: () -> Unit,
) {
    val device by viewModel.device.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    var selected by remember { mutableStateOf(0) }
    val options = listOf(
        "Take screenshot now",
        "After 1 minute",
        "After 5 minutes",
        "After 15 minutes",
        "Custom time",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            stringResource(R.string.request_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SharedColors.DarkTextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SharedColors.ParentPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.ChildCare, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(device?.childDisplayName ?: "Loading...", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
                Text(if (device?.isOnline == true) "Online" else "Offline", style = MaterialTheme.typography.labelMedium, color = SharedColors.StatusSuccessDark)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "A request will be sent to ${device?.childDisplayName ?: "this device"} to take a screenshot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedColors.DarkTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { i, label ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                if (selected == i) SharedColors.ParentPrimary else Color.Transparent,
                                CircleShape,
                            )
                            .border(
                                width = if (selected == i) 0.dp else 2.dp,
                                color = if (selected == i) Color.Transparent else SharedColors.DarkBorder,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected == i) {
                            Box(modifier = Modifier.size(10.dp).background(Color.White, CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, color = SharedColors.DarkTextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isSending) CircularProgressIndicator(color = SharedColors.ParentPrimary) else PrimaryCta(text = stringResource(R.string.request_send), onClick = { 
            val delays = listOf(0, 60, 300, 900, 0)
            viewModel.sendRequest(delays[selected], onSent)
        })
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = SharedColors.DarkTextTertiary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "${device?.childDisplayName ?: "The child"} will be notified that a screenshot is being taken.",
                style = MaterialTheme.typography.labelMedium,
                color = SharedColors.DarkTextTertiary,
            )
        }
    }
}
