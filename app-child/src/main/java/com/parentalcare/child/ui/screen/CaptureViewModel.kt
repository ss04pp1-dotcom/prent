package com.parentalcare.child.ui.screen

import com.parentalcare.core.common.result.userFriendlyMessage
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.child.mediaprojection.ScreenCaptureManager
import com.parentalcare.child.pipeline.IncomingRequestHandler
import com.parentalcare.core.data.request.ScreenshotRequestRepository
import com.parentalcare.core.data.screenshot.ScreenshotRepository
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.common.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenCaptureManager: ScreenCaptureManager,
    private val incomingRequestHandler: IncomingRequestHandler,
    private val requestRepository: ScreenshotRequestRepository,
    private val screenshotRepository: ScreenshotRepository,
    private val childPreferences: ChildPreferences
) : ViewModel() {
    private val _isCapturing = MutableStateFlow(true)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()
    private val _isServiceReady = MutableStateFlow(false)
    val isServiceReady: StateFlow<Boolean> = _isServiceReady.asStateFlow()

    init {
        observeServiceReadiness()
    }

    private fun observeServiceReadiness() {
        viewModelScope.launch {
            screenCaptureManager.isAccessibilityServiceReady
                .collect { ready ->
                    _isServiceReady.value = ready
                }
        }
    }

    fun processCapture() {
        viewModelScope.launch {
            try {
                // Wait for AccessibilityService to be ready via the flow
                _isServiceReady.first { it }

                val req = incomingRequestHandler.active.value
                if (req == null) {
                    _isCapturing.value = false
                    return@launch
                }

                val parentPublicKey = childPreferences.parentPublicKey.first()
                if (parentPublicKey == null) {
                    Timber.e("Parent public key not found")
                    requestRepository.updateStatus(req, "FAILED", "Pairing missing public key")
                    incomingRequestHandler.handleCancelled(com.parentalcare.core.notifications.SafePayload(type="CANCEL"))
                    _isCapturing.value = false
                    return@launch
                }

                requestRepository.updateStatus(req, "PROCESSING")

                val result = screenCaptureManager.captureSilently()
                if (result is Result.Success) {
                    val bitmap = result.data
                    val uploadResult = screenshotRepository.uploadCaptured(bitmap, req, parentPublicKey)
                    
                    if (uploadResult is Result.Success) {
                        requestRepository.updateStatus(req, "UPLOADED")
                    } else {
                        val err = (uploadResult as Result.Failure).error.userFriendlyMessage
                        requestRepository.updateStatus(req, "FAILED", err)
                    }

                    incomingRequestHandler.handleCancelled(com.parentalcare.core.notifications.SafePayload(type="CANCEL"))
                    _isCapturing.value = false
                } else if (result is Result.Failure) {
                    Timber.e("Failed to capture silently: ${result.error}")
                    requestRepository.updateStatus(req, "FAILED", result.error.userFriendlyMessage)
                    incomingRequestHandler.handleCancelled(com.parentalcare.core.notifications.SafePayload(type="CANCEL"))
                    _isCapturing.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Capture failed")
                _isCapturing.value = false
            }
        }
    }

    fun manualCapture() {
        processCapture()
    }
}
