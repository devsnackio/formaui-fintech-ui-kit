/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Security settings — the PIN row is the only one here that goes anywhere. */
class SecurityScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theCardPinRowLeadsToTheChangePinFlow() {
        var opened = false
        compose.setThemedContent {
            SecurityScreen(onChangePin = { opened = true }, onBack = {})
        }

        compose.onNodeWithText("Card PIN").performScrollTo().performClick()

        assertTrue(opened)
    }

    @Test
    fun listsWhereYouAreSignedIn() {
        compose.setThemedContent { SecurityScreen(onChangePin = {}, onBack = {}) }

        compose.onNodeWithText("Where you're signed in").performScrollTo().assertExists()
        compose.onNodeWithText("This device").assertExists()
    }

    @Test
    fun theBackArrowInvokesOnBack() {
        var backs = 0
        compose.setThemedContent {
            SecurityScreen(onChangePin = {}, onBack = { backs++ })
        }

        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}
