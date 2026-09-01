package com.parentalcare.child.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildSettingsViewModel @Inject constructor(
    private val childPreferences: ChildPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {
    fun clearData(onCleared: () -> Unit) {
        viewModelScope.launch {
            childPreferences.clear()
            authRepository.signOut()
            onCleared()
        }
    }
}
