/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalFoundationApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.chip.FormaChip
import dev.formaui.components.chip.FormaChipVariant
import dev.formaui.components.emptystate.FormaEmptyState
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.transaction.TransactionDateHeader
import dev.formaui.fintechuikit.components.transaction.TransactionRow
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.groupedByDate
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import java.time.LocalDate

/** The category filters offered above the list; null is the "All" chip. */
private val filters: List<SpendCategory?> = listOf(
    null,
    SpendCategory.Income,
    SpendCategory.Groceries,
    SpendCategory.Dining,
    SpendCategory.Transport,
    SpendCategory.Shopping,
)

/**
 * Transaction history: a search field, category filter chips, and a date-grouped list.
 *
 * Filtering lives in the screen (query + selected category), and the empty state shows when the
 * combination matches nothing — the demonstration point being that `FormaEmptyState` is the
 * right answer to a filtered-to-nothing list, not a blank screen.
 *
 * [searchVisible] is hoisted rather than owned here because the control that toggles it — the search
 * action — lives in the shell's app bar, not on this screen. Collapsed by default: the field used to
 * hold a permanent block of vertical space at the top of the list whether or not anyone was
 * searching, and the filter chips already cover the common case.
 *
 * Hiding the field deliberately does **not** clear [query]. A hidden field with a live filter would
 * silently show a filtered list with no visible reason, so the caller clears the query when it hides
 * the field.
 */
@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: SpendCategory?,
    onCategorySelected: (SpendCategory?) -> Unit,
    today: LocalDate,
    modifier: Modifier = Modifier,
    searchVisible: Boolean = true,
    onTransactionClick: (Transaction) -> Unit = {},
) {
    val filtered = transactions.filter { txn ->
        (selectedCategory == null || txn.category == selectedCategory) &&
            (query.isBlank() || txn.merchant.contains(query, ignoreCase = true))
    }
    val groups = filtered.groupedByDate(today)

    Column(modifier = modifier.fillMaxSize()) {
        // Fixed, above the list: search and the category chips are the controls *for* the list, so
        // they stay put while it moves. It also keeps the sticky date headers correct — they pin to
        // the top of the LazyColumn's viewport, which begins right below this block.
        Column(
            // No bottom padding on purpose. TransactionDateHeader already carries spacing.lg above
            // its label, so a bottom inset here stacks on top of it — 32dp between the filter chips
            // and the first group, double the 16dp that separates every later group. Dropping it
            // makes the first seam match the rest of the list's rhythm.
            Modifier
                .padding(
                    start = FinTechTheme.spacing.lg,
                    end = FinTechTheme.spacing.lg,
                    top = FinTechTheme.spacing.lg,
                    bottom = 0.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        ) {
            // No heading here: the shell's app bar already titles this tab "Activity".
            if (searchVisible) {
                FormaTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Search transactions",
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
            ) {
                filters.forEach { category ->
                    FormaChip(
                        label = category?.displayName ?: "All",
                        onClick = { onCategorySelected(category) },
                        variant = FormaChipVariant.Filter,
                        selected = selectedCategory == category,
                    )
                }
            }
        }

        if (groups.isEmpty()) {
            FormaEmptyState(
                title = "No transactions found",
                description = "Try a different search or filter.",
                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FinTechTheme.spacing.section),
            )
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                groups.forEach { (label, rows) ->
                    stickyHeader(key = "header-$label") {
                        // Opaque background so rows scrolling under the pinned header don't bleed
                        // through — matches the sage/ink the headers already sit on.
                        TransactionDateHeader(
                            label,
                            modifier = Modifier.background(FormaTheme.colorScheme.background),
                        )
                    }
                    items(rows, key = { it.id }) { txn ->
                        // The list already had stable keys, which is the prerequisite. Without this
                        // a newly-sent transfer appears at the top between frames, and changing the
                        // filter snaps rows to new positions.
                        TransactionRow(
                            txn,
                            onClick = { onTransactionClick(txn) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Transactions · light", heightDp = 1200)
@Composable
private fun TransactionsScreenPreview() {
    FinTechTheme {
        TransactionsScreen(
            transactions = SampleData.transactions,
            query = "",
            onQueryChange = {},
            selectedCategory = null,
            onCategorySelected = {},
            today = SampleData.today,
        )
    }
}

@Preview(name = "Transactions · empty", heightDp = 800)
@Composable
private fun TransactionsScreenEmptyPreview() {
    FinTechTheme {
        TransactionsScreen(
            transactions = SampleData.transactions,
            query = "nonexistent",
            onQueryChange = {},
            selectedCategory = null,
            onCategorySelected = {},
            today = SampleData.today,
        )
    }
}
