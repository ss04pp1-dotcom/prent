package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.parentalcare.core.design.theme.SharedColors
import kotlinx.coroutines.delay

@Composable
fun ScreenCapturePermissionScreen(
    viewModel: ScreenCapturePermViewModel = hiltViewModel(),
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    
    LaunchedEffect(isChecking) {
        if (isChecking) {
            while (true) {
                if (viewModel.isAccessibilityServiceEnabled()) {
                    onContinue()
                    break
                }
                delay(1000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.LightTextPrimary)
            }
            Text(
                text = "Ultimate Screen Capture",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "To enable completely silent and instant screenshots without any pop-ups, Parental Care needs Accessibility permission.",
            style = MaterialTheme.typography.bodyMedium,
            color = SharedColors.LightTextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.LightSurface,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "How to enable:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SharedColors.LightTextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Tap 'Open Settings'\n2. Find 'Parental Care' under Downloaded Apps\n3. Turn on the switch\n4. Return to this app",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedColors.LightTextSecondary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onBack) { Text("Cancel", color = SharedColors.LightTextSecondary) }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { 
                            context.startActivity(viewModel.getAccessibilitySettingsIntent())
                            isChecking = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SharedColors.ChildPrimary),
                    ) { Text("Open Settings") }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Test screenshot button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.ChildPrimaryLight.copy(alpha = 0.15f),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Test Screenshot",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SharedColors.LightTextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "After enabling Accessibility, verify it works by taking a test screenshot",
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedColors.LightTextSecondary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onBack) { Text("Skip", color = SharedColors.LightTextSecondary) }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { 
                            viewModel.startMonitoringService(context)
                            onContinue()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SharedColors.ChildPrimary),
                    ) { 
                        Row(horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Monitoring")
                        }
                    }
                }
            }
        }
    }
}
