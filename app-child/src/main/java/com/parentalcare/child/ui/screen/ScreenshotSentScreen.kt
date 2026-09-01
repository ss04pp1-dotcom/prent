package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalcare.child.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ScreenshotSentScreen(onOk: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SharedColors.StatusSuccessBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = SharedColors.StatusSuccess,
                    modifier = Modifier.size(56.dp),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.sent_header),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.sent_subtext),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedColors.LightTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SharedColors.LightSurface),
                elevation = CardDefaults.cardElevation(1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Time", style = MaterialTheme.typography.labelMedium, color = SharedColors.LightTextSecondary)
                    Text("Today, 5:42 PM", style = MaterialTheme.typography.bodyMedium, color = SharedColors.LightTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status", style = MaterialTheme.typography.labelMedium, color = SharedColors.LightTextSecondary)
                    Text("Sent Successfully", style = MaterialTheme.typography.bodyMedium, color = SharedColors.StatusSuccess, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.5f))
        PrimaryCta(text = stringResource(R.string.sent_ok), onClick = onOk)
    }
}
