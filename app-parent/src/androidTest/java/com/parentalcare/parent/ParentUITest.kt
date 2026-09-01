package com.parentalcare.parent

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertExists
import androidx.hilt.android.testing.HiltAndroidRule
import androidx.hilt.android.testing.HiltTestApplication
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.parentalcare.parent.ui.screen.PairingQRScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Parent App.
 * These tests use Hilt for dependency injection and Compose UI testing.
 */
@RunWith(AndroidJUnit4::class)
@HiltTestApplication
class ParentUITest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var composeRule = createComposeRule()

    @get:Rule
    var grantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun pairingQRScreen_showsQRCode() {
        // Given
        var onBackCalled = false
        
        // When
        composeRule.setContent {
            com.parentalcare.parent.ui.screen.PairingQRScreen(
                onBack = { onBackCalled = true }
            )
        }

        // Then
        composeRule
            .onNodeWithText("Pairing QR Code")
            .assertExists()
        
        composeRule
            .onNodeWithText("Scan this QR code")
            .assertExists()
    }

    @Test
    fun pairingQRScreen_backButtonNavigatesBack() {
        // Given
        var onBackCalled = false
        
        // When
        composeRule.setContent {
            com.parentalcare.parent.ui.screen.PairingQRScreen(
                onBack = { onBackCalled = true }
            )
        }
        
        composeRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Then
        assert(onBackCalled)
    }
}