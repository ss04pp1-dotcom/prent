package com.parentalcare.parent.ui.screen
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors
import com.parentalcare.parent.qr.QrCodeGenerator

/**
 * Parent-side QR pairing screen. Generates a one-time pairing token via
 * [com.parentalcare.parent.pairing.PairingIssuer], then renders the encoded
 * payload as a QR code.
 *
 * Token TTL is 2 minutes. UI shows a countdown.
 *
 * TODO: replace the demo payload with a Hilt-injected ViewModel that calls
 * PairingIssuer.issueForQr(familyId, parentDisplayName, parentEmail) on
 * first composition and on regenerate.
 */
@Composable
fun PairingQRScreen(onBack: () -> Unit, viewModel: PairingQRViewModel = hiltViewModel()) {
    val qrPayload by viewModel.qrPayload.collectAsState()
    var remainingSeconds by remember { mutableStateOf(120L) }

    // Generate on first composition.
    LaunchedEffect(Unit) {
        // TODO: in production, call PairingIssuer via Hilt-injected ViewModel.
        // For demo: synthesize a fake payload so the QR renders.
        viewModel.generatePayload()
    }

    // Countdown.
    LaunchedEffect(qrPayload) {
        if (qrPayload == null) return@LaunchedEffect
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            remainingSeconds -= 1
        }
        // Auto-regenerate when expired.
        viewModel.generatePayload()
        remainingSeconds = 120
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.DarkTextPrimary)
            }
            Text("Add Child Device", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Scan this QR code on your child's device to pair it.",
            style = MaterialTheme.typography.bodyMedium,
            color = SharedColors.DarkTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.size(280.dp).padding(16.dp),
            ) {
                if (qrPayload != null) {
                    val bmp = remember(qrPayload) { QrCodeGenerator().generate(qrPayload!!, 720) }
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Pairing QR code",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.QrCode, contentDescription = null, tint = SharedColors.DarkTextTertiary, modifier = Modifier.size(80.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Countdown badge
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Token expires in", style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary)
                Spacer(modifier = Modifier.weight(1f))
                Text("$remainingSeconds s", style = MaterialTheme.typography.titleSmall, color = if (remainingSeconds < 30) SharedColors.StatusError else SharedColors.ParentPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryCta(text = "Regenerate", onClick = {
            viewModel.generatePayload()
            remainingSeconds = 120
        })
    }
}
