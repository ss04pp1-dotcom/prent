package com.parentalcare.parent.ui.screen
import com.parentalcare.core.common.result.userFriendlyMessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.common.result.Result
import com.parentalcare.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepository.signInParent(email, pass)
            _isLoading.value = false
            if (result is Result.Failure) {
                _error.value = result.error.userFriendlyMessage
            } else {
                onSuccess()
            }
        }
    }

    fun signUp(email: String, pass: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepository.signUpParent(email, pass, displayName = "")
            _isLoading.value = false
            if (result is Result.Failure) {
                _error.value = result.error.userFriendlyMessage
            } else {
                onSuccess()
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            _isLoading.value = false
            if (result is Result.Failure) {
                _error.value = result.error.userFriendlyMessage
            } else {
                onSuccess()
            }
        }
    }

    fun setError(msg: String) {
        _error.value = msg
    }

    fun dismissError() {
        _error.value = null
    }
}
