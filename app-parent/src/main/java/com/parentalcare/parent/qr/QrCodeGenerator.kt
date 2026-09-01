package com.parentalcare.parent.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a QR code bitmap for a pairing payload string.
 *
 * Uses ZXing's [QRCodeWriter] with M-level error correction — enough
 * redundancy for typical phone-screen-to-phone-screen scanning distance
 * while keeping the QR small.
 *
 * Output is a square monochrome bitmap. Caller should tint/color via
 * Compose's BitmapPainter.
 */
@Singleton
class QrCodeGenerator @Inject constructor() {

    fun generate(payload: String, side: Int = 720): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, side, side, hints)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
