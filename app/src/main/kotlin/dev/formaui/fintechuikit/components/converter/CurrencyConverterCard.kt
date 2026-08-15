@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.converter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.divider.FormaDivider
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.components.iconbutton.FormaIconButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.amount.AmountField
import dev.formaui.fintechuikit.components.amount.CurrencyChip
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.ExchangeRate
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The brand's signature interactive component.
 *
 * DESIGN.md specifies it precisely: white surface, **1px solid ink border**, `{spacing.xl}`
 * padding, `{rounded.xl}` radius. The ink hairline is the doc's "Level 1" elevation and is why
 * this uses an explicit border rather than `FormaCardVariant.Outlined`, whose M3 default
 * outline colour is a lighter tone.
 *
 * The fee is shown as its own line rather than folded into the rate — the brand's whole
 * positioning is that the rate is the real mid-market one and the fee is stated openly.
 *
 * [isError]/[errorText] attach to the **"You send"** field only. That is the field the user typed in,
 * so it is where a problem with the amount belongs; "Recipient gets" is computed and cannot be wrong
 * on its own. `AmountField` has always accepted these — nothing passed them until the transfer flow
 * needed an insufficient-funds state.
 */
@Composable
fun CurrencyConverterCard(
    sendAmount: String,
    onSendAmountChange: (String) -> Unit,
    rate: ExchangeRate,
    modifier: Modifier = Modifier,
    onSourceCurrencyClick: () -> Unit = {},
    onTargetCurrencyClick: () -> Unit = {},
    onSwap: () -> Unit = {},
    isError: Boolean = false,
    errorText: String? = null,
) {
    val sendMoney = sendAmount.toMoneyOrZero(rate.from)
    val fee = rate.feeOn(sendMoney)
    val received = rate.convert(sendMoney)

    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FormaTheme.colorScheme.outline, BrandDefaults.cardShape),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {

            AmountField(
                value = sendAmount,
                onValueChange = onSendAmountChange,
                currency = rate.from,
                label = "You send",
                isError = isError,
                errorText = errorText,
                trailing = { CurrencyChip(rate.from, onClick = onSourceCurrencyClick) },
            )

            // The swap control sits on the divider between the two fields, which is what makes
            // the card read as one exchange rather than two unrelated inputs.
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FormaDivider()
                FormaIconButton(
                    onClick = onSwap,
                    variant = FormaIconButtonVariant.Filled,
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = "Swap send and receive currencies",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            AmountField(
                value = received.formatPlain(),
                onValueChange = {},
                currency = rate.to,
                label = "Recipient gets",
                readOnly = true,
                trailing = { CurrencyChip(rate.to, onClick = onTargetCurrencyClick) },
            )

            Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
                BreakdownLine("Fee", fee.formatWithSymbol())
                BreakdownLine("Rate", rate.displayLine)
            }
        }
    }
}

@Composable
private fun BreakdownLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = FormaTheme.typography.numeric.merge(FormaTheme.typography.material.bodySmall))
    }
}

/**
 * Parses typed text into [Money]. Anything unparseable is zero rather than an exception —
 * mid-typing states like "" or "12." reach here on every keystroke.
 */
internal fun String.toMoneyOrZero(currency: Currency): Money {
    val parsed = toBigDecimalOrNull() ?: return Money.zero(currency)
    return Money(parsed.movePointRight(currency.decimalPlaces).toLong(), currency)
}

/** Renders an amount for a read-only field: digits only, no symbol (the chip carries that). */
private fun Money.formatPlain(): String =
    decimal.setScale(currency.decimalPlaces).toPlainString()

@Preview(name = "Converter · light", heightDp = 520)
@Composable
private fun CurrencyConverterCardPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            CurrencyConverterCard(
                sendAmount = "1000.00",
                onSendAmountChange = {},
                rate = SampleData.usdToEur,
            )
        }
    }
}

@Preview(name = "Converter · dark", heightDp = 520)
@Composable
private fun CurrencyConverterCardDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            CurrencyConverterCard(
                sendAmount = "1000.00",
                onSendAmountChange = {},
                rate = SampleData.usdToEur,
            )
        }
    }
}
