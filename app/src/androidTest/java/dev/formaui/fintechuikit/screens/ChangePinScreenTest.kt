/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Setting a new card PIN, which is two stages wearing one screen. The stage is hoisted, so what is
 * testable here is that each stage says which one it is — a confirm step that looked identical to
 * the first would have the user re-entering a PIN with no idea why.
 */
class ChangePinScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theChooseStageAsksForANewPin() {
        compose.setThemedContent {
            ChangePinScreen(pin = "", onPinChange = {}, stage = PinStage.Choose, onBack = {})
        }

        compose.onNodeWithText("Choose a new PIN").assertExists()
        compose.onNodeWithText("Enter it again").assertDoesNotExist()
    }

    @Test
    fun theConfirmStageAsksForItAgain() {
        compose.setThemedContent {
            ChangePinScreen(pin = "", onPinChange = {}, stage = PinStage.Confirm, onBack = {})
        }

        compose.onNodeWithText("Enter it again").assertExists()
    }

    @Test
    fun aMismatchIsExplainedRatherThanJustReset() {
        compose.setThemedContent {
            ChangePinScreen(
                pin = "",
                onPinChange = {},
                stage = PinStage.Choose,
                onBack = {},
                isError = true,
                errorMessage = "That didn't match. Choose a PIN again.",
            )
        }

        compose.onNodeWithText("That didn't match. Choose a PIN again.").assertExists()
    }

    @Test
    fun tappingADigitAppendsIt() {
        var entered: String? = null
        compose.setThemedContent {
            ChangePinScreen(pin = "12", onPinChange = { entered = it }, stage = PinStage.Choose, onBack = {})
        }

        compose.onNodeWithText("7").performClick()

        assertEquals("127", entered)
    }

    @Test
    fun backspaceRemovesTheLastDigit() {
        var entered: String? = null
        compose.setThemedContent {
            ChangePinScreen(pin = "12", onPinChange = { entered = it }, stage = PinStage.Choose, onBack = {})
        }

        compose.onNodeWithContentDescription("Delete last digit").performClick()

        assertEquals("1", entered)
    }
}
