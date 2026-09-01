package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.parent.R
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    val rows = listOf(
        Triple(R.string.notif_enable, true, true),
        Triple(R.string.notif_alerts, true, true),
        Triple(R.string.notif_received, true, true),
        Triple(R.string.notif_requested, true, true),
        Triple(R.string.notif_other, false, false),
        Triple(R.string.notif_online, true, true),
        Triple(R.string.notif_offline, true, true),
        Triple(R.string.notif_perms, true, true),
    )
    val states = remember { mutableStateOf(rows.map { it.second }) }

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
                stringResource(R.string.settings_notifications),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                rows.forEachIndexed { i, (labelRes, _, enabled) ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(labelRes),
                                color = SharedColors.DarkTextPrimary,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Switch(
                                checked = states.value[i],
                                onCheckedChange = { v ->
                                    states.value = states.value.toMutableList().also { it[i] = v }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SharedColors.ParentPrimary,
                                    uncheckedTrackColor = SharedColors.DarkBorder,
                                ),
                            )
                        }
                        if (i < rows.size - 1) {
                            Divider(color = SharedColors.DarkBorder)
                        }
                    }
                }
            }
        }
    }
}
