package com.parentalcare.child.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.parentalcare.child.ui.screen.ChildPairingViewModel.PairingState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.parentalcare.child.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun PairingScreen(
    onPaired: () -> Unit,
    onBack: () -> Unit,
    viewModel: ChildPairingViewModel = hiltViewModel()
) {
    val pairingState by viewModel.pairingState.collectAsState()
    var code by remember { mutableStateOf("") }
    
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            code = result.contents
            viewModel.pairWithCode(result.contents)
        }
    }

    LaunchedEffect(pairingState) {
        if (pairingState is PairingState.Success) {
            onPaired()
        }
    }

    if (pairingState is PairingState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Pairing Error") },
            text = { Text((pairingState as PairingState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) { Text("OK") }
            },
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SharedColors.LightBg)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = stringResource(R.string.pair_header),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = SharedColors.LightTextPrimary,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.pair_subheader),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedColors.LightTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // QR viewfinder placeholder
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, SharedColors.ChildPrimary, RoundedCornerShape(24.dp))
                    .clickable {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        options.setPrompt("Scan Parent QR Code")
                        options.setBeepEnabled(false)
                        scanLauncher.launch(options)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        tint = SharedColors.ChildPrimary,
                        modifier = Modifier.size(80.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.labelMedium,
                        color = SharedColors.LightTextSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.pair_or_code),
                style = MaterialTheme.typography.labelMedium,
                color = SharedColors.LightTextSecondary,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = { Text("Enter code", color = SharedColors.LightTextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = SharedColors.LightTextPrimary,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { /* help */ }) {
                Text(stringResource(R.string.pair_help), color = SharedColors.ChildPrimary)
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryCta(text = "Continue", onClick = { viewModel.pairWithCode(code) }, enabled = code.length >= 4)
        }
        
        if (pairingState is PairingState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SharedColors.ChildPrimary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PairingPreview() {
    PairingScreen(onPaired = {}, onBack = {})
}
