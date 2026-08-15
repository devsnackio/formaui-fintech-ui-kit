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
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Account creation. Four conditions gate the CTA — name, a well-formed email, a long-enough
 * password, and accepted terms — so each is withheld in turn. A gate that checks three of four looks
 * identical until the fourth is the one missing.
 */
class SignUpScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aCompleteFormEnablesSubmit() {
        compose.showSignUp()

        compose.onNodeWithText("Create account").performScrollTo().assertIsEnabled()
    }

    @Test
    fun aMissingNameBlocksSubmit() {
        compose.showSignUp(name = "")

        compose.onNodeWithText("Create account").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun aMalformedEmailBlocksSubmitAndSaysWhy() {
        compose.showSignUp(email = "alex.morgan@example")

        compose.onNodeWithText("That doesn't look like an email address").assertExists()
        compose.onNodeWithText("Create account").performScrollTo().assertIsNotEnabled()
    }

    /** Eight characters is the documented floor; seven must not pass. */
    @Test
    fun aShortPasswordBlocksSubmit() {
        compose.showSignUp(password = "hunter7")

        compose.onNodeWithText("Create account").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun unacceptedTermsBlockSubmit() {
        compose.showSignUp(termsAccepted = false)

        compose.onNodeWithText("Create account").performScrollTo().assertIsNotEnabled()
    }

    /** The terms have to be readable before they can be meaningfully accepted. */
    @Test
    fun theTermsCanBeOpened() {
        var opened = false
        compose.showSignUp(onViewTerms = { opened = true })

        compose.onNodeWithText("Read the Terms of Service").performScrollTo().performClick()

        assertTrue(opened)
    }

    @Test
    fun theSignInLinkNavigates() {
        var opened = false
        compose.showSignUp(onSignIn = { opened = true })

        compose.onNodeWithText("Sign in").performScrollTo().performClick()

        assertTrue(opened)
    }
}

private fun ComposeContentTestRule.showSignUp(
    name: String = "Alex Morgan",
    email: String = "alex.morgan@example.com",
    password: String = "hunter2000",
    termsAccepted: Boolean = true,
    onViewTerms: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    setThemedContent {
        SignUpScreen(
            name = name,
            onNameChange = {},
            email = email,
            onEmailChange = {},
            password = password,
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
            termsAccepted = termsAccepted,
            onTermsAcceptedChange = {},
            onViewTerms = onViewTerms,
            onSignIn = onSignIn,
        )
    }
}
