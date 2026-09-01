package com.parentalcare.core.security

import com.parentalcare.core.common.util.SecureTokenGenerator
import com.parentalcare.core.security.pairing.NonceRegistry
import com.parentalcare.core.security.pairing.ScreenshotRequest
import com.parentalcare.core.security.pairing.ScreenshotRequestValidator
import com.parentalcare.core.security.pairing.TestNonceRegistry
import com.parentalcare.core.security.pairing.freshNonce
import com.parentalcare.core.security.pairing.sha256Hex
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for cross-module utilities consumed by [PairingTokenFactory],
 * [ScreenshotRequestValidator], and [NonceRegistry].
 *
 * These run on the JVM (no Robolectric, no Android instrumentation) so they
 * exercise the pure-Kotlin code paths only.
 */
class CommonUtilsTest {

    @Test
    fun `SecureTokenGenerator pairing codes are 8 chars and use safe alphabet`() {
        repeat(50) {
            val code = SecureTokenGenerator.generatePairingCode()
            assertEquals(8, code.length)
            assertTrue(code.all { it.isLetterOrDigit() })
            assertFalse(code.any { it in setOf('0', '1', 'I', 'O') })
        }
    }

    @Test
    fun `two consecutive tokens differ`() {
        val a = SecureTokenGenerator.generateOpaqueToken()
        val b = SecureTokenGenerator.generateOpaqueToken()
        assertNotEquals(a, b)
    }

    @Test
    fun `nonce registry rejects replays within TTL`() {
        val registry = TestNonceRegistry()
        val n = freshNonce()
        assertTrue(registry.isFresh(n, now = 1_000))
        assertFalse(registry.isFresh(n, now = 1_500)) // < 1h
    }

    @Test
    fun `request validator rejects cross-family request`() {
        val req = ScreenshotRequest(
            requestId = "r1",
            familyId = "FAM_A",
            parentUserId = "uid1",
            childDeviceId = "dev1",
            createdAt = 1_000,
            expiresAt = 5_000,
            nonce = "0123456789abcdef",
            status = "REQUESTED",
        )
        assertFalse(ScreenshotRequestValidator.isValid(req, expectedFamilyId = "FAM_B", expectedDeviceId = "dev1", now = 2_000))
    }

    @Test
    fun `request validator rejects expired request`() {
        val req = ScreenshotRequest(
            requestId = "r1",
            familyId = "FAM_A",
            parentUserId = "uid1",
            childDeviceId = "dev1",
            createdAt = 1_000,
            expiresAt = 5_000,
            nonce = "0123456789abcdef",
            status = "REQUESTED",
        )
        assertFalse(ScreenshotRequestValidator.isValid(req, "FAM_A", "dev1", now = 10_000))
    }

    @Test
    fun `request validator rejects weak nonce`() {
        val req = ScreenshotRequest(
            requestId = "r1",
            familyId = "FAM_A",
            parentUserId = "uid1",
            childDeviceId = "dev1",
            createdAt = 1_000,
            expiresAt = 5_000,
            nonce = "short",
            status = "REQUESTED",
        )
        assertFalse(ScreenshotRequestValidator.isValid(req, "FAM_A", "dev1", now = 2_000))
    }

    @Test
    fun `sha256Hex returns 64-char hex string`() {
        val out = sha256Hex("hello")
        assertEquals(64, out.length)
        assertTrue(out.all { it in "0123456789abcdef" })
    }
}
