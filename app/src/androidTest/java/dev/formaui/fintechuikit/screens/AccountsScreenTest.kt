/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The balance list behind "View all balances" — the only screen in the app that shows more than one
 * currency at once, which makes it the one place a shared formatter would go unnoticed.
 */
class AccountsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listsEveryHeldCurrency() {
        compose.setThemedContent {
            AccountsScreen(accounts = SampleData.accounts, onBack = {})
        }

        SampleData.accounts.forEach { account ->
            compose.onNodeWithText(account.label).assertExists()
        }
    }

    /**
     * Each balance is formatted in its own currency, not the primary one. `formatWithSymbol` reads
     * the `Money`'s currency, so a yen balance must render without decimals — the case a
     * dollar-shaped formatter gets wrong.
     */
    @Test
    fun eachBalanceUsesItsOwnCurrency() {
        compose.setThemedContent {
            AccountsScreen(accounts = SampleData.accounts, onBack = {})
        }

        SampleData.accounts.forEach { account ->
            compose.onNodeWithText(account.balance.formatWithSymbol()).assertExists()
        }
    }

    @Test
    fun theBackArrowInvokesOnBack() {
        var backs = 0
        compose.setThemedContent {
            AccountsScreen(accounts = SampleData.accounts, onBack = { backs++ })
        }

        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}
