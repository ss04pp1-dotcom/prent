package com.parentalcare.child.ui.screen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.child.R
import com.parentalcare.core.design.components.DestructiveCta
import com.parentalcare.core.design.components.StatusBadge
import com.parentalcare.core.design.components.StatusKind
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun MonitoringStatusScreen(
    viewModel: MonitoringViewModel = hiltViewModel(),
    onStopMonitoring: () -> Unit,
    onSeeHistory: () -> Unit,
    onSeeAbout: () -> Unit,
) {
    val device by viewModel.device.collectAsState()
    val requestCount = device?.requestCount ?: 0
    val screenshotCount = device?.screenshotCount ?: 0
    val parentName = "Parent" // We could fetch from FamilyDoc later if needed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Outlined.Menu, contentDescription = "Menu")
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.monitoring_active_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Status card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SharedColors.StatusSuccessBg,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SharedColors.StatusSuccess, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = Icons.Outlined.Shield, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Monitoring is Active",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SharedColors.StatusSuccessDark,
                        )
                        Text(
                            "Your parent can request screenshots from this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SharedColors.StatusSuccessDark.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parent card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.LightSurface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null, tint = SharedColors.ChildPrimary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Parent", style = MaterialTheme.typography.labelMedium, color = SharedColors.LightTextSecondary)
                    Text(parentName, style = MaterialTheme.typography.titleMedium, color = SharedColors.LightTextPrimary, fontWeight = FontWeight.SemiBold)
                }
                StatusBadge(label = "Connected", kind = StatusKind.ACTIVE)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                icon = Icons.Outlined.PhotoLibrary,
                label = "Total Screenshots",
                value = screenshotCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Outlined.History,
                label = "Total Requests",
                value = requestCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                onClick = onSeeHistory,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.History, contentDescription = null, tint = SharedColors.ChildPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("History", color = SharedColors.ChildPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                onClick = onSeeAbout,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = SharedColors.ChildPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About", color = SharedColors.ChildPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DestructiveCta(text = stringResource(R.string.monitoring_active_stop), onClick = {
            viewModel.stopMonitoring()
            onStopMonitoring()
        })
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SharedColors.LightSurface,
        tonalElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SharedColors.ChildPrimaryLight.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SharedColors.ChildPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = SharedColors.LightTextSecondary)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = SharedColors.LightTextPrimary)
        }
    }
}
