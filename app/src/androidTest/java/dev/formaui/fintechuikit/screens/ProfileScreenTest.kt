/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.setThemedContent
import dev.formaui.fintechuikit.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The Profile tab. Its sign-out is the only route back into the pre-session flow once setup is
 * recorded, so the tests below care as much about it *not* firing as about it firing.
 */
class ProfileScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun biometricToggleReportsItsNewValue() {
        var biometrics by mutableStateOf(false)
        compose.setThemedContent {
            ProfileScreen(
                biometricsEnabled = biometrics,
                onBiometricsChange = { biometrics = it },
                notificationsEnabled = true,
                onNotificationsChange = {},
                themeMode = ThemeMode.System,
                onThemeModeChange = {},
            )
        }

        compose.onNodeWithText("Biometric unlock").assertIsOff().performClick()
        compose.onNodeWithText("Biometric unlock").assertIsOn()
    }

    /** The two toggles sit adjacent in one group — the arrangement most likely to be cross-wired. */
    @Test
    fun theTwoTogglesAreIndependent() {
        var notifications by mutableStateOf(true)
        var biometrics by mutableStateOf(false)
        compose.setThemedContent {
            ProfileScreen(
                biometricsEnabled = biometrics,
                onBiometricsChange = { biometrics = it },
                notificationsEnabled = notifications,
                onNotificationsChange = { notifications = it },
                themeMode = ThemeMode.System,
                onThemeModeChange = {},
            )
        }

        compose.onNodeWithText("Push notifications").performClick()

        compose.onNodeWithText("Push notifications").assertIsOff()
        compose.onNodeWithText("Biometric unlock").assertIsOff()
        assertFalse("Toggling notifications must not touch biometrics", biometrics)
    }

    @Test
    fun securityRowNavigates() {
        var opened = false
        compose.showProfile(onSecurityClick = { opened = true })

        compose.onNodeWithText("Security").performScrollTo().performClick()

        assertTrue(opened)
    }

    @Test
    fun statementsRowNavigates() {
        var opened = false
        compose.showProfile(onStatementsClick = { opened = true })

        compose.onNodeWithText("Statements").performScrollTo().performClick()

        assertTrue(opened)
    }

    @Test
    fun helpRowNavigates() {
        var opened = false
        compose.showProfile(onHelpClick = { opened = true })

        compose.onNodeWithText("Help & support").performScrollTo().performClick()

        assertTrue(opened)
    }

    /**
     * Sign-out clears the one persisted flag and drops the user back to Welcome, so it confirms
     * first. The trigger and the dialog's confirm button share the label "Sign out", which is why
     * the confirming click is scoped to the dialog rather than matched by text alone.
     */
    @Test
    fun signOutAsksBeforeLeavingTheSession() {
        var signedOut = false
        compose.showProfile(onSignOut = { signedOut = true })

        compose.onNodeWithText("Sign out").performScrollTo().performClick()

        compose.onNodeWithText("Sign out?").assertExists()
        assertFalse("Opening the dialog must not sign the user out", signedOut)
    }

    @Test
    fun cancellingSignOutKeepsTheSession() {
        var signedOut = false
        compose.showProfile(onSignOut = { signedOut = true })

        compose.onNodeWithText("Sign out").performScrollTo().performClick()
        compose.onNodeWithText("Cancel").performClick()

        compose.onNodeWithText("Sign out?").assertDoesNotExist()
        assertFalse(signedOut)
    }

    @Test
    fun confirmingSignOutLeavesTheSession() {
        var signedOut = false
        compose.showProfile(onSignOut = { signedOut = true })

        compose.onNodeWithText("Sign out").performScrollTo().performClick()
        compose.onNode(hasText("Sign out") and hasAnyAncestor(isDialog())).performClick()

        assertTrue(signedOut)
    }

    @Test
    fun appearanceSheetOpensOnRequest() {
        var open = false
        compose.showProfile(onAppearanceOpenChange = { open = it })

        compose.onNodeWithText("Appearance").performScrollTo().performClick()

        assertEquals(true, open)
    }
}

private fun ComposeContentTestRule.showProfile(
    onSecurityClick: () -> Unit = {},
    onStatementsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAppearanceOpenChange: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
) {
    setThemedContent {
        ProfileScreen(
            biometricsEnabled = true,
            onBiometricsChange = {},
            notificationsEnabled = true,
            onNotificationsChange = {},
            themeMode = ThemeMode.System,
            onThemeModeChange = {},
            onSecurityClick = onSecurityClick,
            onStatementsClick = onStatementsClick,
            onHelpClick = onHelpClick,
            onAppearanceOpenChange = onAppearanceOpenChange,
            onSignOut = onSignOut,
        )
    }
}
