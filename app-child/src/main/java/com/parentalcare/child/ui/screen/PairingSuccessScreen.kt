package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.parentalcare.child.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.components.StatusBadge
import com.parentalcare.core.design.components.StatusKind
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun PairingSuccessScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 56.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
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
            Text(
                text = stringResource(R.string.pair_success_header),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.pair_success_subheader),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedColors.LightTextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SharedColors.LightSurface),
                elevation = CardDefaults.cardElevation(1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text("Parent", style = MaterialTheme.typography.labelMedium, color = SharedColors.LightTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = SharedColors.ChildPrimary,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Abdul Kader", style = MaterialTheme.typography.titleMedium, color = SharedColors.LightTextPrimary)
                            Text("parent@email.com", style = MaterialTheme.typography.bodySmall, color = SharedColors.LightTextSecondary)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        StatusBadge(label = "Connected", kind = StatusKind.ACTIVE)
                    }
                }
            }
        }
        PrimaryCta(text = stringResource(R.string.pair_success_done), onClick = onDone)
    }
}

@Preview(showBackground = true)
@Composable
private fun PairingSuccessPreview() {
    PairingSuccessScreen(onDone = {})
}
