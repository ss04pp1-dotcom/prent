package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun AboutScreen(onBack: () -> Unit) {
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
            Text("About", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Parental Care", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = SharedColors.ParentPrimary)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = SharedColors.DarkTextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                AboutRow(Icons.Outlined.Info, "How it works")
                Divider(color = SharedColors.DarkBorder)
                AboutRow(Icons.Outlined.Lock, "Privacy Policy")
                Divider(color = SharedColors.DarkBorder)
                AboutRow(Icons.Outlined.Description, "Terms of Service")
                Divider(color = SharedColors.DarkBorder)
                AboutRow(Icons.Outlined.Build, "Open Source Licenses")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "© 2024 Parental Care. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = SharedColors.DarkTextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun AboutRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SharedColors.ParentPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
        Text(">", color = SharedColors.DarkTextTertiary)
    }
}
