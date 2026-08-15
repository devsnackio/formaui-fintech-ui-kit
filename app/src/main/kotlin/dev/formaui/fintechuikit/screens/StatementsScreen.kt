/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalMaterial3Api::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.datepicker.FormaDateRangePickerSheet
import dev.formaui.components.divider.FormaDivider
import dev.formaui.components.emptystate.FormaEmptyState
import dev.formaui.components.slider.FormaRangeSlider
import dev.formaui.components.tabs.FormaTab
import dev.formaui.components.tabs.FormaTabRow
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.common.rememberCopyToClipboard
import dev.formaui.fintechuikit.components.transaction.TransactionRow
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.StatementDirection
import dev.formaui.fintechuikit.data.asPlainTextStatement
import dev.formaui.fintechuikit.data.inAmountRange
import dev.formaui.fintechuikit.data.inDateRange
import dev.formaui.fintechuikit.data.inDirection
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.data.statementTotals
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** The widest amount the slider offers, in minor units. Above this the filter is "no upper bound". */
private const val MaxAmountMinor = 500_000f

/**
 * A statement for a date range: what came in, what went out, and what it nets to.
 *
 * This answers the one question the Activity tab cannot. Activity filters by category and by a text
 * query — never by date — even though every `Transaction` has carried a `date` since the model was
 * written. "What did I spend in July?" had no answer in the app.
 *
 * All the filtering and the totalling live in `data/Statements.kt` as plain functions, unit-tested
 * there. That is deliberate: both edges here fail *silently*. An exclusive date boundary quietly
 * drops the last day of every statement, and money compared as a float drifts. Neither shows up in a
 * screenshot, so neither belongs in a composable.
 *
 * State is fully hoisted so the screen stays previewable in every combination, including the two
 * that matter most — nothing in range, and nothing at all.
 */
