/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * The PIN gate. Its one subtle behaviour is that [onComplete] fires from a `LaunchedEffect` keyed on
 * the PIN, not from the keypad — so it must fire exactly once, when the last digit lands, and not on
 * the way there.
 */
class PinEntryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsThePrompt() {
        compose.setThemedContent { PinEntryScreen(pin = "", onPinChange = {}) }

        compose.onNodeWithText("Enter your PIN").assertExists()
    }

    @Test
    fun tappingADigitAppendsIt() {
        var entered: String? = null
        compose.setThemedContent { PinEntryScreen(pin = "12", onPinChange = { entered = it }) }

        compose.onNodeWithText("9").performClick()

        assertEquals("129", entered)
    }

    @Test
    fun backspaceRemovesTheLastDigit() {
        var entered: String? = null
        compose.setThemedContent { PinEntryScreen(pin = "12", onPinChange = { entered = it }) }

        compose.onNodeWithContentDescription("Delete last digit").performClick()

        assertEquals("1", entered)
    }

    /**
     * Driven through real state, because completion is a reaction to the PIN, not to the tap.
     *
     * Six digits, not four: `PinDefaults.Length` is six — four is only the *minimum* the dot row
     * supports. A test that stopped at four would never reach the branch it means to exercise.
     */
    @Test
    fun completionFiresOnTheFinalDigitOnly() {
        var pin by mutableStateOf("")
        val completions = mutableListOf<String>()
        compose.setThemedContent {
            PinEntryScreen(pin = pin, onPinChange = { pin = it }, onComplete = { completions += it })
        }

        listOf("1", "2", "3", "4", "5").forEach { compose.onNodeWithText(it).performClick() }
        assertEquals("12345", pin)
        assertEquals(emptyList<String>(), completions)

        compose.onNodeWithText("6").performClick()
        // Explicit, because completion is a `LaunchedEffect` rather than a callback on the tap. The
        // five taps above each got synced by the *next* interaction; the last one has nothing
        // following it, so without this the assertion runs before the effect does.
        compose.waitForIdle()

        assertEquals(listOf("123456"), completions)
    }

    @Test
    fun aWrongPinExplainsItself() {
        compose.setThemedContent {
            PinEntryScreen(
                pin = "",
                onPinChange = {},
                isError = true,
                errorMessage = "Incorrect PIN. 2 attempts left.",
            )
        }

        compose.onNodeWithText("Incorrect PIN. 2 attempts left.").assertExists()
    }

    /**
     * The escape hatch confirms first — leaving the PIN gate means signing in with a password, and
     * the screen explains that before the host navigates. The action and the dialog title share the
     * words "Forgot your PIN?", so the confirming click is scoped to the dialog.
     */
    @Test
    fun forgettingThePinConfirmsBeforeLeaving() {
        var forgot = false
        compose.setThemedContent {
            PinEntryScreen(pin = "", onPinChange = {}, onForgotPin = { forgot = true })
        }

        compose.onAllNodesWithText("Forgot your PIN?").onFirst().performClick()

        compose.onNodeWithText("Keep trying").assertExists()
        assertEquals(false, forgot)

        compose.onNodeWithText("Go to sign in").performClick()

        assertEquals(true, forgot)
    }

    /**
     * The biometric key is omitted, not disabled, when the caller offers no biometric route — its
     * slot stays empty space so the "0" does not slide out from under the thumb.
     */
    @Test
    fun theBiometricKeyIsAbsentUnlessOffered() {
        compose.setThemedContent { PinEntryScreen(pin = "", onPinChange = {}) }

        compose.onNodeWithContentDescription("Unlock with biometrics").assertDoesNotExist()
    }

    @Test
    fun theBiometricKeyFiresWhenOffered() {
        var unlocked: Boolean? = null
        compose.setThemedContent {
            PinEntryScreen(pin = "", onPinChange = {}, onBiometric = { unlocked = true })
        }

        compose.onNodeWithContentDescription("Unlock with biometrics").performClick()

        assertEquals(true, unlocked)
    }

    @Test
    fun aDisabledKeypadAcceptsNothing() {
        var entered: String? = null
        compose.setThemedContent {
            PinEntryScreen(pin = "", onPinChange = { entered = it }, enabled = false)
        }

        compose.onNodeWithText("5").performClick()

        assertNull(entered)
    }
}
