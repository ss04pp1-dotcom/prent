package com.parentalcare.parent.ui.screen

import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.parent.R
import com.parentalcare.core.common.util.BiometricAuthManager
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun BiometricSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val biometricManager = remember { BiometricAuthManager(context as androidx.fragment.app.FragmentActivity) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var canAuthenticate by remember { mutableStateOf(false) }
    val isEnabled by viewModel.biometricEnabled.collectAsState()
    var isAuthenticating by remember { mutableStateOf(false) }

    // Check biometric availability on screen enter
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val result = biometricManager.canAuthenticate()
        canAuthenticate = result == 0
    }

    fun enableBiometric() {
        isAuthenticating = true
        coroutineScope.launch {
            val result = biometricManager.authenticate("Unlock Parental Care with biometrics")
            if (result.isSuccess) {
                viewModel.setBiometricEnabled(true)
            } else {
                // Show error
            }
            isAuthenticating = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // Back button + title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.DarkTextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_privacy),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (!canAuthenticate) {
            // Biometric not available
            Card(
                colors = CardDefaults.cardColors(containerColor = SharedColors.DarkSurface),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        tint = SharedColors.DarkTextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(align = androidx.compose.ui.Alignment.Center)
                            .padding(vertical = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.biometric_not_available),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = SharedColors.DarkTextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.biometric_not_available_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedColors.DarkTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            // Biometric available - show toggle
            Card(
                colors = CardDefaults.cardColors(containerColor = SharedColors.DarkSurface),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = SharedColors.ParentPrimary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.biometric_lock_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = SharedColors.DarkTextPrimary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.biometric_lock_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SharedColors.DarkTextSecondary,
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = isEnabled,
                            onCheckedChange = { newValue ->
                                if (newValue) {
                                    enableBiometric()
                                } else {
                                    viewModel.setBiometricEnabled(false)
                                }
                            },
                            enabled = !isAuthenticating,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = SharedColors.ParentPrimary,
                                uncheckedThumbColor = SharedColors.DarkTextSecondary,
                                checkedTrackColor = SharedColors.ParentPrimary.copy(alpha = 0.3f),
                                uncheckedTrackColor = SharedColors.DarkBorder,
                            ),
                        )
                    }
                    if (isAuthenticating) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = SharedColors.ParentPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Authenticating...", style = MaterialTheme.typography.bodyMedium, color = SharedColors.DarkTextSecondary)
                        }
                    }
                }
            }
        }
    }


}