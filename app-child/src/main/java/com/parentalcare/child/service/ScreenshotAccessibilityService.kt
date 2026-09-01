package com.parentalcare.child.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import androidx.annotation.RequiresApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import com.parentalcare.child.mediaprojection.ScreenCaptureManager
import com.parentalcare.child.app.ChildApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.Executor
import kotlin.coroutines.resume

@RequiresApi(Build.VERSION_CODES.R)
class ScreenshotAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile
        var instance: ScreenshotAccessibilityService? = null
            private set
    }

    private lateinit var screenCaptureManager: ScreenCaptureManager

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as ChildApplication
        screenCaptureManager = app.hiltEntryPoint.screenCaptureManager()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (instance != null && instance != this) {
            Timber.tag("PC.A11ySvc").w("Replacing stale AccessibilityService instance")
        }
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
        }
        serviceInfo = info
        Timber.tag("PC.A11ySvc").i("Accessibility Service connected")
        screenCaptureManager.setAccessibilityServiceReady(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        clearInstance()
        Timber.tag("PC.A11ySvc").i("Accessibility Service disconnected")
        screenCaptureManager.setAccessibilityServiceReady(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearInstance()
        Timber.tag("PC.A11ySvc").i("Accessibility Service destroyed")
    }

    private fun clearInstance() {
        if (instance === this) {
            instance = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun takeSilentScreenshot(): Bitmap? {
        return withTimeoutOrNull(15000L) {
            suspendCancellableCoroutine<Bitmap?> { cont ->
                try {
                    takeScreenshot(
                        Display.DEFAULT_DISPLAY,
                        Executor { command -> Handler(Looper.getMainLooper()).post(command) },
                        object : TakeScreenshotCallback {
                            override fun onSuccess(screenshot: ScreenshotResult) {
                                var hwBitmap: Bitmap? = null
                                try {
                                    hwBitmap = Bitmap.wrapHardwareBuffer(
                                        screenshot.hardwareBuffer,
                                        screenshot.colorSpace
                                    )
                                    val swBitmap = hwBitmap?.copy(Bitmap.Config.ARGB_8888, false)
                                    if (cont.isActive) cont.resume(swBitmap)
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to convert hardware bitmap")
                                    if (cont.isActive) cont.resume(null)
                                } finally {
                                    try {
                                        hwBitmap?.recycle()
                                    } catch(e: Exception){}
                                    try {
                                        screenshot.hardwareBuffer.close()
                                    } catch(e: Exception){}
                                }
                            }

                            override fun onFailure(errorCode: Int) {
                                Timber.e("Accessibility screenshot failed with error code: $errorCode")
                                if (cont.isActive) cont.resume(null)
                            }
                        }
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Exception in takeScreenshot")
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }
}
