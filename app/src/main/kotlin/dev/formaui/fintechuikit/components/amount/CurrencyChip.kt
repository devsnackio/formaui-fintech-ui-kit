/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.amount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.chip.FormaChip
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The currency pill — flag and code, optionally with a chevron.
 *
 * The chip serves two different jobs and the chevron is what separates them. On a text field it is a
 * **button that opens a picker**, and the chevron is the only thing saying so. Inside that picker it
 * is an **option to choose**, where a chevron actively misleads: it promises a further menu that does
 * not exist, and tapping selects rather than expands. Hence [opensPicker] rather than a `showChevron`
 * flag — the caller states what the chip *is*, and the affordance follows.
 */
@Composable
fun CurrencyChip(
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    opensPicker: Boolean = true,
) {
    FormaChip(
        label = "${currency.flag}  ${currency.code}",
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        trailingIcon = if (opensPicker) {
            {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    // The chip's own label names the currency; this only signals "opens a picker",
                    // which the Role.Button semantics already convey.
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            null
        },
    )
}

@Preview
@Composable
private fun CurrencyChipPreview() {
    FinTechTheme {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        ) {
            // Top row: on a field, where the chevron says "tap to change".
            Row(horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
                CurrencyChip(Currency.USD, onClick = {})
                CurrencyChip(Currency.EUR, onClick = {})
                CurrencyChip(Currency.JPY, onClick = {})
            }
            // Bottom row: inside the picker, where each chip is an option and there is nothing
            // further to expand.
            Row(horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
                CurrencyChip(Currency.USD, onClick = {}, opensPicker = false)
                CurrencyChip(Currency.EUR, onClick = {}, opensPicker = false)
                CurrencyChip(Currency.JPY, onClick = {}, opensPicker = false, enabled = false)
            }
        }
    }
}
