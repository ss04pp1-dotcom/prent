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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ProfileScreen(onBack: () -> Unit) {
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
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SharedColors.ParentPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = SharedColors.ParentPrimary, modifier = Modifier.size(56.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Abdul Kader", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
            Text("parent@email.com", style = MaterialTheme.typography.bodySmall, color = SharedColors.DarkTextSecondary)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                RowMenu(Icons.Outlined.Edit, "Edit Profile", onClick = {})
                Divider(color = SharedColors.DarkBorder)
                RowMenu(Icons.Outlined.Lock, "Change Password", onClick = {})
                Divider(color = SharedColors.DarkBorder)
                RowMenu(Icons.Outlined.Public, "Language", trailing = "English")
                Divider(color = SharedColors.DarkBorder)
                RowMenu(Icons.Outlined.DarkMode, "Theme", trailing = "Dark")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.StatusError.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = SharedColors.StatusError)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Delete Account", color = SharedColors.StatusError, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(">", color = SharedColors.StatusError)
            }
        }
    }
}

@Composable
private fun RowMenu(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailing: String? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SharedColors.ParentPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = SharedColors.DarkTextSecondary, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(">", color = SharedColors.DarkTextTertiary)
    }
}
