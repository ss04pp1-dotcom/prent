package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parentalcare.child.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun PermissionInfoScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        TopBar(title = stringResource(R.string.perm_info_header), onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.perm_info_subheader),
            style = MaterialTheme.typography.bodyMedium,
            color = SharedColors.LightTextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionRow(
                icon = Icons.Outlined.PhotoCamera,
                title = stringResource(R.string.perm_capture_title),
                subtitle = stringResource(R.string.perm_capture_desc),
            )
            PermissionRow(
                icon = Icons.Outlined.Folder,
                title = stringResource(R.string.perm_storage_title),
                subtitle = stringResource(R.string.perm_storage_desc),
            )
            PermissionRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.perm_notif_title),
                subtitle = stringResource(R.string.perm_notif_desc),
            )
            PermissionRow(
                icon = Icons.Outlined.PlayArrow,
                title = stringResource(R.string.perm_fg_title),
                subtitle = stringResource(R.string.perm_fg_desc),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.perm_footer),
            style = MaterialTheme.typography.labelSmall,
            color = SharedColors.LightTextTertiary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        PrimaryCta(text = stringResource(R.string.perm_cta), onClick = onContinue)
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SharedColors.LightSurface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SharedColors.ChildPrimaryLight.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SharedColors.ChildPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = SharedColors.LightTextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SharedColors.LightTextSecondary)
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SharedColors.LightTextPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionInfoPreview() {
    PermissionInfoScreen(onContinue = {}, onBack = {})
}
