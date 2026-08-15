/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.bottomsheet.FormaBottomSheet
import dev.formaui.components.button.FormaButton
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.amount.CurrencyChip
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.converter.CurrencyConverterCard
import dev.formaui.fintechuikit.components.converter.toMoneyOrZero
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.canCover
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.ExchangeRate
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Step 1 of the transfer: how much, and in which currencies.
 *
 * The flow is two destinations — amount here, recipient next — with the review kept as a
 * [FormaBottomSheet] on the recipient step. Splitting amount from recipient gives each step one
 * question and lets the amount survive a back-press, but the review stays a sheet: it is a momentary
 * confirmation of what the user just assembled, not somewhere they navigate to.
 *
 * Three affordances on [CurrencyConverterCard] used to be dead — the source chip, the target chip and
 * the swap button all defaulted to `{}` and this screen forwarded none of them, so a filled
 * CTA-green swap button sat there doing nothing. They now open a currency picker and flip the pair.
 * That is only possible because `SampleData.rateFor` resolves any pair rather than just USD→EUR.
 *
 * The insufficient-funds error is the other gap closed here: `AmountField` has carried `isError` and
 * `errorText` since it was written and nothing ever passed them, so the flow would happily review a
 * transfer larger than the balance.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransferScreen(
    sendAmount: String,
    onSendAmountChange: (String) -> Unit,
    rate: ExchangeRate,
    fundingAccount: Account?,
    onSourceCurrencyChange: (Currency) -> Unit,
    onTargetCurrencyChange: (Currency) -> Unit,
    onSwapCurrencies: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Which chip opened the picker, or null when it is closed. One nullable beats two booleans that
    // could both be true.
    var picking by remember { mutableStateOf<CurrencySlot?>(null) }

    val send = sendAmount.toMoneyOrZero(rate.from)
    val total = send + rate.feeOn(send)
    val enteredSomething = send.minorUnits > 0
    // Checked against the fee-inclusive total: a transfer that clears the balance but not the fee is
    // still one the account cannot fund.
    val shortOfFunds = enteredSomething && fundingAccount?.canCover(total) == false

    FlowScreen(
        title = "Send money",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = onContinue,
                enabled = enteredSomething && !shortOfFunds,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Choose recipient")
            }
        },
    ) {
        CurrencyConverterCard(
            sendAmount = sendAmount,
            onSendAmountChange = onSendAmountChange,
            rate = rate,
            onSourceCurrencyClick = { picking = CurrencySlot.Source },
            onTargetCurrencyClick = { picking = CurrencySlot.Target },
            onSwap = onSwapCurrencies,
            isError = shortOfFunds,
            // Names the shortfall rather than just refusing: the fee is part of what has to clear,
            // and an amount that looks affordable can still fail on the total.
            errorText = fundingAccount?.let {
                "More than your ${it.balance.formatWithSymbol()} balance, including the fee"
            },
        )

        // The funding line stays visible when there is no error, so the account being debited is
        // never a surprise at the review step.
        if (fundingAccount != null && !shortOfFunds) {
            Text(
                text = "From ${fundingAccount.label} · ${fundingAccount.balance.formatWithSymbol()}",
                style = FormaTheme.typography.material.bodySmall,
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    picking?.let { slot ->
        CurrencyPickerSheet(
            selected = if (slot == CurrencySlot.Source) rate.from else rate.to,
            // Offer only what the demo can price. Listing a currency that resolves to no rate would
            // put the flow in a state it cannot show a conversion for.
            options = Currency.entries.filter { candidate ->
                when (slot) {
                    CurrencySlot.Source -> SampleData.rateFor(candidate, rate.to) != null
                    CurrencySlot.Target -> SampleData.rateFor(rate.from, candidate) != null
                }
            },
            onSelect = { currency ->
                when (slot) {
                    CurrencySlot.Source -> onSourceCurrencyChange(currency)
                    CurrencySlot.Target -> onTargetCurrencyChange(currency)
                }
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
}

/** Which side of the converter the picker was opened from. */
private enum class CurrencySlot { Source, Target }

/**
 * The currency picker, built from [CurrencyChip] — a component that existed, fully written and
 * previewed, and was rendered by no screen in the app until now.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrencyPickerSheet(
    selected: Currency,
    options: List<Currency>,
    onSelect: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    FormaBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        ) {
            Text("Choose currency", style = FormaTheme.typography.material.headlineSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
            ) {
                options.forEach { currency ->
                    CurrencyChip(
                        currency = currency,
                        onClick = { onSelect(currency) },
                        // Already-chosen currency is inert rather than hidden, so the sheet shows
                        // where you are as well as where you can go.
                        enabled = currency != selected,
                        // No chevron in here: these are the options, not a way to open more.
                        opensPicker = false,
                    )
                }
            }
        }
    }
}

@Preview(name = "Transfer · light", heightDp = 800)
@Composable
private fun TransferScreenPreview() {
    FinTechTheme {
        TransferScreen(
            sendAmount = "1000.00",
            onSendAmountChange = {},
            rate = SampleData.usdToEur,
            fundingAccount = SampleData.primaryAccount,
            onSourceCurrencyChange = {},
            onTargetCurrencyChange = {},
            onSwapCurrencies = {},
            onBack = {},
            onContinue = {},
        )
    }
}

@Preview(name = "Transfer · dark", heightDp = 800)
@Composable
private fun TransferScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        TransferScreen(
            sendAmount = "1000.00",
            onSendAmountChange = {},
            rate = SampleData.usdToEur,
            fundingAccount = SampleData.primaryAccount,
            onSourceCurrencyChange = {},
            onTargetCurrencyChange = {},
            onSwapCurrencies = {},
            onBack = {},
            onContinue = {},
        )
    }
}

/** The state the CTA guards against: more than the account holds, once the fee is counted. */
@Preview(name = "Transfer · insufficient funds", heightDp = 800)
@Composable
private fun TransferScreenShortPreview() {
    FinTechTheme {
        TransferScreen(
            sendAmount = "99000.00",
            onSendAmountChange = {},
            rate = SampleData.usdToEur,
            fundingAccount = SampleData.primaryAccount,
            onSourceCurrencyChange = {},
            onTargetCurrencyChange = {},
            onSwapCurrencies = {},
            onBack = {},
            onContinue = {},
        )
    }
}
