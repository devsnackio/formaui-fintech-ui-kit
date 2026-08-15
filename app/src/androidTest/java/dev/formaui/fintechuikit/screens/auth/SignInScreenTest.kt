/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens.auth

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Sign-in, and the two different kinds of "wrong" this form distinguishes.
 *
 * The inline hint is about *shape* — it appears only once the field is non-blank, so an untouched
 * form never shows red. The error banner is about *outcome*, and only the caller knows it. Conflating
 * the two is how a form ends up scolding someone who has typed one character.
 */
class SignInScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun anUntouchedFormShowsNoError() {
        compose.showSignIn(email = "", password = "")

        compose.onNodeWithText("That doesn't look like an email address").assertDoesNotExist()
        compose.onNodeWithText("Sign in").assertIsNotEnabled()
    }

    @Test
    fun aMalformedEmailIsFlaggedOnceSomethingIsTyped() {
        compose.showSignIn(email = "alex.morgan@example", password = "hunter2000")

        compose.onNodeWithText("That doesn't look like an email address").assertExists()
        compose.onNodeWithText("Sign in").assertIsNotEnabled()
    }

    @Test
    fun aWellFormedFormEnablesSubmit() {
        compose.showSignIn()

        compose.onNodeWithText("That doesn't look like an email address").assertDoesNotExist()
        compose.onNodeWithText("Sign in").assertIsEnabled()
    }

    @Test
    fun anEmptyPasswordBlocksSubmit() {
        compose.showSignIn(password = "")

        compose.onNodeWithText("Sign in").assertIsNotEnabled()
    }

    /** The rejection the form cannot predict — supplied by the caller, shown as a banner. */
    @Test
    fun aRejectionFromTheCallerIsShown() {
        compose.showSignIn(errorMessage = "That email and password don't match an account.")

        compose.onNodeWithText("That email and password don't match an account.").assertExists()
    }

    /** While a submission is in flight the button says so and refuses a second tap. */
    @Test
    fun submittingLocksTheForm() {
        compose.showSignIn(submitting = true)

        compose.onNodeWithText("Signing in…").assertIsNotEnabled()
    }

    @Test
    fun submitReportsUp() {
        var submitted = false
        compose.showSignIn(onSubmit = { submitted = true })

        compose.onNodeWithText("Sign in").performClick()

        assertTrue(submitted)
    }

    @Test
    fun theForgotPasswordLinkNavigates() {
        var opened = false
        compose.showSignIn(onForgotPassword = { opened = true })

        compose.onNodeWithText("Forgot password?").performClick()

        assertTrue(opened)
    }

    @Test
    fun theCreateAccountLinkNavigates() {
        var opened = false
        compose.showSignIn(onCreateAccount = { opened = true })

        compose.onNodeWithText("Create an account").performClick()

        assertTrue(opened)
    }
}

private fun ComposeContentTestRule.showSignIn(
    email: String = "alex.morgan@example.com",
    password: String = "hunter2000",
    errorMessage: String? = null,
    submitting: Boolean = false,
    onSubmit: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
) {
    setThemedContent {
        SignInScreen(
            email = email,
            onEmailChange = {},
            password = password,
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
            errorMessage = errorMessage,
            submitting = submitting,
            onSubmit = onSubmit,
            onForgotPassword = onForgotPassword,
            onCreateAccount = onCreateAccount,
        )
    }
}
