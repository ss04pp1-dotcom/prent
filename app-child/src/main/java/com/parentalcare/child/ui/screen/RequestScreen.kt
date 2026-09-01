package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.child.R
import com.parentalcare.core.design.components.DestructiveCta
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RequestScreen(
    viewModel: RequestViewModel = hiltViewModel(),
    onTake: () -> Unit,
    onCancel: () -> Unit,
) {
    val activeReq by viewModel.activeRequest.collectAsState()
    activeReq?.let { req ->
        val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val timeString = timeFormat.format(Date(req.createdAt ?: System.currentTimeMillis()))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SharedColors.LightBg)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.request_header),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SharedColors.LightSurface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = SharedColors.ChildPrimary,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Parent",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SharedColors.LightTextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "has requested a screenshot.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedColors.LightTextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelMedium,
                        color = SharedColors.LightTextTertiary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SharedColors.StatusWarning.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = stringResource(R.string.request_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedColors.LightTextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryCta(text = stringResource(R.string.request_take), onClick = {
                viewModel.acceptRequest()
                onTake()
            })
            Spacer(modifier = Modifier.height(8.dp))
            DestructiveCta(text = stringResource(R.string.request_cancel), onClick = {
                viewModel.cancelRequest()
                onCancel()
            })
        }
    }
}
