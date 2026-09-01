package com.parentalcare.child

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
import androidx.lifecycle.lifecycleScope
import com.parentalcare.child.nav.ChildRootNav
import com.parentalcare.child.pipeline.IncomingRequestHandler
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.data.request.ScreenshotRequestRepository
import com.parentalcare.core.design.theme.ParentalCareChildTheme
import com.parentalcare.core.notifications.SafePayload
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var childPreferences: ChildPreferences
    @Inject lateinit var requestRepository: ScreenshotRequestRepository
    @Inject lateinit var incomingRequestHandler: IncomingRequestHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        listenForRequestsFallback()
        
        val light = Color(0xFFF5F6FA)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(light.toArgb(), light.toArgb()),
            navigationBarStyle = SystemBarStyle.light(light.toArgb(), light.toArgb()),
        )
        setContent {
            ParentalCareChildTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = light) {
                    ChildRootNav()
                }
            }
        }
    }

    private fun listenForRequestsFallback() {
        lifecycleScope.launch {
            val deviceId = childPreferences.deviceId.first() ?: return@launch
            val familyId = childPreferences.familyId.first() ?: return@launch
            
            requestRepository.listenForDeviceRequests(familyId, deviceId).collect { req ->
                if (req != null && req.status == "REQUESTED") {
                    val payload = SafePayload(
                        type = "SCREENSHOT_REQUEST",
                        requestId = req.requestId,
                        familyId = req.familyId
                    )
                    incomingRequestHandler.handleIncomingRequest(payload)
                }
            }
        }
    }
}
