/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Choosing who gets the money, and the review sheet in front of the irreversible step.
 *
 * The sheet is the point: a transfer is confirmed from a summary that restates fee, rate and total
 * debited, so "Confirm and send" is never one tap away from a recipient row.
 */
class TransferRecipientScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listsEveryRecipient() {
        compose.showRecipients()

        SampleData.recipients.forEach { compose.onNodeWithText(it.name).assertExists() }
    }

    @Test
    fun reviewIsBlockedUntilSomeoneIsChosen() {
        compose.showRecipients(selected = null)

        compose.onNodeWithText("Review transfer").assertIsNotEnabled()
    }

    @Test
    fun choosingARecipientReportsIt() {
        var chosen: Recipient? = null
        compose.showRecipients(selected = null, onRecipientSelected = { chosen = it })

        compose.onNodeWithText("Kenji Watanabe").performClick()

        assertEquals("r2", chosen?.id)
    }

    @Test
    fun searchNarrowsTheList() {
        compose.showRecipients()

        compose.onNode(hasSetTextAction()).performTextInput("kenji")

        compose.onNodeWithText("Kenji Watanabe").assertExists()
        compose.onNodeWithText("Sofia Almeida").assertDoesNotExist()
    }

    @Test
    fun searchWithNoMatchesSaysSo() {
        compose.showRecipients()

        compose.onNode(hasSetTextAction()).performTextInput("nobody by that name")

        compose.onNodeWithText("No matching recipients").assertExists()
    }

    /**
     * The confirmation is a summary sheet, not a second button on the list. It restates what leaves
     * the account including the fee — "Total debited" is the number the list never showed.
     */
    @Test
    fun reviewOpensASheetBeforeConfirming() {
        var confirmed = false
        compose.showRecipients(onConfirm = { confirmed = true })

        compose.onNodeWithText("Review transfer").assertIsEnabled().performClick()

        compose.onNodeWithText("Total debited").assertExists()
        compose.onNodeWithText("They receive").assertExists()
        assertEquals(false, confirmed)
    }

    @Test
    fun confirmingFromTheSheetSendsTheTransfer() {
        var confirmed = false
        compose.showRecipients(onConfirm = { confirmed = true })

        compose.onNodeWithText("Review transfer").performClick()
        compose.onNodeWithText("Confirm and send").performClick()

        assertTrue(confirmed)
    }
}

private fun ComposeContentTestRule.showRecipients(
    selected: Recipient? = SampleData.recipients.first(),
    onRecipientSelected: (Recipient) -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    setThemedContent {
        TransferRecipientScreen(
            sendAmount = "1000.00",
            rate = SampleData.usdToEur,
            recipients = SampleData.recipients,
            selectedRecipient = selected,
            onRecipientSelected = onRecipientSelected,
            onBack = {},
            onConfirm = onConfirm,
        )
    }
}
