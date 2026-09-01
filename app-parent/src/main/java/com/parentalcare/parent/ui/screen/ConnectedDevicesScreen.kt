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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.parent.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

private data class DeviceItem(
    val name: String,
    val os: String,
    val online: Boolean,
    val lastSeen: String? = null,
)

@Composable
fun ConnectedDevicesScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    val devices = listOf(
        DeviceItem("Rahim's Phone", "Android 13", true),
        DeviceItem("Ayesha's Phone", "Android 12", true),
        DeviceItem("Old Device", "Android 11", false, lastSeen = "3d ago"),
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
                stringResource(R.string.devices_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        devices.forEach { device ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SharedColors.DarkSurface,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Smartphone, contentDescription = null, tint = SharedColors.ParentPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(device.name, style = MaterialTheme.typography.titleSmall, color = SharedColors.DarkTextPrimary)
                        Text(device.os, style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (device.online) SharedColors.StatusSuccessDark else SharedColors.DarkTextTertiary,
                                        CircleShape,
                                    ),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (device.online) "Online" else "Last seen ${device.lastSeen ?: "—"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (device.online) SharedColors.StatusSuccessDark else SharedColors.DarkTextTertiary,
                            )
                        }
                    }
                    Text(">", color = SharedColors.DarkTextTertiary)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SharedColors.ParentPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, SharedColors.ParentPrimary),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("+ ${stringResource(R.string.devices_add)}")
        }
    }
}
