package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Brand
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SharedColors.LightSurface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Parental Care",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SharedColors.ChildPrimary,
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedColors.LightTextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.LightSurface,
            tonalElevation = 1.dp,
        ) {
            Column {
                AboutRow(Icons.Outlined.Info, "How it works")
                Divider()
                AboutRow(Icons.Outlined.Lock, "Privacy Policy")
                Divider()
                AboutRow(Icons.Outlined.Description, "Terms of Service")
                Divider()
                AboutRow(Icons.Outlined.Build, "Open Source Licenses")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "© 2024 Parental Care. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = SharedColors.LightTextTertiary,
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun AboutRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = SharedColors.ChildPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = SharedColors.LightTextPrimary, modifier = Modifier.weight(1f))
        Text(">", color = SharedColors.LightTextTertiary)
    }
}
