package com.parentalcare.child.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parentalcare.child.R
import com.parentalcare.core.design.components.PrimaryCta
import com.parentalcare.core.design.theme.SharedColors

/**
 * Screen 1: Welcome.
 * Light purple background, shield icon, three bullets, CTA "Get Started",
 * footer with Privacy Policy / Terms.
 */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedColors.LightBg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // App brand
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 40.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SharedColors.ChildPrimary, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp),
                )
            }
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.LightTextPrimary,
            )
            Text(
                text = stringResource(R.string.welcome_subheader),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedColors.LightTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Bullets
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            BulletRow(icon = Icons.Outlined.PhotoCamera, text = stringResource(R.string.welcome_bullet_1))
            BulletRow(icon = Icons.Outlined.Notifications, text = stringResource(R.string.welcome_bullet_2))
            BulletRow(icon = Icons.Outlined.Lock, text = stringResource(R.string.welcome_bullet_3))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryCta(text = stringResource(R.string.welcome_cta), onClick = onGetStarted)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_footer),
                style = MaterialTheme.typography.labelSmall,
                color = SharedColors.LightTextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun BulletRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(SharedColors.ChildPrimaryLight.copy(alpha = 0.18f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SharedColors.ChildPrimary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = SharedColors.LightTextPrimary)
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePreview() {
    WelcomeScreen(onGetStarted = {})
}
