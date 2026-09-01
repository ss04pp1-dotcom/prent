package com.parentalcare.core.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class KeyExchangeManager {

    fun getParentPublicKeyBase64(): String {
        val alias = MASTER_KEY_ALIAS
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val cert = ks.getCertificate(alias)
        
        if (cert != null) {
            return Base64.encodeToString(cert.publicKey.encoded, Base64.NO_WRAP)
        }

        // Generate new RSA key pair
        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setKeySize(RSA_KEY_SIZE_BITS)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            try {
                builder.setIsStrongBoxBacked(true)
                kpg.initialize(builder.build())
                val kp = kpg.generateKeyPair()
                return Base64.encodeToString(kp.public.encoded, Base64.NO_WRAP)
            } catch (t: Throwable) {
                Timber.tag(TAG).w("StrongBox unavailable for RSA key pair, falling back: %s", t.message)
            }
        }
        
        val fallbackBuilder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setKeySize(RSA_KEY_SIZE_BITS)
            
        kpg.initialize(fallbackBuilder.build())
        val kp = kpg.generateKeyPair()
        return Base64.encodeToString(kp.public.encoded, Base64.NO_WRAP)
    }

    fun unwrapByParent(wrappedKeyBase64: String): ByteArray {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = ks.getKey(MASTER_KEY_ALIAS, null) as? PrivateKey 
            ?: throw IllegalStateException("Parent RSA private key not found")
            
        val cipher = Cipher.getInstance(RSA_TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_PARAMS)
        val wrappedBytes = Base64.decode(wrappedKeyBase64, Base64.NO_WRAP)
        return cipher.doFinal(wrappedBytes)
    }

    fun wrapByChild(contentKey: ByteArray, parentPublicKeyBase64: String): String {
        val publicBytes = Base64.decode(parentPublicKeyBase64, Base64.NO_WRAP)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(java.security.spec.X509EncodedKeySpec(publicBytes))
        
        val cipher = Cipher.getInstance(RSA_TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP_PARAMS)
        val wrappedBytes = cipher.doFinal(contentKey)
        return Base64.encodeToString(wrappedBytes, Base64.NO_WRAP)
    }

    private companion object {
        const val TAG = "PC.KeyExchange"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val MASTER_KEY_ALIAS = "pc_parent_rsa_exchange"
        const val RSA_KEY_SIZE_BITS = 2048
        const val RSA_TRANSFORM = "RSA/ECB/OAEPPadding"
        val OAEP_PARAMS = OAEPParameterSpec(
            "SHA-256", 
            "MGF1", 
            MGF1ParameterSpec.SHA1, 
            PSource.PSpecified.DEFAULT
        )
    }
}
