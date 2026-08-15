/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The shared completion screen. Every money-movement flow ends here, and one of them — changing a
 * PIN — moves no money at all, which is the case these tests exist for.
 */
class SuccessScreenTest {

    private val sent = Money.of(250, 0, Currency.USD)

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTheHeadlineAmountAndSubtitle() {
        compose.setThemedContent {
            SuccessScreen(
                headline = "Sent.",
                amount = sent,
                subtitle = "to Kenji Watanabe",
                onDone = {},
            )
        }

        compose.onNodeWithText("Sent.").assertExists()
        compose.onNodeWithText("$250.00").assertExists()
        compose.onNodeWithText("to Kenji Watanabe").assertExists()
    }

    /**
     * A completion that moved no money omits the figure rather than rendering a zero. A large
     * "$0.00" reads as an amount the user should care about, not as an absence — which is wrong on
     * the PIN-changed screen.
     */
    @Test
    fun anAmountlessCompletionShowsNoFigure() {
        compose.setThemedContent {
            SuccessScreen(
                headline = "PIN changed.",
                amount = null,
                subtitle = "Use it next time you pay in a shop",
                onDone = {},
            )
        }

        compose.onNodeWithText("PIN changed.").assertExists()
        compose.onNodeWithText("$0.00").assertDoesNotExist()
    }

    @Test
    fun doneDismissesTheFlow() {
        var done = 0
        compose.setThemedContent {
            SuccessScreen("Sent.", sent, "to Kenji Watanabe", onDone = { done++ })
        }

        compose.onNodeWithText("Done").performClick()

        assertEquals(1, done)
    }

    /** The secondary action is omitted rather than shown inert when the caller has nothing to link. */
    @Test
    fun viewReceiptIsAbsentUnlessSupplied() {
        compose.setThemedContent {
            SuccessScreen("Sent.", sent, "to Kenji Watanabe", onDone = {})
        }

        compose.onNodeWithText("View receipt").assertDoesNotExist()
    }

    @Test
    fun viewReceiptFiresWhenSupplied() {
        var receipts = 0
        compose.setThemedContent {
            SuccessScreen(
                headline = "Sent.",
                amount = sent,
                subtitle = "to Kenji Watanabe",
                onDone = {},
                onViewReceipt = { receipts++ },
            )
        }

        compose.onNodeWithText("View receipt").performClick()

        assertEquals(1, receipts)
    }
}
