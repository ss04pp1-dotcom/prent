package com.parentalcare.child.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.common.result.getOrThrow
import com.parentalcare.core.data.device.DeviceRepository
import com.parentalcare.core.data.model.DeviceDoc
import com.parentalcare.core.data.prefs.ChildPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val childPreferences: ChildPreferences
) : ViewModel() {

    private val _device = MutableStateFlow<DeviceDoc?>(null)
    val device: StateFlow<DeviceDoc?> = _device.asStateFlow()

    init {
        loadDeviceData()
    }

    private fun loadDeviceData() {
        viewModelScope.launch {
            val deviceId = childPreferences.deviceId.first()
            val familyId = childPreferences.familyId.first()
            
            if (deviceId != null && familyId != null) {
                try {
                    val currentDevice = deviceRepository.getCurrentDevice(deviceId, familyId).getOrThrow()
                    _device.value = currentDevice
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load device info")
                }
            }
        }
    }

    fun stopMonitoring() {
        viewModelScope.launch {
            val deviceId = childPreferences.deviceId.first()
            val familyId = childPreferences.familyId.first()
            if (deviceId != null && familyId != null) {
                try {
                    deviceRepository.setMonitoringActive(deviceId, familyId, false).getOrThrow()
                    // Re-load
                    loadDeviceData()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to stop monitoring")
                }
            }
        }
    }
}
