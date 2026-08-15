/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.amount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A currency amount entry field.
 *
 * State is fully hoisted as a raw [String] rather than a parsed number: a partially-typed
 * amount ("12.", "0.0") is not yet a valid value, and forcing it through a numeric type would
 * either reject the keystroke or silently rewrite what the user typed mid-entry.
 *
 * The field renders in `typography.numeric` so digits stay tabular, and no shape is passed —
 * FormaUI's default already resolves to the brand's 12dp input radius.
 *
 * [trailing] is inset from the field's end edge rather than dropped straight into M3's trailing slot.
 * That slot is spaced for a 24dp icon, whose 48dp touch target supplies the visual gap for free; the
 * thing actually passed here is a [CurrencyChip], a much wider pill with its own filled background,
 * and at the default spacing its edge sits almost on the outline stroke. An icon can afford to sit
 * near the border because it is mostly transparent — a filled pill cannot.
 */
@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currency: Currency,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    FormaTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.filterAmountInput(currency)) },
        modifier = modifier.fillMaxWidth(),
        label = label,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        errorText = errorText,
        singleLine = true,
        textStyle = FormaTheme.typography.numeric,
        placeholder = "0${if (currency.decimalPlaces > 0) ".00" else ""}",
        leadingIcon = {
            Text(currency.symbol, style = FormaTheme.typography.material.titleMedium)
        },
        trailingIcon = trailing?.let { content ->
            {
                Box(Modifier.padding(end = FinTechTheme.spacing.sm)) {
                    content()
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

/**
 * Keeps input to something that can become an amount: digits, at most one separator, and no
 * more fraction digits than the currency actually has.
 *
 * Filtering on the way in beats validating on submit — the user never gets to type something
 * that will later be rejected, and a zero-decimal currency like JPY cannot accept a decimal
 * point at all.
 */
internal fun String.filterAmountInput(currency: Currency): String {
    val cleaned = filter { it.isDigit() || it == '.' }
    if (currency.decimalPlaces == 0) return cleaned.filter { it.isDigit() }

    val firstDot = cleaned.indexOf('.')
    if (firstDot < 0) return cleaned

    val whole = cleaned.substring(0, firstDot)
    val fraction = cleaned.substring(firstDot + 1)
        .filter { it.isDigit() }
        .take(currency.decimalPlaces)
    return "$whole.$fraction"
}

@Preview
@Composable
private fun AmountFieldPreview() {
    FinTechTheme {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            AmountField(value = "1240.00", onValueChange = {}, currency = Currency.USD, label = "You send")
            AmountField(value = "", onValueChange = {}, currency = Currency.EUR, label = "Recipient gets")
            AmountField(
                value = "9999999.00",
                onValueChange = {},
                currency = Currency.USD,
                label = "Amount",
                isError = true,
                errorText = "Exceeds your daily limit",
            )
            // With the trailing chip — the arrangement the converter card actually uses, and the only
            // one where the end inset is visible. Without this the previews never showed it.
            AmountField(
                value = "1240.00",
                onValueChange = {},
                currency = Currency.USD,
                label = "You send",
                trailing = { CurrencyChip(Currency.USD, onClick = {}) },
            )
        }
    }
}
