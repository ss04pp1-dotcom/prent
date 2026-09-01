package com.parentalcare.parent.ui.screen

import com.parentalcare.core.common.result.getOrThrow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.core.data.auth.AuthRepository
import com.parentalcare.core.data.family.FamilyRepository
import com.parentalcare.core.data.pairing.PairingRepository
import com.parentalcare.core.security.pairing.PairingSerializer
import com.parentalcare.core.security.keystore.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PairingQRViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository,
    private val pairingRepository: PairingRepository,
    private val pairingSerializer: PairingSerializer,
    private val keystoreManager: KeystoreManager
) : ViewModel() {

    private val _qrPayload = MutableStateFlow<String?>(null)
    val qrPayload: StateFlow<String?> = _qrPayload.asStateFlow()

    fun generatePayload() {
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser
                if (user == null) {
                    Timber.e("User not authenticated")
                    return@launch
                }
                
                val family = familyRepository.getOrCreateFamily().getOrThrow()
                
                val displayName = "Parent"
                val email = user.email ?: ""
                
                val token = pairingRepository.issuePairingToken(
                    familyId = family.familyId,
                    parentDisplayName = displayName,
                    parentEmail = email,
                    parentPublicKey = keystoreManager.getParentPublicKeyBase64()
                ).getOrThrow()
                
                _qrPayload.value = pairingSerializer.encode(token)
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate pairing payload")
                _qrPayload.value = null
            }
        }
    }
}