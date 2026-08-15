@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.segmentedbutton.FormaSegmentedButton
import dev.formaui.components.segmentedbutton.FormaSegmentedButtonRow
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.amount.AmountField
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.converter.toMoneyOrZero
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/** Where a top-up comes from. The label is what the activity row will show as the counterparty. */
enum class TopUpSource(val label: String) {
    Card("Visa •••• 4821"),
    Bank("Bank transfer"),
    Linked("Linked account"),
}

/**
 * Add money to the account — the flow behind the Dashboard's "Add money" quick action, which until
 * now was a button wired to `{}`.
 *
 * The mirror image of the transfer flow and deliberately shorter: money coming in needs no recipient,
 * no rate and no fee, so it is one screen rather than two. There is also no insufficient-funds state
 * — the constraint on a top-up lives at the source, which this demo does not model.
 *
 * [onReceive] leads to the account details for someone else to pay in, which is the other half of
 * "add money" and the reason that screen exists.
 */
@Composable
fun AddMoneyScreen(
    amount: String,
    onAmountChange: (String) -> Unit,
    source: TopUpSource,
    onSourceChange: (TopUpSource) -> Unit,
    destination: Account,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onReceive: (() -> Unit)? = null,
) {
    val entered = amount.toMoneyOrZero(destination.balance.currency)

    FlowScreen(
        title = "Add money",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = onConfirm,
                enabled = entered.minorUnits > 0,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add ${entered.formatWithSymbol()}")
            }
            if (onReceive != null) {
                FormaButton(
                    onClick = onReceive,
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Get paid by someone else")
                }
            }
        },
    ) {
        Text("Where from", style = FormaTheme.typography.material.titleMedium)

        FormaSegmentedButtonRow(
            multiSelect = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TopUpSource.entries.forEachIndexed { index, candidate ->
                FormaSegmentedButton(
                    selected = candidate == source,
                    onClick = { onSourceChange(candidate) },
                    index = index,
                    count = TopUpSource.entries.size,
                    // The enum's label is the full counterparty string, too long for a segment.
                    label = { Text(candidate.name) },
                )
            }
        }

        Text("How much", style = FormaTheme.typography.material.titleMedium)

        AmountField(
            value = amount,
            onValueChange = onAmountChange,
            currency = destination.balance.currency,
            label = "Amount",
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Into ${destination.label} · ${destination.balance.formatWithSymbol()}",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "From ${source.label}. Instant, no fee.",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Add money · light", heightDp = 720)
@Composable
private fun AddMoneyScreenPreview() {
    FinTechTheme {
        AddMoneyScreen(
            amount = "200.00",
            onAmountChange = {},
            source = TopUpSource.Card,
            onSourceChange = {},
            destination = SampleData.primaryAccount,
            onBack = {},
            onConfirm = {},
            onReceive = {},
        )
    }
}

@Preview(name = "Add money · dark", heightDp = 720)
@Composable
private fun AddMoneyScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        AddMoneyScreen(
            amount = "",
            onAmountChange = {},
            source = TopUpSource.Bank,
            onSourceChange = {},
            destination = SampleData.primaryAccount,
            onBack = {},
            onConfirm = {},
            onReceive = {},
        )
    }
}
