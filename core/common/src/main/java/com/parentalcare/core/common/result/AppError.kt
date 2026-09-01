package com.parentalcare.core.common.result

/**
 * Typed error hierarchy used by [Result.Failure].
 *
 * Each branch maps to a specific user-visible error state in the UI.
 * Sensitive data (tokens, screenshot URLs, etc.) is intentionally NOT
 * included here to prevent accidental logging.
 */
sealed class AppError {
    /** Network unavailable / Firebase unreachable. */
    data object Network : AppError()

    /** Firebase Auth session expired / user must re-auth. */
    data class AuthExpired(val message: String? = null) : AppError()

    /** Caller is authenticated but not authorized for this resource. */
    data object Unauthorized : AppError()

    /** Resource not found (e.g. request expired / device unpaired). */
    data object NotFound : AppError()

    /** Validation failed (e.g. invalid pairing code format). */
    data class Validation(val field: String) : AppError()

    /** Screenshot capture pipeline failed. */
    data object CaptureFailed : AppError()

    /** Secure upload failed. */
    data object UploadFailed : AppError()

    /** MediaProjection permission revoked by user or OS. */
    data object MediaProjectionRevoked : AppError()

    /** Storage quota exhausted. */
    data object StorageFull : AppError()

    /** Request nonce replay detected — reject silently. */
    data object ReplayDetected : AppError()

    /** Device has been unpaired from parent account. */
    data object DeviceUnpaired : AppError()

    /** Catch-all for unknown internal failures. */
    data class Unknown(val category: String, val message: String? = null) : AppError()

    


    companion object {
        fun from(throwable: Throwable): AppError {
            // Redact the message — never include raw stack traces in
            // production-visible error objects. Log the original via
            // Timber at the call site if needed.
            val name = throwable::class.simpleName ?: "Throwable"
            return when {
                name.contains("Network", ignoreCase = true) ||
                    name.contains("IOException", ignoreCase = true) -> Network

                
                    name.contains("SessionExpired", ignoreCase = true) || name.contains("AuthRestException", ignoreCase = true) -> AuthExpired(throwable.message)

                name.contains("Unauthorized", ignoreCase = true) ||
                    name.contains("PermissionDenied", ignoreCase = true) -> Unauthorized

                name.contains("NotFound", ignoreCase = true) ||
                name.contains("ExpiredException", ignoreCase = true) -> NotFound

                else -> Unknown(category = name, message = throwable.message)
            }
        }
    }
}

val AppError.userFriendlyMessage: String
    get() = when(this) {
        is AppError.Network -> "Network error. Please check your connection."
        is AppError.AuthExpired -> message ?: "Authentication error. Please check your credentials."
        is AppError.Unauthorized -> "You are not authorized to perform this action."
        is AppError.NotFound -> "The requested resource was not found."
        is AppError.Validation -> "Validation failed for field: $field"
        is AppError.CaptureFailed -> "Failed to capture screenshot."
        is AppError.UploadFailed -> "Failed to upload screenshot."
        is AppError.MediaProjectionRevoked -> "Screen recording permission was revoked."
        is AppError.StorageFull -> "Storage is full."
        is AppError.ReplayDetected -> "Security error: Replay detected."
        is AppError.DeviceUnpaired -> "This device is no longer paired."
        is AppError.Unknown -> message ?: "An unknown error occurred ($category)."
    }
