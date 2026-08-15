/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.balance.CurrencyBalanceRow
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Every balance the user holds, one row per currency.
 *
 * This screen exists because the app already knew about balances it never showed. `SampleData` holds
 * four accounts — USD, EUR, GBP, JPY — and only the primary one appeared anywhere; `CurrencyBalanceRow`
 * was written, previewed, and rendered by nothing. Both were finished and invisible.
 *
 * **Rows are deliberately not clickable.** `CurrencyBalanceRow.onClick` is nullable and `null` is
 * passed, so a row is a readout with no ripple and no promise. There is no per-account screen to open,
 * and the Activity tab filters by category rather than currency, so it cannot be narrowed to one
 * balance either. A tappable row here would be four new dead ends on a screen built to remove them.
 *
 * No total is shown. Summing USD, EUR, GBP and JPY needs a conversion, and the honest figure depends
 * on a rate and a base currency this screen is not asking about — a fake sum is worse than none.
 */
@Composable
fun AccountsScreen(
    accounts: List<Account>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowScreen(
        title = "Balances",
        onBack = onBack,
        modifier = modifier,
    ) {
        Text(
            text = "You hold ${accounts.size} currencies. Money sent to you in any of them lands " +
                "in the matching balance.",
            style = FormaTheme.typography.material.bodyLarge,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )

        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            // Zero so the rows span the card like every other list in the app. These rows
            // are not clickable, so this is purely about matching the shared rhythm.
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child: FormaCard spaces its content slot by FormaUI's 16dp with no way to override
            // it, which would gap every row. The rows carry their own vertical padding.
            Column(modifier = Modifier.fillMaxWidth()) {
                accounts.forEach { account ->
                    CurrencyBalanceRow(account = account, onClick = null)
                }
            }
        }
    }
}

@Preview(name = "Accounts · light", heightDp = 640)
@Composable
private fun AccountsScreenPreview() {
    FinTechTheme {
        AccountsScreen(accounts = SampleData.accounts, onBack = {})
    }
}

@Preview(name = "Accounts · dark", heightDp = 640)
@Composable
private fun AccountsScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        AccountsScreen(accounts = SampleData.accounts, onBack = {})
    }
}
