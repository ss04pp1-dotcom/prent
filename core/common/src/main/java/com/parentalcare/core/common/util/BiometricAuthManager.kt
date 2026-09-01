package com.parentalcare.core.common.util

import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Biometric authentication manager for the Parent App.
 * Handles fingerprint/face authentication using AndroidX Biometric API.
 */
class BiometricAuthManager(private val activity: FragmentActivity) {

    private var biometricPrompt: BiometricPrompt? = null
    private var pendingCallback: kotlinx.coroutines.CancellableContinuation<Result<Unit>>? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    pendingCallback?.resumeWithException(BiometricException(errString.toString(), errorCode))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    pendingCallback?.resume(Result.success(Unit))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    pendingCallback?.resumeWithException(BiometricException("Authentication failed", BiometricPrompt.ERROR_HW_UNAVAILABLE))
                }
            })
        }
    }

    /** Checks if biometric hardware is available and enrolled. */
    fun canAuthenticate(): Int {
        val manager = BiometricManager.from(activity)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            manager.canAuthenticate()
        }
    }

    /** Prompts user for biometric authentication. Returns Result on success, throws on failure/cancel. */
    suspend fun authenticate(reason: String = "Authenticate to access Parental Care"): Result<Unit> = suspendCancellableCoroutine { cont ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cont.resumeWithException(BiometricException("Biometric authentication requires Android 9+"))
            return@suspendCancellableCoroutine
        }

        val canAuth = canAuthenticate()
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            cont.resumeWithException(BiometricException("Biometric not available: ${canAuth}"))
            return@suspendCancellableCoroutine
        }

        pendingCallback = cont
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Parental Care")
            .setSubtitle(reason)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
            
        biometricPrompt?.authenticate(promptInfo)
        
        cont.invokeOnCancellation {
            biometricPrompt?.cancelAuthentication()
            pendingCallback = null
        }
    }

    /** Cancels any pending authentication prompt. */
    fun cancel() {
        biometricPrompt?.cancelAuthentication()
        pendingCallback?.resumeWithException(BiometricException("Cancelled"))
        pendingCallback = null
    }

    class BiometricException(message: String, val errorCode: Int = -1) : Exception(message)
}
