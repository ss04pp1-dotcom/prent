package com.parentalcare.parent.ui.screen

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ScreenshotViewerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val screenshotRepository: ScreenshotRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val screenshotId: String = checkNotNull(savedStateHandle["screenshotId"])

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadScreenshot()
    }

    private fun loadScreenshot() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val familyId = authRepository.currentUser?.id ?: return@launch
                val result = screenshotRepository.downloadAndDecrypt(familyId, screenshotId)
                _bitmap.value = result.getOrNull()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load screenshot")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
