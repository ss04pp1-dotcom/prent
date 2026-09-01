package com.parentalcare.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.parentalcare.core.design.theme.ParentalCareParentTheme
import com.parentalcare.parent.nav.ParentRootNav
import dagger.hilt.android.AndroidEntryPoint

/**
 * Parent app entry point.
 *
 * Edge-to-edge is enabled with a dark system bar style that matches the
 * parent app's dark premium theme background (#0F172A).
 *
 * Compose-only — no XML layout, no fragments, no view bindings.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dark = Color(0xFF0F172A)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(dark.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(dark.toArgb()),
        )
        setContent {
            ParentalCareParentTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = dark) {
                    ParentRootNav()
                }
            }
        }
    }
}
