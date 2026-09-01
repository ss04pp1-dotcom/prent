package com.parentalcare.parent.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.common.result.getOrThrow
import com.parentalcare.core.data.device.DeviceRepository
import com.parentalcare.core.data.family.FamilyRepository
import com.parentalcare.core.data.screenshot.ScreenshotRepository
import com.parentalcare.core.data.request.ScreenshotRequestRepository
import com.parentalcare.core.data.model.DeviceDoc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val deviceRepository: DeviceRepository,
    private val screenshotRepository: ScreenshotRepository,
    private val requestRepository: ScreenshotRequestRepository
) : ViewModel() {

    private val _devices = MutableStateFlow<List<DeviceDoc>>(emptyList())
    val devices: StateFlow<List<DeviceDoc>> = _devices.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayScreenshots = MutableStateFlow(0)
    val todayScreenshots: StateFlow<Int> = _todayScreenshots.asStateFlow()

    private val _todayRequests = MutableStateFlow(0)
    val todayRequests: StateFlow<Int> = _todayRequests.asStateFlow()

    fun loadDevices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val family = familyRepository.getOrCreateFamily().getOrThrow()
                val devices = deviceRepository.listDevices(family.familyId).getOrThrow()
                _devices.value = devices
                
                // Get start of today
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfToday = cal.timeInMillis

                launch {
                    screenshotRepository.listenForFamilyScreenshots(family.familyId).collectLatest { shots ->
                        _todayScreenshots.value = shots.count { shot: com.parentalcare.core.data.model.ScreenshotDoc -> (shot.capturedAt ?: 0L) >= startOfToday }
                    }
                }
                
                launch {
                    requestRepository.listenForParentRequests(family.familyId).collectLatest { reqs ->
                        _todayRequests.value = reqs.count { (it.createdAt ?: 0L) >= startOfToday }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to load dashboard data")
                _devices.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
