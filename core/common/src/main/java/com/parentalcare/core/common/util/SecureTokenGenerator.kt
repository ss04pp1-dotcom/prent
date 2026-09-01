package com.parentalcare.core.common.util

import java.security.SecureRandom

/**
 * Cryptographically-secure random generators used for pairing tokens and
 * screenshot request nonces. Uses [SecureRandom] — never falls back to
 * predictable [Random].
 *
 * Output format is URL-safe base32 (no padding) so tokens stay human-readable
 * when shown as a pairing code.
 */
object SecureTokenGenerator {

    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no I/O/0/1
    private val random: SecureRandom = SecureRandom()

    /** 8-char human-typable pairing code (e.g. "ABCD2345"). */
    fun generatePairingCode(): String =
        generate(8)

    /** 256-bit URL-safe opaque token (32 base32 chars). */
    fun generateOpaqueToken(): String =
        generate(32)

    /** 128-bit nonce for screenshot requests (16 base32 chars). */
    fun generateNonce(): String =
        generate(16)

    /** Raw bytes from [SecureRandom]. */
    fun nextBytes(size: Int): ByteArray {
        val out = ByteArray(size)
        random.nextBytes(out)
        return out
    }

    private fun generate(length: Int): String {
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)])
        }
        return sb.toString()
    }
}
