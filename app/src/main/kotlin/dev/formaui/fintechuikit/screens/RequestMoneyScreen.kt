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
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.button.FormaButton
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.components.radiobutton.FormaRadioButton
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.amount.AmountField
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.converter.toMoneyOrZero
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Ask someone for money — the flow behind the Dashboard's "Request" quick action, previously a
 * button wired to `{}`.
 *
 * What it creates is a **Pending transaction**, not a new kind of object. A request is money you
 * expect but do not have, which is exactly what `TransactionStatus.Pending` already means here, so
 * the created request drops into the activity list, groups by date and gets a `StatusPill` with no
 * new plumbing. Before this, `Pending` appeared only on a hardcoded sample row and nothing the user
 * did could produce one.
 *
 * The note is optional but prompted for. "What's it for?" is the difference between a request the
 * other person recognises and one they ignore, and it costs one field.
 */
@Composable
fun RequestMoneyScreen(
    amount: String,
    onAmountChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    recipients: List<Recipient>,
    selectedRecipient: Recipient?,
    onRecipientSelected: (Recipient) -> Unit,
    account: Account,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entered = amount.toMoneyOrZero(account.balance.currency)

    FlowScreen(
        title = "Request money",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = onCreate,
                enabled = selectedRecipient != null && entered.minorUnits > 0,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Request ${entered.formatWithSymbol()}")
            }
        },
    ) {
        Text("From", style = FormaTheme.typography.material.titleMedium)

        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            // Zero: the payee rows are the tap targets and must reach the card edges.
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child — FormaCard's 16dp content arrangement cannot be overridden.
            Column(modifier = Modifier.fillMaxWidth()) {
                recipients.forEach { recipient ->
                    FormaListItem(
                        headline = recipient.name,
                        supporting = recipient.handle,
                        onClick = { onRecipientSelected(recipient) },
                        leading = {
                            FormaAvatar(
                                initials = recipient.initials,
                                size = FormaAvatarSize.Medium,
                            )
                        },
                        trailing = {
                            // Row owns the click and the semantics; the control is display-only.
                            FormaRadioButton(
                                selected = recipient == selectedRecipient,
                                onClick = null,
                            )
                        },
                        headlineTextStyle = FormaTheme.typography.material.bodyMedium,
                        supportingTextStyle = FormaTheme.typography.material.bodySmall,
                    )
                }
            }
        }

        Text("How much", style = FormaTheme.typography.material.titleMedium)

        AmountField(
            value = amount,
            onValueChange = onAmountChange,
            currency = account.balance.currency,
            label = "Amount",
            modifier = Modifier.fillMaxWidth(),
        )

        FormaTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = "What's it for?",
            placeholder = "Dinner on Friday",
            singleLine = true,
        )

        Text(
            text = "They'll get a link to pay you. It shows in your activity as pending until " +
                "they do.",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Request money · light", heightDp = 900)
@Composable
private fun RequestMoneyScreenPreview() {
    FinTechTheme {
        RequestMoneyScreen(
            amount = "75.00",
            onAmountChange = {},
            note = "Dinner on Friday",
            onNoteChange = {},
            recipients = SampleData.recipients,
            selectedRecipient = SampleData.recipients.first(),
            onRecipientSelected = {},
            account = SampleData.primaryAccount,
            onBack = {},
            onCreate = {},
        )
    }
}

@Preview(name = "Request money · dark", heightDp = 900)
@Composable
private fun RequestMoneyScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        RequestMoneyScreen(
            amount = "",
            onAmountChange = {},
            note = "",
            onNoteChange = {},
            recipients = SampleData.recipients,
            selectedRecipient = null,
            onRecipientSelected = {},
            account = SampleData.primaryAccount,
            onBack = {},
            onCreate = {},
        )
    }
}
