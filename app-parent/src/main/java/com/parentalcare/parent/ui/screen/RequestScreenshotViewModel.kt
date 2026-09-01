package com.parentalcare.parent.ui.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.data.device.DeviceRepository
import com.parentalcare.core.data.model.DeviceDoc
import com.parentalcare.core.data.request.ScreenshotRequestRepository
import com.parentalcare.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RequestScreenshotViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val requestRepository: ScreenshotRequestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])

    private val _device = MutableStateFlow<DeviceDoc?>(null)
    val device: StateFlow<DeviceDoc?> = _device.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    init {
        loadDevice()
    }

    private fun loadDevice() {
        viewModelScope.launch {
            val user = authRepository.currentUser ?: return@launch
            val familyId = user.id // Parent user ID is family ID
            
            try {
                _device.value = deviceRepository.getCurrentDevice(deviceId, familyId).getOrNull()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load device for request")
            }
        }
    }

    fun sendRequest(delaySeconds: Int, onSent: () -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser ?: return@launch
            val familyId = user.id
            _isSending.value = true
            
            try {
                val res = requestRepository.createRequest(familyId, deviceId, delaySeconds)
                if (res.isSuccess) {
                    onSent()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to send request")
            } finally {
                _isSending.value = false
            }
        }
    }
}
