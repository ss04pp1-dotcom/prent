package com.parentalcare.child.mediaprojection

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.parentalcare.child.service.ScreenshotAccessibilityService
import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _isAccessibilityServiceReady = MutableStateFlow(false)
    val isAccessibilityServiceReady: StateFlow<Boolean> = _isAccessibilityServiceReady.asStateFlow()

    fun setAccessibilityServiceReady(ready: Boolean) {
        _isAccessibilityServiceReady.value = ready
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun captureSilently(): Result<Bitmap> = resultOf {
        if (!_isAccessibilityServiceReady.value) {
            throw IllegalStateException("Accessibility service not ready")
        }
        val svc = ScreenshotAccessibilityService.instance
        if (svc != null) {
            Timber.i("Taking screenshot via AccessibilityService")
            val bmp = svc.takeSilentScreenshot()
            if (bmp != null) return@resultOf bmp
        } else {
            Timber.w("AccessibilityService instance is null")
        }
        throw IllegalStateException("Accessibility service not available or capture failed")
    }

    companion object { const val TAG = "PC.ScreenCaptureManager" }
}
