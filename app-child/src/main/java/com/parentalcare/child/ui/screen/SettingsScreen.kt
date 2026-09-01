package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Info
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.child.R
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun SettingsScreen(
    onAbout: () -> Unit,
    onClearData: () -> Unit,
    viewModel: ChildSettingsViewModel = hiltViewModel()
) {
    var startOnBoot by remember { mutableStateOf(true) }
    var persistentNotif by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SharedColors.LightTextPrimary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        SectionHeader(stringResource(R.string.settings_permissions))
        Spacer(modifier = Modifier.height(8.dp))
        SettingCard {
            SettingsToggle(
                icon = Icons.Outlined.PhotoCamera,
                title = stringResource(R.string.settings_capture),
                checked = true,
                onCheckedChange = {},
                enabled = false,
            )
            HorizontalDivider()
            SettingsToggle(
                icon = Icons.Outlined.Folder,
                title = stringResource(R.string.settings_storage),
                checked = true,
                onCheckedChange = {},
                enabled = false,
            )
            HorizontalDivider()
            SettingsToggle(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.settings_notif),
                checked = true,
                onCheckedChange = {},
                enabled = false,
            )
            HorizontalDivider()
            SettingsToggle(
                icon = Icons.Outlined.PlayArrow,
                title = stringResource(R.string.settings_fg),
                checked = true,
                onCheckedChange = {},
                enabled = false,
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SectionHeader(stringResource(R.string.settings_general))
        Spacer(modifier = Modifier.height(8.dp))
        SettingCard {
            SettingsToggle(
                icon = Icons.Outlined.PlayArrow,
                title = stringResource(R.string.settings_start_on_boot),
                checked = startOnBoot,
                onCheckedChange = { startOnBoot = it },
            )
            HorizontalDivider()
            SettingsToggle(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.settings_persistent_notif),
                checked = persistentNotif,
                onCheckedChange = { persistentNotif = it },
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SectionHeader(stringResource(R.string.settings_other))
        Spacer(modifier = Modifier.height(8.dp))
        SettingCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.clearData(onClearData) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = SharedColors.StatusError)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.settings_clear_data), color = SharedColors.StatusError, modifier = Modifier.weight(1f))
                Text(">", color = SharedColors.StatusError)
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAbout() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = SharedColors.ChildPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.settings_about), color = SharedColors.LightTextPrimary, modifier = Modifier.weight(1f))
                Text(">", color = SharedColors.LightTextTertiary)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = SharedColors.LightTextSecondary,
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SharedColors.LightSurface,
        tonalElevation = 1.dp,
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = SharedColors.ChildPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, color = SharedColors.LightTextPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = SharedColors.ChildPrimary),
        )
    }
}
