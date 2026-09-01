package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.parent.R
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(), onLogout: () -> Unit, 
    onNotifications: () -> Unit,
    onConnectedDevices: () -> Unit,
    onProfile: () -> Unit,
    onAbout: () -> Unit,
    onPairingQR: () -> Unit,
    onBiometric: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader("App Settings")
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                SettingsRow(Icons.Outlined.PhotoCamera, stringResource(R.string.settings_screenshots), onClick = onPairingQR)
                Divider(color = SharedColors.DarkBorder)
                SettingsRow(Icons.Outlined.Notifications, stringResource(R.string.settings_notifications), onClick = onNotifications)
                Divider(color = SharedColors.DarkBorder)
                SettingsRow(Icons.Outlined.Lock, stringResource(R.string.settings_privacy), onClick = onBiometric)
                Divider(color = SharedColors.DarkBorder)
                SettingsRow(Icons.Outlined.Storage, stringResource(R.string.settings_data), onClick = onConnectedDevices)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader("General")
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                SettingsRow(Icons.Outlined.Help, stringResource(R.string.settings_help), onClick = {})
                Divider(color = SharedColors.DarkBorder)
                SettingsRow(Icons.Outlined.Info, stringResource(R.string.settings_about), onClick = onAbout)
                Divider(color = SharedColors.DarkBorder)
                SettingsRow(Icons.Outlined.Person, "Profile", onClick = onProfile)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.StatusError.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth().clickable { viewModel.logout(onLogout) },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, tint = SharedColors.StatusError)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.settings_logout), color = SharedColors.StatusError, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(">", color = SharedColors.StatusError)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = SharedColors.DarkTextSecondary,
    )
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SharedColors.ParentPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
        Text(">", color = SharedColors.DarkTextTertiary)
    }
}
