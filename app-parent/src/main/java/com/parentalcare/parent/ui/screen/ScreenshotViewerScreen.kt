package com.parentalcare.parent.ui.screen
import android.graphics.Bitmap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.core.design.components.DestructiveCta
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ScreenshotViewerScreen(
    viewModel: ScreenshotViewerViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val bitmap by viewModel.bitmap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.DarkTextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Screenshot", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary)
                Text("Today, 5:42 PM", style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary)
            }
            Icon(Icons.Outlined.Download, contentDescription = null, tint = SharedColors.DarkTextPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Image area
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = SharedColors.ParentPrimary)
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("Failed to load image", color = SharedColors.DarkTextSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Pagination + delete
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("1 / 11", style = MaterialTheme.typography.labelLarge, color = SharedColors.DarkTextSecondary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        DestructiveCta(text = "Delete Permanently", onClick = onDelete)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
