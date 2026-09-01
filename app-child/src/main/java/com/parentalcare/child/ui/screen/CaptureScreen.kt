package com.parentalcare.child.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.child.R
import com.parentalcare.core.design.theme.SharedColors

/**
 * Screen 10: Capturing Screen.
 * Shows a phone + spinner + warning.
 */
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel = hiltViewModel(),
    onCaptured: () -> Unit
) {
    // Auto-advance after 2.5s — simulates the capture pipeline.
    // In production this is driven by MediaProjection callback.
    val isCapturing by viewModel.isCapturing.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.processCapture()
    }

    LaunchedEffect(isCapturing) {
        if (!isCapturing) {
            onCaptured()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SharedColors.LightSurface),
            elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(SharedColors.ChildPrimaryLight.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = null,
                        tint = SharedColors.ChildPrimary,
                        modifier = Modifier.size(80.dp),
                    )
                    CircularProgressIndicator(
                        color = SharedColors.ChildPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(120.dp),
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.capture_header),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SharedColors.LightTextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.capture_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedColors.LightTextSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.capture_subtext),
                    style = MaterialTheme.typography.bodySmall,
                    color = SharedColors.LightTextTertiary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SharedColors.StatusWarningBg,
                ) {
                    Text(
                        text = stringResource(R.string.capture_warn),
                        style = MaterialTheme.typography.labelMedium,
                        color = SharedColors.LightTextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Manual screenshot button
                androidx.compose.material3.OutlinedButton(
                    onClick = { viewModel.manualCapture() },
                    enabled = !isCapturing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = SharedColors.LightSurface,
                        contentColor = SharedColors.ChildPrimary,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = SharedColors.ChildPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Screenshot Manually",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = SharedColors.ChildPrimary,
                        )
                    }
                }
            }
        }
    }
}