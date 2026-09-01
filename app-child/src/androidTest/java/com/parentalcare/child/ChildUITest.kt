package com.parentalcare.child

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertExists
import androidx.hilt.android.testing.HiltAndroidRule
import androidx.hilt.android.testing.HiltTestApplication
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.parentalcare.child.ui.screen.PairingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Child App.
 */
@RunWith(AndroidJUnit4::class)
@HiltTestApplication
class ChildUITest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var composeRule = createComposeRule()

    @get:Rule
    var grantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun pairingScreen_showsQRScanner() {
        // Given
        var onPaired = false
        var onBackCalled = false
        
        // When
        composeRule.setContent {
            PairingScreen(
                onPaired = { onPaired = true },
                onBack = { onBackCalled = true }
            )
        }

        // Then
        composeRule
            .onNodeWithText("Connect to Parent")
            .assertExists()
        
        composeRule
            .onNodeWithText("Scan QR Code")
            .assertExists()
    }

    @Test
    fun pairingScreen_backButtonNavigatesBack() {
        // Given
        var onBackCalled = false
        
        // When
        composeRule.setContent {
            PairingScreen(
                onPaired = {},
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