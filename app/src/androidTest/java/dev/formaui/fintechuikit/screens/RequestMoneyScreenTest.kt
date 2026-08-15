/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Asking someone for money. The CTA needs *both* a recipient and a non-zero amount, so each is
 * withheld in turn — a gate that only checks one of two conditions passes a naive test.
 */
class RequestMoneyScreenTest {

    @get:Rule
    val compose = createComposeRule()

    /** The button restates the figure, so the label moves with the amount. */
    private val ctaFor75 = "Request $75.00"

    @Test
    fun theCtaIsBlockedWithoutARecipient() {
        compose.showRequest(amount = "75.00", selected = null)

        compose.onNodeWithText(ctaFor75).assertIsNotEnabled()
    }

    @Test
    fun theCtaIsBlockedWithoutAnAmount() {
        compose.showRequest(amount = "")

        compose.onNodeWithText("Request $0.00").assertIsNotEnabled()
    }

    @Test
    fun theCtaUnlocksWithBoth() {
        compose.showRequest(amount = "75.00")

        compose.onNodeWithText(ctaFor75).assertIsEnabled()
    }

    @Test
    fun creatingTheRequestReportsUp() {
        var created = false
        compose.showRequest(amount = "75.00", onCreate = { created = true })

        compose.onNodeWithText(ctaFor75).performClick()

        assertTrue(created)
    }

    @Test
    fun choosingARecipientReportsIt() {
        var chosen: Recipient? = null
        compose.showRequest(selected = null, onRecipientSelected = { chosen = it })

        compose.onNodeWithText("Priya Nair").performClick()

        assertEquals("r3", chosen?.id)
    }
}

private fun ComposeContentTestRule.showRequest(
    amount: String = "75.00",
    selected: Recipient? = SampleData.recipients.first(),
    onRecipientSelected: (Recipient) -> Unit = {},
    onCreate: () -> Unit = {},
) {
    setThemedContent {
        RequestMoneyScreen(
            amount = amount,
            onAmountChange = {},
            note = "Dinner on Friday",
            onNoteChange = {},
            recipients = SampleData.recipients,
            selectedRecipient = selected,
            onRecipientSelected = onRecipientSelected,
            account = SampleData.primaryAccount,
            onBack = {},
            onCreate = onCreate,
        )
    }
}
