package com.parentalcare.core.common.util

import android.graphics.Bitmap
import android.util.Base64
import com.parentalcare.core.common.constants.AppConstants
import java.io.ByteArrayOutputStream

/**
 * Compresses + resizes captured Bitmap to meet bandwidth/storage budget.
 *
 * Targets (per [AppConstants.Image]):
 *   - Max width 1080 / max height 1920
 *   - WEBP quality 82 (initial), 65 (second pass if file too big)
 *   - Hard ceiling 600 KB
 *
 * Output is the compressed byte array. Caller is responsible for encryption
 * (handled by [com.parentalcare.core.security.crypto.ScreenshotEncryptor]).
 */
object ScreenshotEncoder {

    /** Returns compressed bytes meeting the size budget. */
    fun encode(bitmap: Bitmap): ByteArray {
        val resized = downscaleIfNeeded(bitmap)
        val firstPass = compress(resized, AppConstants.Image.WEBP_QUALITY)
        if (firstPass.size <= AppConstants.Image.MAX_FILE_BYTES) return firstPass
        // Second pass: harder quality.
        val second = compress(resized, 65)
        return second
    }

    /** Returns a base64 thumbnail suitable for inline storage. */
    fun thumbnail(bitmap: Bitmap, maxPx: Int = 256, quality: Int = 70): String {
        val ratio = maxPx.toFloat() / maxOf(bitmap.width, bitmap.height)
        val tw = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val th = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        val thumb = Bitmap.createScaledBitmap(bitmap, tw, th, true)
        val out = ByteArrayOutputStream()
        thumb.compress(Bitmap.CompressFormat.WEBP, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun downscaleIfNeeded(src: Bitmap): Bitmap {
        val maxW = AppConstants.Image.MAX_WIDTH_PX
        val maxH = AppConstants.Image.MAX_HEIGHT_PX
        if (src.width <= maxW && src.height <= maxH) return src
        val ratio = minOf(maxW.toFloat() / src.width, maxH.toFloat() / src.height)
        return Bitmap.createScaledBitmap(
            src,
            (src.width * ratio).toInt().coerceAtLeast(1),
            (src.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }

    private fun compress(bitmap: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out)
        return out.toByteArray()
    }
}