@Composable
fun StatementsScreen(
    transactions: List<Transaction>,
    startDate: LocalDate,
    endDate: LocalDate,
    onRangeChange: (LocalDate, LocalDate) -> Unit,
    direction: StatementDirection,
    onDirectionChange: (StatementDirection) -> Unit,
    amountRange: ClosedFloatingPointRange<Float>,
    onAmountRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    currency: Currency = Currency.USD,
) {
    var pickingRange by remember { mutableStateOf(false) }
    val copy = rememberCopyToClipboard("Statement")

    // The slider hands back Float; it is turned into Long here and every comparison downstream
    // happens in integer minor units. Money itself never touches the float.
    val filtered = transactions
        .inDateRange(startDate, endDate)
        .inDirection(direction)
        .inAmountRange(
            minMinor = amountRange.start.toLong(),
            maxMinor = amountRange.endInclusive.toLong(),
        )
    val totals = filtered.statementTotals(currency)

    FlowScreen(
        title = "Statements",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = {
                    copy(filtered.asPlainTextStatement(startDate, endDate, currency))
                },
                enabled = filtered.isNotEmpty(),
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Text("  Copy statement")
            }
        },
    ) {
        FormaButton(
            onClick = { pickingRange = true },
            variant = FormaButtonVariant.Outlined,
            shape = BrandDefaults.buttonShape,
            contentPadding = BrandDefaults.buttonContentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.DateRange, contentDescription = null)
            Text("  $startDate  →  $endDate")
        }

        // A tab row rather than chips: Activity already uses chips for category, and these three are
        // mutually exclusive views of one list rather than filters that combine.
        FormaTabRow(selectedTabIndex = StatementDirection.entries.indexOf(direction)) {
            StatementDirection.entries.forEach { candidate ->
                FormaTab(
                    selected = candidate == direction,
                    onClick = { onDirectionChange(candidate) },
                    text = { Text(candidate.label) },
                )
            }
        }

        TotalsCard(totals.moneyIn, totals.moneyOut, totals.net)

        AmountFilter(
            range = amountRange,
            onRangeChange = onAmountRangeChange,
            currency = currency,
        )

        if (filtered.isEmpty()) {
            FormaEmptyState(
                title = "Nothing in this range",
                description = "Try a wider date range, or widen the amount filter.",
                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            FormaCard(
                variant = FormaCardVariant.Filled,
                shape = BrandDefaults.cardShape,
                // Zero: the rows are full-width tap targets and the card's 24dp inset would stop
                // the ripple short of its edges.
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // One child: FormaCard's 16dp content arrangement cannot be overridden.
                Column(modifier = Modifier.fillMaxWidth()) {
                    filtered.sortedByDescending { it.date }.forEach { txn ->
                        // Tappable, like the same row is on Dashboard and Activity. A statement row
                        // that looked identical but did nothing would be the odd one out.
                        TransactionRow(txn, onClick = { onTransactionClick(txn) })
                    }
                }
            }
        }
    }

    if (pickingRange) {
        val pickerState = rememberDateRangePickerState()
        FormaDateRangePickerSheet(
            onDismissRequest = { pickingRange = false },
            state = pickerState,
            confirmButton = {
                val start = pickerState.selectedStartDateMillis
                val end = pickerState.selectedEndDateMillis
                FormaButton(
                    onClick = {
                        if (start != null && end != null) {
                            onRangeChange(start.toUtcLocalDate(), end.toUtcLocalDate())
                        }
                        pickingRange = false
                    },
                    // The sheet wires nothing to this slot, so the slot owns the disabled state too.
                    enabled = start != null && end != null,
                    shape = BrandDefaults.buttonShape,
                    contentPadding = BrandDefaults.buttonContentPadding,
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                FormaButton(
                    onClick = { pickingRange = false },
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Converts the picker's epoch millis to a date **in UTC**, which is what Material 3 hands back.
 *
 * Using the system zone here is the classic off-by-one-day bug: east of UTC the picked day becomes
 * the day before, so a statement silently loses or gains a day's transactions at the boundary. The
 * reference documents these as UTC millis, so UTC is what they are read as.
 */
private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun TotalsCard(moneyIn: Money, moneyOut: Money, net: Money) {
    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // One child: FormaCard's 16dp content arrangement cannot be overridden.
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {
            TotalLine("Money in", moneyIn, FinTechTheme.brandColors.positive)
            TotalLine("Money out", moneyOut, FormaTheme.colorScheme.onSurface)
            FormaDivider()
            TotalLine("Net", net, FormaTheme.colorScheme.onSurface, emphasise = true)
        }
    }
}

@Composable
private fun TotalLine(
    label: String,
    amount: Money,
    color: Color,
    emphasise: Boolean = false,
) {
    val slot = if (emphasise) {
        FormaTheme.typography.material.titleMedium
    } else {
        FormaTheme.typography.material.bodyMedium
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = slot, color = FormaTheme.colorScheme.onSurfaceVariant)
        Text(
            // Only the net carries a sign — "Money out" already says the direction in words, and a
            // minus beside it reads as a double negative.
            text = amount.formatWithSymbol(signed = emphasise),
            style = FormaTheme.typography.numeric.merge(slot),
            color = color,
        )
    }
}

@Composable
private fun AmountFilter(
    range: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    currency: Currency,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Amount", style = FormaTheme.typography.material.titleSmall)
            Text(
                text = "${Money(range.start.toLong(), currency).formatWithSymbol()} – " +
                    Money(range.endInclusive.toLong(), currency).formatWithSymbol(),
                style = FormaTheme.typography.numeric.merge(
                    FormaTheme.typography.material.bodySmall,
                ),
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
        }
        FormaRangeSlider(
            value = range,
            onValueChange = onRangeChange,
            valueRange = 0f..MaxAmountMinor,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatementsPreview(
    direction: StatementDirection = StatementDirection.All,
    range: ClosedFloatingPointRange<Float> = 0f..MaxAmountMinor,
    dark: Boolean = false,
    start: LocalDate = SampleData.today.minusDays(7),
) {
    FinTechTheme(darkTheme = dark) {
        StatementsScreen(
            transactions = SampleData.transactions,
            startDate = start,
            endDate = SampleData.today,
            onRangeChange = { _, _ -> },
            direction = direction,
            onDirectionChange = {},
            amountRange = range,
            onAmountRangeChange = {},
            onTransactionClick = {},
            onBack = {},
        )
    }
}

@Preview(name = "Statements · all", heightDp = 1100)
@Composable
private fun StatementsAllPreview() = StatementsPreview()

@Preview(name = "Statements · money in", heightDp = 900)
@Composable
private fun StatementsInPreview() = StatementsPreview(direction = StatementDirection.In)

@Preview(name = "Statements · dark", heightDp = 1100)
@Composable
private fun StatementsDarkPreview() = StatementsPreview(dark = true)

/** Filters that match nothing — the state the empty copy exists for. */
@Preview(name = "Statements · nothing in range", heightDp = 800)
@Composable
private fun StatementsEmptyPreview() =
    StatementsPreview(range = 400_000f..MaxAmountMinor)
