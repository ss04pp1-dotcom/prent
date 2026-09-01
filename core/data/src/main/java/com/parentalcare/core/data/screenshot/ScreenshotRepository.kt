package com.parentalcare.core.data.screenshot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.getOrThrow
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.common.util.Redactor
import com.parentalcare.core.data.model.ScreenshotDoc
import com.parentalcare.core.data.supabase.SupabasePaths
import com.parentalcare.core.security.crypto.ScreenshotEncryptor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val encryptor: ScreenshotEncryptor,
) {
    private val screenshotsTable = SupabasePaths.TABLE_SCREENSHOTS
    private val storageBucket = SupabasePaths.STORAGE_BUCKET_SCREENSHOTS

    suspend fun uploadCaptured(
        bitmap: android.graphics.Bitmap,
        request: com.parentalcare.core.data.model.ScreenshotRequestDoc,
        parentPublicKeyBase64: String
    ): Result<ScreenshotDoc> = resultOf {
        val currentUser = supabase.auth.currentUserOrNull()
        val uid = currentUser?.id ?: throw IllegalStateException("not authenticated")

        val out = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, out)
        var compressed = out.toByteArray()
        
        if (compressed.size > 5 * 1024 * 1024) {
            val out2 = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 40, out2)
            compressed = out2.toByteArray()
        }

        val payload = encryptor.encrypt(compressed, parentPublicKeyBase64)
        val screenshotId = "shot_${request.requestId}_${System.currentTimeMillis()}"
        val storagePath = "families/${request.familyId}/screenshots/${request.childDeviceId}/$screenshotId.enc"

        try {
            supabase.storage.from(storageBucket).upload(
                storagePath, 
                android.util.Base64.decode(payload.ciphertext, android.util.Base64.NO_WRAP)
            )
        } catch (e: Exception) {
            throw IllegalStateException("Storage upload failed", e)
        }

        val now = System.currentTimeMillis()
        val retentionHours = 24L
        
        val thumbRatio = 256f / maxOf(bitmap.width, bitmap.height)
        val tw = (bitmap.width * thumbRatio).toInt().coerceAtLeast(1)
        val th = (bitmap.height * thumbRatio).toInt().coerceAtLeast(1)
        val thumb = android.graphics.Bitmap.createScaledBitmap(bitmap, tw, th, true)
        val thumbOut = java.io.ByteArrayOutputStream()
        thumb.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, thumbOut)
        val thumbBase64 = android.util.Base64.encodeToString(thumbOut.toByteArray(), android.util.Base64.NO_WRAP)

        val doc = ScreenshotDoc(
            screenshotId = screenshotId,
            familyId = request.familyId,
            parentUserId = request.parentUserId,
            childDeviceId = request.childDeviceId,
            requestId = request.requestId,
            storagePath = storagePath,
            iv = payload.iv,
            wrappedKey = payload.wrappedKey,
            thumbnailBase64 = thumbBase64,
            mimeType = "image/jpeg",
            widthPx = bitmap.width,
            heightPx = bitmap.height,
            sizeBytes = compressed.size.toLong(),
            capturedAt = now,
            retentionExpiresAt = now + retentionHours * 60 * 60 * 1000,
            isUnread = true,
            encryptedPayloadBase64 = ""
        )
        
        try {
            supabase.postgrest[screenshotsTable].insert(doc)
        } catch (e: Exception) {
            runCatching { supabase.storage.from(storageBucket).delete(storagePath) }
            throw IllegalStateException("Database insert failed", e)
        }
        
        Timber.tag(TAG).i("uploaded: shotId=%s size=%dB", screenshotId.take(8), compressed.size)
        doc
    }

    suspend fun downloadAndDecrypt(familyId: String, screenshotId: String): Result<Bitmap> = resultOf {
        val doc = supabase.postgrest[screenshotsTable]
            .select {
                filter {
                    eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                    eq(SupabasePaths.Columns.SCREENSHOT_ID, screenshotId)
                }
            }
            .decodeSingleOrNull<ScreenshotDoc>()
            ?: throw IllegalStateException("Screenshot not found")

        val encryptedBytes = downloadEncryptedFile(doc.storagePath).getOrThrow()
        
        val payload = com.parentalcare.core.security.crypto.EncryptedPayload(
            iv = doc.iv,
            wrappedKey = doc.wrappedKey,
            ciphertext = ""
        )
        
        val plaintext = encryptor.decryptWithContent(payload, encryptedBytes)
        decodeBitmap(plaintext)
    }

    suspend fun fetchDecryptedBitmap(doc: ScreenshotDoc): Result<Bitmap> = resultOf {
        val encryptedBytes = downloadEncryptedFile(doc.storagePath).getOrThrow()
        val payload = com.parentalcare.core.security.crypto.EncryptedPayload(
            iv = doc.iv,
            wrappedKey = doc.wrappedKey,
            ciphertext = ""
        )
        val plaintext = encryptor.decryptWithContent(payload, encryptedBytes)
        decodeBitmap(plaintext)
    }

    suspend fun listRecent(familyId: String, limit: Int = 20): Result<List<ScreenshotDoc>> = resultOf {
        supabase.postgrest[screenshotsTable]
            .select {
                filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) }
                order(SupabasePaths.Columns.CAPTURED_AT, io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit((limit).toLong())
            }
            .decodeList<ScreenshotDoc>()
    }

    fun observeScreenshot(familyId: String, screenshotId: String): Flow<ScreenshotDoc?> = callbackFlow {
        val channel = supabase.realtime.channel("screenshot_$screenshotId")
        val job = Job()
        
        launch(job) {
            channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = screenshotsTable
                filter = "screenshot_id=eq.$screenshotId"
            }.collect { change ->
                try {
                    val doc = supabase.postgrest[screenshotsTable]
                        .select {
                            filter {
                                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                                eq(SupabasePaths.Columns.SCREENSHOT_ID, screenshotId)
                            }
                        }
                        .decodeSingleOrNull<ScreenshotDoc>()
                    trySend(doc)
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching screenshot doc after change")
                }
            }
        }
        
        supabase.realtime.connect()
        channel.subscribe()
        
        launch {
            try {
                val doc = supabase.postgrest[screenshotsTable]
                    .select {
                        filter {
                            eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                            eq(SupabasePaths.Columns.SCREENSHOT_ID, screenshotId)
                        }
                    }
                    .decodeSingleOrNull<ScreenshotDoc>()
                trySend(doc)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching initial screenshot doc")
            }
        }
        
        awaitClose {
            job.cancel()
            launch { supabase.realtime.removeChannel(channel) }
        }
    }

    fun listenForFamilyScreenshots(familyId: String): Flow<List<ScreenshotDoc>> = callbackFlow {
        val channel = supabase.realtime.channel("family_screenshots_$familyId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            this.table = screenshotsTable
            this.filter = "family_id=eq.$familyId"
        }
        
        val job = launch {
            flow.collect { action ->
                try {
                    val list = supabase.postgrest[screenshotsTable]
                        .select { filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) } }
                        .decodeList<ScreenshotDoc>()
                    trySend(list)
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching family screenshots")
                }
            }
        }
        
        supabase.realtime.connect()
        channel.subscribe()
        
        launch {
            try {
                val list = supabase.postgrest[screenshotsTable]
                    .select { filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) } }
                    .decodeList<ScreenshotDoc>()
                trySend(list)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching initial family screenshots")
            }
        }

        awaitClose {
            job.cancel()
            launch { supabase.realtime.removeChannel(channel) }
        }
    }

    private suspend fun downloadEncryptedFile(storagePath: String): Result<ByteArray> {
        return try {
            val bytes = supabase.storage.from(storageBucket).downloadAuthenticated(storagePath)
            Result.Success(bytes)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown storage error"
            val appError = when {
                errorMsg.contains("404") || errorMsg.contains("not found") -> 
                    com.parentalcare.core.common.result.AppError.NotFound
                errorMsg.contains("403") || errorMsg.contains("permission") -> 
                    com.parentalcare.core.common.result.AppError.Unauthorized
                else -> 
                    com.parentalcare.core.common.result.AppError.Unknown("Storage download failed: $errorMsg")
            }
            Result.Failure(appError)
        }
    }

    suspend fun markViewed(familyId: String, screenshotId: String): Result<Unit> = resultOf {
        supabase.postgrest[screenshotsTable].update(
            mapOf(
                SupabasePaths.Columns.VIEWED_AT to System.currentTimeMillis(),
                SupabasePaths.Columns.IS_UNREAD to false
            )
        ) {
            filter {
                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                eq(SupabasePaths.Columns.SCREENSHOT_ID, screenshotId)
            }
        }
    }

    suspend fun deletePermanently(doc: ScreenshotDoc): Result<Unit> = resultOf {
        try {
            supabase.storage.from(storageBucket).delete(doc.storagePath)
        } catch (e: Exception) {
            val errorMsg = Redactor.redact(e.message)
            throw IllegalStateException("Storage delete failed: $errorMsg")
        }
        try {
            supabase.postgrest[screenshotsTable].delete {
                filter {
                    eq(SupabasePaths.Columns.FAMILY_ID, doc.familyId)
                    eq(SupabasePaths.Columns.SCREENSHOT_ID, doc.screenshotId)
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("DB delete failed after storage delete: shotId=%s", Redactor.idPrefix(doc.screenshotId))
            val errorMsg = Redactor.redact(e.message)
            throw IllegalStateException("Database delete failed after storage delete: $errorMsg")
        }
        Timber.tag(TAG).i("permanently deleted: shotId=%s", doc.screenshotId.take(8))
    }

    suspend fun sweepExpired(familyId: String, now: Long = System.currentTimeMillis()): Result<Int> = resultOf {
        val expired = supabase.postgrest[screenshotsTable]
            .select {
                filter {
                    eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                    lt(SupabasePaths.Columns.RETENTION_EXPIRES_AT, now)
                }
            }
            .decodeList<ScreenshotDoc>()
        for (doc in expired) {
            runCatching { deletePermanently(doc) }
        }
        expired.size
    }

    private fun decodeBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("decode failed")
    }

    private companion object {
        const val TAG = "PC.SupabaseScreenshotRepo"
    }
}
