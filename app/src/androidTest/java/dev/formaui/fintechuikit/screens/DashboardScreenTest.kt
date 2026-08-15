/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.formatAmount
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The home screen. It owns no state and navigates nowhere itself — every control is a hoisted
 * callback — so what is worth testing is that each one is wired to its own destination and not to a
 * neighbour's. Three quick actions sitting in a row is exactly the shape that invites a copy-paste
 * mix-up, and the compiler cannot catch one.
 */
class DashboardScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTheBalanceAndItsTrend() {
        compose.showDashboard()

        // `formatAmount`, not `formatWithSymbol`: the hero card draws the currency symbol as its own
        // Text at a smaller style, so the two never merge into one "$12,480.65" node.
        compose.onNodeWithText(SampleData.primaryAccount.balance.formatAmount()).assertExists()
        compose.onNodeWithText("+4.2% this month").assertExists()
    }

    @Test
    fun sendIsWiredToItsOwnAction() {
        var fired = ""
        compose.showDashboard(
            onSendClick = { fired = "send" },
            onAddMoneyClick = { fired = "add" },
            onRequestClick = { fired = "request" },
        )

        compose.onNodeWithText("Send").performClick()

        assertEquals("send", fired)
    }

    @Test
    fun addMoneyIsWiredToItsOwnAction() {
        var fired = ""
        compose.showDashboard(
            onSendClick = { fired = "send" },
            onAddMoneyClick = { fired = "add" },
            onRequestClick = { fired = "request" },
        )

        compose.onNodeWithText("Add money").performClick()

        assertEquals("add", fired)
    }

    @Test
    fun requestIsWiredToItsOwnAction() {
        var fired = ""
        compose.showDashboard(
            onSendClick = { fired = "send" },
            onAddMoneyClick = { fired = "add" },
            onRequestClick = { fired = "request" },
        )

        compose.onNodeWithText("Request").performClick()

        assertEquals("request", fired)
    }

    /**
     * The balance card's `actions` slot went unused for a long time; this pins the link that now
     * lives in it, which is the only route to the other three currencies from home.
     */
    @Test
    fun viewAllBalancesLeadsToTheAccountList() {
        var opened = false
        compose.showDashboard(onViewBalances = { opened = true })

        compose.onNodeWithText("View all balances").performClick()

        assertEquals(true, opened)
    }

    @Test
    fun seeAllLeadsToTheActivityTab() {
        var opened = false
        compose.showDashboard(onSeeAllTransactions = { opened = true })

        compose.onNodeWithText("See all").performScrollTo().performClick()

        assertEquals(true, opened)
    }

    @Test
    fun recentRowsCarryTheirOwnTransaction() {
        var tapped: Transaction? = null
        compose.showDashboard(onTransactionClick = { tapped = it })

        compose.onNodeWithText("Blue Bottle Coffee").performScrollTo().performClick()

        assertEquals("t2", tapped?.id)
    }
}

private fun ComposeContentTestRule.showDashboard(
    onSendClick: () -> Unit = {},
    onAddMoneyClick: () -> Unit = {},
    onRequestClick: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onViewBalances: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
) {
    setThemedContent {
        DashboardScreen(
            account = SampleData.primaryAccount,
            recentTransactions = SampleData.recentTransactions,
            spendByCategory = SampleData.spendByCategory,
            balanceTrend = SampleData.balanceTrend,
            onSendClick = onSendClick,
            onAddMoneyClick = onAddMoneyClick,
            onRequestClick = onRequestClick,
            onSeeAllTransactions = onSeeAllTransactions,
            onViewBalances = onViewBalances,
            onTransactionClick = onTransactionClick,
        )
    }
}
