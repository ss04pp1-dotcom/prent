package com.parentalcare.parent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalcare.core.design.theme.SharedColors
import java.util.Calendar

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val today = cal.get(Calendar.DAY_OF_MONTH)
    val cal2 = Calendar.getInstance()
    val daysInMonth = cal2.apply { set(Calendar.DAY_OF_MONTH, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal2.apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK)
    val activeDays = setOf(25, 24, 22, 20)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.DarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SharedColors.DarkTextPrimary)
            }
            Text("Calendar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SharedColors.DarkTextPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = {}) { Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = null, tint = SharedColors.DarkTextPrimary) }
            Text("August 2024", color = SharedColors.DarkTextPrimary, style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = {}) { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = SharedColors.DarkTextPrimary) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                        Text(
                            d,
                            style = MaterialTheme.typography.labelSmall,
                            color = SharedColors.DarkTextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                val totalCells = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7
                val cells = (1..totalCells).map { idx ->
                    val day = idx - (firstDayOfWeek - 1)
                    if (day in 1..daysInMonth) day else 0
                }
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (day != 0) {
                                    val isToday = day == today
                                    val isActive = day in activeDays
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (isToday) SharedColors.ParentPrimary else Color.Transparent,
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            day.toString(),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (isActive || isToday) FontWeight.Bold else FontWeight.Normal),
                                            color = if (isToday) Color.White else SharedColors.DarkTextPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Summary
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SharedColors.DarkSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SummaryRow("Aug 26, 2024", "5 screenshots")
                Divider(color = SharedColors.DarkBorder)
                SummaryRow("Aug 24, 2024", "8 screenshots")
            }
        }
    }
}

@Composable
private fun SummaryRow(date: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(date, color = SharedColors.DarkTextPrimary, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(count, color = SharedColors.ParentPrimary, style = MaterialTheme.typography.labelLarge)
    }
}
