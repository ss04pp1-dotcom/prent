package com.parentalcare.child.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.parentalcare.core.common.result.Result
import com.parentalcare.core.data.model.DeviceDoc
import com.parentalcare.core.data.pairing.PairingRepository
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.security.pairing.PairingSerializer
import com.parentalcare.core.security.pairing.PairingToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChildPairingViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val childPreferences: ChildPreferences,
    private val pairingSerializer: PairingSerializer,
) : ViewModel() {
    sealed interface PairingState {
        data object Loading : PairingState
        data class Success(val device: DeviceDoc) : PairingState
        data class Error(val message: String) : PairingState
        object Idle : PairingState
    }
    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState

    fun pairWithCode(code: String) {
        viewModelScope.launch {
            _pairingState.value = PairingState.Loading
            
            val decodeResult = pairingSerializer.decode(code.trim())
            if (decodeResult is Result.Failure) {
                _pairingState.value = PairingState.Error("Invalid pairing code format: ${decodeResult.error}")
                return@launch
            }
            
            val token = (decodeResult as com.parentalcare.core.common.result.Result.Success<PairingToken>).data
            if (token.isExpired) {
                _pairingState.value = PairingState.Error("Pairing code has expired. Please generate a new one.")
                return@launch
            }
            
            val deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
            val androidVersion = android.os.Build.VERSION.RELEASE
            
            var fcmToken = ""
            try {
                fcmToken = FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                Timber.e(e, "Failed to get FCM token")
            }
            
            val redeemResult = pairingRepository.redeemPairingToken(
                token = token,
                deviceName = deviceName,
                deviceModel = android.os.Build.MODEL,
                androidVersion = androidVersion,
                fcmToken = fcmToken,
            )
            
            if (redeemResult is Result.Failure) {
                _pairingState.value = PairingState.Error("Failed to redeem pairing code: ${redeemResult.error}")
                return@launch
            }
            
            val device = (redeemResult as com.parentalcare.core.common.result.Result.Success<DeviceDoc>).data
            childPreferences.savePairingData(device.deviceId, device.familyId, token.parentEncryptionPublicKey ?: token.parentPublicKey ?: "")
            _pairingState.value = PairingState.Success(device)
        }
    }
    fun dismissError() {
        _pairingState.value = PairingState.Idle
    }
}
