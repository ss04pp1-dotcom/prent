package com.parentalcare.core.security.pairing

import com.parentalcare.core.common.result.Result
import com.parentalcare.core.security.pairing.PairingSerializer
import com.parentalcare.core.security.pairing.PairingToken
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

/**
 * Tests for the pairing flow including serialization and token verification.
 */
class PairingFlowTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = PairingSerializer()

    @Test
    fun testPairingTokenEncodeDecode() {
        val token = PairingToken(
            tokenId = "tok_123",
            opaque = "opaque_456",
            familyId = "fam_789",
            parentUserId = "user_123",
            parentDisplayName = "John Doe",
            parentEmail = "john@example.com",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 120000,
            nonce = "nonce_abc",
        )

        val encoded = serializer.encode(token)
        val decoded = serializer.decode(encoded)

        Assert.assertTrue("Decode should succeed", decoded.isSuccess)
        val decodedToken = decoded.getOrNull()!!
        
        Assert.assertEquals(token.tokenId, decodedToken.tokenId)
        Assert.assertEquals(token.opaque, decodedToken.opaque)
        Assert.assertEquals(token.familyId, decodedToken.familyId)
        Assert.assertEquals(token.parentUserId, decodedToken.parentUserId)
        Assert.assertEquals(token.parentDisplayName, decodedToken.parentDisplayName)
        Assert.assertEquals(token.parentEmail, decodedToken.parentEmail)
        Assert.assertEquals(token.nonce, decodedToken.nonce)
    }

    @Test
    fun testPairingTokenRejectsInvalidPrefix() {
        val result = serializer.decode("INVALID_PREFIX")
        Assert.assertTrue("Should fail with invalid prefix", result.isFailure)
    }

    @Test
    fun testPairingTokenRejectsExpired() {
        val token = PairingToken(
            tokenId = "tok_123",
            opaque = "opaque_456",
            familyId = "fam_789",
            parentUserId = "user_123",
            parentDisplayName = "John Doe",
            parentEmail = "john@example.com",
            createdAt = System.currentTimeMillis() - 300000,
            expiresAt = System.currentTimeMillis() - 60000,
            nonce = "nonce_abc",
        )

        val encoded = serializer.encode(token)
        val decoded = serializer.decode(encoded)

        Assert.assertTrue("Decode should succeed", decoded.isSuccess)
        val decodedToken = decoded.getOrNull()!!
        Assert.assertTrue("Token should be expired", decodedToken.isExpired)
    }

    @Test
    fun testPairingTokenVerify() {
        val token = PairingToken(
            tokenId = "tok_123",
            opaque = "opaque_456",
            familyId = "fam_789",
            parentUserId = "user_123",
            parentDisplayName = "John Doe",
            parentEmail = "john@example.com",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 120000,
            nonce = "nonce_abc",
        )

        val sameToken = token.copy()
        val differentToken = token.copy(tokenId = "tok_999")

        Assert.assertTrue("Same token should verify", token.verify(sameToken))
        Assert.assertFalse("Different token should not verify", token.verify(differentToken))
    }

    @Test
    fun testPairingTokenFactory() {
        val factory = PairingTokenFactory()
        
        val token = factory.issue(
            familyId = "fam_123",
            parentUserId = "user_456",
            parentDisplayName = "Jane Smith",
            parentEmail = "jane@example.com",
        )

        Assert.assertNotNull(token.tokenId)
        Assert.assertNotNull(token.opaque)
        Assert.assertNotNull(token.nonce)
        Assert.assertEquals("fam_123", token.familyId)
        Assert.assertEquals("user_456", token.parentUserId)
        Assert.assertEquals("Jane Smith", token.parentDisplayName)
        Assert.assertEquals("jane@example.com", token.parentEmail)
        Assert.assertFalse(token.isConsumed)
        Assert.assertNull(token.consumedByDeviceId)
        Assert.assertFalse(token.isExpired)
    }

    @Test
    fun testPairingTokenFactoryHashKey() {
        val factory = PairingTokenFactory()
        val token = factory.issue(
            familyId = "fam_123",
            parentUserId = "user_456",
            parentDisplayName = "Jane Smith",
            parentEmail = "jane@example.com",
        )

        val hashKey = factory.hashKey(token)
        Assert.assertEquals(64, hashKey.length) // SHA-256 hex = 64 chars
    }

    @Test
    fun testTokenConsumption() {
        val token = PairingToken(
            tokenId = "tok_123",
            opaque = "opaque_456",
            familyId = "fam_789",
            parentUserId = "user_123",
            parentDisplayName = "John Doe",
            parentEmail = "john@example.com",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 120000,
            nonce = "nonce_abc",
            isConsumed = false,
        )

        Assert.assertFalse(token.isConsumed)
        
        val consumedToken = token.copy(isConsumed = true)
        Assert.assertTrue(consumedToken.isConsumed)
    }
}