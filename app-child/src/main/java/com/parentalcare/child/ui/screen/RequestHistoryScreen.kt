package com.parentalcare.child.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalcare.child.R
import com.parentalcare.core.design.theme.SharedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class HistoryItem(
    val timeMillis: Long,
    val isToday: Boolean,
    val sent: Boolean,
    val message: String,
)

private val sampleHistory = listOf(
    HistoryItem(System.currentTimeMillis() - 60_000, true, true, "Screenshot sent"),
    HistoryItem(System.currentTimeMillis() - 3 * 3600_000, true, true, "Screenshot sent"),
    HistoryItem(System.currentTimeMillis() - 26 * 3600_000, false, true, "Screenshot sent"),
    HistoryItem(System.currentTimeMillis() - 30 * 3600_000, false, false, "Cancelled"),
)

@Composable
fun RequestHistoryScreen(onBack: () -> Unit) {
    var filter by remember { mutableStateOf(0) } // 0=all 1=completed 2=cancelled
    val filters = listOf("All", "Completed", "Cancelled")
    val filtered = when (filter) {
        1 -> sampleHistory.filter { it.sent }
        2 -> sampleHistory.filter { !it.sent }
        else -> sampleHistory
    }
    val today = filtered.filter { it.isToday }
    val yesterday = filtered.filter { !it.isToday }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SingleChoiceSegmentedRow(filters, filter, onChange = { filter = it })
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (today.isNotEmpty()) {
                item { SectionLabel("Today") }
                items(today) { HistoryRow(it) }
            }
            if (yesterday.isNotEmpty()) {
                item { SectionLabel("Yesterday") }
                items(yesterday) { HistoryRow(it) }
            }
            item {
                Text(
                    text = stringResource(R.string.history_footer),
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedColors.LightTextTertiary,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun SingleChoiceSegmentedRow(labels: List<String>, selected: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        labels.forEachIndexed { i, label ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (i == selected) SharedColors.ChildPrimary else SharedColors.LightSurface,
                tonalElevation = 1.dp,
                modifier = Modifier.weight(1f),
                onClick = { onChange(i) },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (i == selected) Color.White else SharedColors.LightTextSecondary,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = SharedColors.LightTextSecondary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun HistoryRow(item: HistoryItem) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SharedColors.LightSurface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (item.sent) SharedColors.StatusSuccessBg else SharedColors.StatusErrorBg,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (item.sent) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                    contentDescription = null,
                    tint = if (item.sent) SharedColors.StatusSuccess else SharedColors.StatusError,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.titleSmall,
                    color = SharedColors.LightTextPrimary,
                )
                Text(
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timeMillis)),
                    style = MaterialTheme.typography.labelMedium,
                    color = SharedColors.LightTextSecondary,
                )
            }
        }
    }
}
