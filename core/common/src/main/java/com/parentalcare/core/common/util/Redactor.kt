package com.parentalcare.core.common.util

/**
 * Minimal redaction utility.
 *
 * Logging policy:
 *   - NEVER log: screenshot image bytes, encryption keys, Firebase tokens,
 *     auth secrets, screenshot URLs containing credentials, child PII.
 *
 * Always pass any value that might contain sensitive data through [redact]
 * before sending it to Timber.
 */
object Redactor {

    fun redact(input: String?): String {
        if (input.isNullOrEmpty()) return "<empty>"
        if (input.length <= 4) return "<redacted:${input.length}>"
        return input.take(2) + "…<redacted:${input.length - 4}>"
    }

    fun redact(token: ByteArray?): String =
        if (token == null) "<empty>" else "<bytes:${token.size}>"

    /** Use for IDs safe to log (requestId prefix only). */
    fun idPrefix(id: String?): String =
        if (id.isNullOrEmpty()) "<null>" else id.take(8)
}
