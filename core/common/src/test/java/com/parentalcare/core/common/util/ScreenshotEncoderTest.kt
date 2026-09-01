package com.parentalcare.core.common.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.junit.Assert
import org.junit.Test

/**
 * Tests for the ScreenshotEncoder utility.
 */
class ScreenshotEncoderTest {

    private fun createTestBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    @Test
    fun testEncodeDownscalesLargeImage() {
        val largeBitmap = createTestBitmap(2000, 3000)
        val encoded = ScreenshotEncoder.encode(largeBitmap)

        val decoded = BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
        Assert.assertNotNull(decoded)
        Assert.assertTrue(decoded.width <= 1080)
        Assert.assertTrue(decoded.height <= 1920)
        largeBitmap.recycle()
        decoded.recycle()
    }

    @Test
    fun testEncodeKeepsSmallImage() {
        val smallBitmap = createTestBitmap(800, 600)
        val encoded = ScreenshotEncoder.encode(smallBitmap)

        val decoded = BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
        Assert.assertNotNull(decoded)
        Assert.assertEquals(800, decoded.width)
        Assert.assertEquals(600, decoded.height)
        smallBitmap.recycle()
        decoded.recycle()
    }

    @Test
    fun testEncodeRespectsMaxFileSize() {
        val largeBitmap = createTestBitmap(1920, 1080)
        val encoded = ScreenshotEncoder.encode(largeBitmap)

        Assert.assertTrue("Encoded size should be under 600KB", encoded.size <= 600 * 1024)
        
        val decoded = BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
        Assert.assertNotNull(decoded)
        largeBitmap.recycle()
        decoded.recycle()
    }

    @Test
    fun testThumbnailGeneration() {
        val bitmap = createTestBitmap(1920, 1080)
        val thumbnailBase64 = ScreenshotEncoder.thumbnail(bitmap, 256, 70)

        Assert.assertNotNull(thumbnailBase64)
        Assert.assertTrue(thumbnailBase64.length > 0)

        val decoded = BitmapFactory.decodeByteArray(
            android.util.Base64.decode(thumbnailBase64, android.util.Base64.NO_WRAP), 
            0, 
            android.util.Base64.decode(thumbnailBase64, android.util.Base64.NO_WRAP).size
        )
        Assert.assertNotNull(decoded)
        Assert.assertTrue(decoded.width <= 256)
        Assert.assertTrue(decoded.height <= 256)
        
        bitmap.recycle()
        decoded?.recycle()
    }

    @Test
    fun testWebpFormat() {
        val bitmap = createTestBitmap(1920, 1080)
        val encoded = ScreenshotEncoder.encode(bitmap)

        // Verify it's WEBP by checking the header
        // WEBP files start with "RIFF" followed by file size, then "WEBP"
        Assert.assertEquals(0x52, encoded[0].toInt()) // 'R'
        Assert.assertEquals(0x49, encoded[1].toInt()) // 'I'
        Assert.assertEquals(0x46, encoded[2].toInt()) // 'F'
        Assert.assertEquals(0x46, encoded[3].toInt()) // 'F'
        Assert.assertEquals(0x57, encoded[8].toInt()) // 'W' at position 8
        Assert.assertEquals(0x45, encoded[9].toInt()) // 'E' at position 9
        Assert.assertEquals(0x42, encoded[10].toInt()) // 'B' at position 10
        Assert.assertEquals(0x50, encoded[11].toInt()) // 'P' at position 11
        
        bitmap.recycle()
    }
}