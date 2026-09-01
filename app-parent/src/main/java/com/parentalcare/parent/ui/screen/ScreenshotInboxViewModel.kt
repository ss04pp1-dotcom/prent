package com.parentalcare.parent.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.data.model.ScreenshotDoc
import com.parentalcare.core.data.screenshot.ScreenshotRepository
import com.parentalcare.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScreenshotInboxViewModel @Inject constructor(
    private val screenshotRepository: ScreenshotRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _screenshots = MutableStateFlow<List<ScreenshotDoc>>(emptyList())
    val screenshots: StateFlow<List<ScreenshotDoc>> = _screenshots.asStateFlow()

    init {
        listenToInbox()
    }

    private fun listenToInbox() {
        val user = authRepository.currentUser ?: return
        val familyId = user.id
        viewModelScope.launch {
            try {
                screenshotRepository.listenForFamilyScreenshots(familyId).collect { docs ->
                    _screenshots.value = docs
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to listen for inbox")
            }
        }
    }
}
