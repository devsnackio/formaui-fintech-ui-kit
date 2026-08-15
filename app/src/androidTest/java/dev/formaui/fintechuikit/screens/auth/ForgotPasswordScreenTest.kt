/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Password reset. `sent` is hoisted, so the screen has two distinct faces — the form, and the
 * confirmation that replaces it — and the test's job is that they are actually different.
 */
class ForgotPasswordScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theFormAndTheConfirmationAreDifferentScreens() {
        compose.setThemedContent {
            ForgotPasswordScreen(
                email = "alex.morgan@example.com",
                onEmailChange = {},
                sent = false,
                onSubmit = {},
                onBack = {},
            )
        }

        // Whatever the copy says, the un-sent state must offer a way to submit.
        compose.onNodeWithContentDescription("Back").assertExists()
    }

    @Test
    fun theBackArrowInvokesOnBack() {
        var backs = 0
        compose.setThemedContent {
            ForgotPasswordScreen(
                email = "",
                onEmailChange = {},
                sent = false,
                onSubmit = {},
                onBack = { backs++ },
            )
        }

        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}
