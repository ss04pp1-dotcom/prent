package com.parentalcare.core.security.crypto

import android.util.Base64
import com.parentalcare.core.security.keystore.KeystoreManager
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class ScreenshotEncryptor(
    private val keystore: KeystoreManager,
) {
    /**
     * Child side: Encrypts screenshot bytes and wraps the AES key with Parent's RSA public key.
     */
    fun encrypt(plaintext: ByteArray, parentPublicKeyBase64: String): EncryptedPayload {
        val contentKey = ByteArray(KEY_SIZE_BYTES).also { SECURE_RANDOM.nextBytes(it) }
        val iv = ByteArray(IV_SIZE_BYTES).also { SECURE_RANDOM.nextBytes(it) }

        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(contentKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        val ciphertext = cipher.doFinal(plaintext)

        val wrapped = keystore.wrapByChild(contentKey, parentPublicKeyBase64)

        return EncryptedPayload(
            iv = Base64.encodeToString(iv, BASE64_FLAGS),
            wrappedKey = wrapped,
            ciphertext = Base64.encodeToString(ciphertext, BASE64_FLAGS),
        )
    }

    /**
     * Parent side: Decrypts using payload containing base64 ciphertext (tests mostly).
     */
    fun decrypt(payload: EncryptedPayload): ByteArray {
        val contentKey = keystore.unwrapByParent(payload.wrappedKey)
        val iv = Base64.decode(payload.iv, BASE64_FLAGS)
        val ciphertext = Base64.decode(payload.ciphertext, BASE64_FLAGS)

        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(contentKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    /**
     * Parent side: Decrypts using the actual downloaded encrypted content from Storage.
     */
    fun decryptWithContent(payload: EncryptedPayload, encryptedContent: ByteArray): ByteArray {
        val contentKey = keystore.unwrapByParent(payload.wrappedKey)
        val iv = Base64.decode(payload.iv, BASE64_FLAGS)

        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(contentKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return cipher.doFinal(encryptedContent)
    }

    private companion object {
        const val KEY_SIZE_BYTES = 32 // 256-bit AES
        const val IV_SIZE_BYTES = 12  // 96-bit GCM IV
        const val GCM_TAG_BITS = 128
        const val TRANSFORM = "AES/GCM/NoPadding"
        const val BASE64_FLAGS = Base64.NO_WRAP
        val SECURE_RANDOM: SecureRandom = SecureRandom()
    }
}

data class EncryptedPayload(
    val iv: String,
    val wrappedKey: String,
    val ciphertext: String, 
)
