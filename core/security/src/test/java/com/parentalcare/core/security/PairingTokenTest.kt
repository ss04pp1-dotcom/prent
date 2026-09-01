package com.parentalcare.core.security

import com.parentalcare.core.security.pairing.PairingSerializer
import com.parentalcare.core.security.pairing.PairingToken
import com.parentalcare.core.security.pairing.PairingTokenFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the lifecycle of a pairing token: issue, serialize to QR,
 * deserialize, verify, consume.
 *
 * These tests run on the JVM (no Android instrumentation). They use the
 * androidx Base64 stub provided by `android.jar` in the local unit test
 * classpath, which delegates to the JVM implementation.
 */
class PairingTokenTest {

    private val factory = PairingTokenFactory()
    private val serializer = PairingSerializer()

    @Test
    fun `issued token is not expired and not consumed`() {
        val token = factory.issue(
            familyId = "FAM_1",
            parentUserId = "uid_parent_1",
            parentDisplayName = "Abdul Kader",
            parentEmail = "parent@example.com",
        )
        assertFalse(token.isExpired)
        assertFalse(token.isConsumed)
        assertNull(token.consumedByDeviceId)
    }

    @Test
    fun `serialize then deserialize round-trips the token`() {
        val token = factory.issue(
            familyId = "FAM_1",
            parentUserId = "uid_parent_1",
            parentDisplayName = "Abdul Kader",
            parentEmail = "parent@example.com",
        )
        val encoded = serializer.encode(token)
        assertTrue(encoded.startsWith(PairingSerializer.PREFIX))

        val decoded = serializer.decode(encoded).getOrThrow()
        assertEquals(token, decoded)
    }

    @Test
    fun `verify returns true for matching tokens`() {
        val token = factory.issue(
            familyId = "FAM_1",
            parentUserId = "uid_parent_1",
            parentDisplayName = "Abdul Kader",
            parentEmail = "parent@example.com",
        )
        // Copy of the same fields produces a token that "verifies" against itself.
        val copy = token.copy()
        assertTrue(token.verify(copy))
    }

    @Test
    fun `verify returns false when opaque field differs`() {
        val token = factory.issue(
            familyId = "FAM_1",
            parentUserId = "uid_parent_1",
            parentDisplayName = "Abdul Kader",
            parentEmail = "parent@example.com",
        )
        val tampered = token.copy(opaque = "different")
        assertFalse(token.verify(tampered))
    }

    @Test
    fun `two issued tokens have distinct opaque values`() {
        val a = factory.issue("FAM_1", "u1", "n1", "e1")
        val b = factory.issue("FAM_1", "u1", "n1", "e1")
        assertNotEquals(a.opaque, b.opaque)
        assertNotEquals(a.tokenId, b.tokenId)
        assertNotEquals(a.nonce, b.nonce)
    }

    @Test
    fun `token with past expiresAt is expired`() {
        val past = System.currentTimeMillis() - 10_000
        val token = PairingToken(
            tokenId = "tid",
            opaque = "opaque",
            familyId = "FAM_1",
            parentUserId = "u1",
            parentDisplayName = "Abdul Kader",
            parentEmail = "p@example.com",
            createdAt = past - 60_000,
            expiresAt = past,
            nonce = "0123456789abcdef",
        )
        assertTrue(token.isExpired)
    }
}
