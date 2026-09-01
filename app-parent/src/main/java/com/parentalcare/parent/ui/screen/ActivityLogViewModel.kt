package com.parentalcare.parent.ui.screen
import com.parentalcare.core.common.result.userFriendlyMessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.common.model.ActivityEvent
import com.parentalcare.core.data.activity.ActivityLogRepository
import com.parentalcare.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val activityLogRepository: ActivityLogRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _activityEvents = MutableStateFlow<List<ActivityEvent>>(emptyList())
    val activityEvents: StateFlow<List<ActivityEvent>> = _activityEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadActivityLog()
    }

    fun loadActivityLog() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val user = authRepository.currentUser ?: return@launch
            val familyResult = authRepository.getFamilyForUser(user.id)
            
            if (familyResult is com.parentalcare.core.common.result.Result.Success) {
                loadEvents(familyResult.data.familyId)
            } else {
                _isLoading.value = false
                _error.value = "Failed to get family info"
            }
        }
    }

    private fun loadEvents(familyId: String) {
        viewModelScope.launch {
            val result = activityLogRepository.getRecentActivity(familyId, 100)
            if (result is com.parentalcare.core.common.result.Result.Success) {
                _activityEvents.value = result.data
            } else {
                _error.value = (result as com.parentalcare.core.common.result.Result.Failure).error.userFriendlyMessage
            }
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadActivityLog()
    }
}