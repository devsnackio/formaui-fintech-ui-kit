/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.components

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.components.transaction.TransactionRow
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The row every list in the app is built from — Dashboard, Activity, card activity and statements
 * all render this, so a regression here shows up in four places at once.
 */
class TransactionRowTest {

    @get:Rule
    val compose = createComposeRule()

    /** Whole Foods — a plain debit with no note. */
    private val groceries = SampleData.transactions.first { it.id == "t1" }

    /** Pacific Gas — Failed, and the one fixture carrying a note ("Card declined"). */
    private val failed = SampleData.transactions.first { it.id == "t8" }

    @Test
    fun showsMerchantAndCategory() {
        compose.setThemedContent { TransactionRow(groceries) }

        compose.onNodeWithText("Whole Foods").assertIsDisplayed()
        compose.onNodeWithText("Groceries").assertIsDisplayed()
    }

    /**
     * The note is the more specific fact, so it replaces the category rather than joining it. A row
     * showing both would say "Utilities · Card declined" and bury the reason the payment failed.
     */
    @Test
    fun noteReplacesCategoryWhenPresent() {
        compose.setThemedContent { TransactionRow(failed) }

        compose.onNodeWithText("Card declined").assertIsDisplayed()
        compose.onNodeWithText("Utilities").assertDoesNotExist()
    }

    /**
     * A failed payment must be legible as failed from the row alone. The amount is greyed to `mute`
     * for the same reason, but colour cannot be the only signal — hence the pill.
     */
    @Test
    fun failedTransactionCarriesItsStatusPill() {
        compose.setThemedContent { TransactionRow(failed) }

        compose.onNodeWithText("Failed").assertIsDisplayed()
    }

    @Test
    fun clickInvokesTheCallbackOnce() {
        var clicks = 0
        compose.setThemedContent { TransactionRow(groceries, onClick = { clicks++ }) }

        compose.onNodeWithText("Whole Foods").performClick()

        assertEquals(1, clicks)
    }

    /**
     * The inverse, and the one that actually guards something: `onClick` is nullable, and a row
     * given none must not look or behave interactive. Statements shipped inert rows once already.
     */
    @Test
    fun rowWithoutACallbackIsNotClickable() {
        compose.setThemedContent { TransactionRow(groceries) }

        compose.onNodeWithText("Whole Foods").assertHasNoClickAction()
    }
}
