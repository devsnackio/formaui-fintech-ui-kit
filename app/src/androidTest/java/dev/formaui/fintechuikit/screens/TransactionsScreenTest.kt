/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The Activity tab's filtering. The screen is stateless — query and category are hoisted — so these
 * drive it by re-rendering with a given filter rather than by typing, which is exactly how
 * `MainActivity` uses it.
 */
class TransactionsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unfilteredListShowsTheLedger() {
        compose.showTransactions()

        compose.onNodeWithText("Whole Foods").assertExists()
    }

    @Test
    fun categoryFilterNarrowsTheList() {
        compose.showTransactions(category = SpendCategory.Income)

        compose.onNodeWithText("Acme Corp").assertExists()
        compose.onNodeWithText("Whole Foods").assertDoesNotExist()
    }

    /**
     * A partial, lower-case query on purpose. It exercises the `contains(ignoreCase = true)` match
     * rather than an equality that would pass either way — and it keeps the assertion unambiguous,
     * since the search field renders the query too and a full "Netflix" matches the field as well as
     * the row.
     */
    @Test
    fun queryFilterNarrowsTheList() {
        compose.showTransactions(query = "netfl")

        compose.onNodeWithText("Netflix").assertExists()
        compose.onNodeWithText("Whole Foods").assertDoesNotExist()
    }

    /**
     * The demonstration point of the screen: a filtered-to-nothing list gets `FormaEmptyState`, not a
     * blank column that reads as a loading failure.
     */
    @Test
    fun emptyStateAppearsWhenNothingMatches() {
        compose.showTransactions(query = "nothing matches this")

        compose.onNodeWithText("No transactions found").assertExists()
    }

    @Test
    fun tappingARowReportsThatTransaction() {
        var tapped: Transaction? = null
        compose.showTransactions(
            category = SpendCategory.Income,
            onTransactionClick = { tapped = it },
        )

        compose.onNodeWithText("Acme Corp").performClick()

        assertEquals("t3", tapped?.id)
    }
}

private fun ComposeContentTestRule.showTransactions(
    query: String = "",
    category: SpendCategory? = null,
    onTransactionClick: (Transaction) -> Unit = {},
) {
    setThemedContent {
        TransactionsScreen(
            transactions = SampleData.transactions,
            query = query,
            onQueryChange = {},
            selectedCategory = category,
            onCategorySelected = {},
            today = SampleData.today,
            onTransactionClick = onTransactionClick,
        )
    }
}
