package com.parentalcare.parent.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.data.auth.AuthRepository
import com.parentalcare.core.data.family.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    init {
        loadBiometricSetting()
    }

    private fun loadBiometricSetting() {
        viewModelScope.launch {
            val familyResult = familyRepository.getOrCreateFamily()
            if (familyResult is com.parentalcare.core.common.result.Result.Success) {
                _biometricEnabled.value = familyResult.data.biometricLockEnabled
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val familyResult = familyRepository.getOrCreateFamily()
            if (familyResult is com.parentalcare.core.common.result.Result.Success) {
                val family = familyResult.data.copy(biometricLockEnabled = enabled)
                val updateResult = familyRepository.updateFamily(family)
                if (updateResult is com.parentalcare.core.common.result.Result.Success) {
                    _biometricEnabled.value = enabled
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSuccess()
        }
    }
}
