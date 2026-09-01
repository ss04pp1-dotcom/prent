package com.parentalcare.child.ui.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.parentalcare.child.mediaprojection.ScreenCaptureManager
import com.parentalcare.child.service.ScreenshotAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ScreenCapturePermViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenCaptureManager: ScreenCaptureManager
) : ViewModel() {

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    fun startMonitoringService(context: Context) {
        com.parentalcare.child.service.MonitoringForegroundService.start(context)
    }
    
    fun isAccessibilityServiceEnabled(): Boolean {
        return ScreenshotAccessibilityService.instance != null
    }
}
