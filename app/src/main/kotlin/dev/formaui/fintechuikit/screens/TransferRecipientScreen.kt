@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.bottomsheet.FormaBottomSheet
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.divider.FormaDivider
import dev.formaui.components.emptystate.FormaEmptyState
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.components.radiobutton.FormaRadioButton
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.converter.toMoneyOrZero
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.ExchangeRate
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Step 2 of the transfer: who it goes to, then the review sheet.
 *
 * Three things the old single-screen version did not do:
 *
 *  - **Search.** Four sample payees fit on a screen; a real payee list does not, and a send flow
 *    without search is the first thing to break at realistic scale.
 *  - **A real selection control.** Selection used to be the literal word "Selected" as trailing
 *    text, which a screen reader announced as content rather than as state. A
 *    [FormaRadioButton] with `onClick = null` inside a clickable row is FormaUI's documented
 *    pattern: one target, announced once, with the selected state carried in semantics.
 *  - **Avatars.** `Recipient.initials` has existed since the model was written and no screen used it.
 *
 * The review stays a [FormaBottomSheet], as it always was — it confirms what the user has already
 * assembled rather than asking anything new, so it is an interruption, not a destination.
 */
@Composable
fun TransferRecipientScreen(
    sendAmount: String,
    rate: ExchangeRate,
    recipients: List<Recipient>,
    selectedRecipient: Recipient?,
    onRecipientSelected: (Recipient) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var reviewing by remember { mutableStateOf(false) }

    val matches = recipients.filter { recipient ->
        query.isBlank() ||
            recipient.name.contains(query, ignoreCase = true) ||
            recipient.handle.contains(query, ignoreCase = true)
    }

    FlowScreen(
        title = "Choose recipient",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = { reviewing = true },
                enabled = selectedRecipient != null,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Review transfer")
            }
        },
    ) {
        FormaTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Search name or handle",
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        )

        if (matches.isEmpty()) {
            FormaEmptyState(
                title = "No matching recipients",
                description = "Check the spelling, or clear the search to see everyone.",
                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            FormaCard(
                variant = FormaCardVariant.Filled,
                shape = BrandDefaults.cardShape,
                // Zero: the recipient rows are the tap targets and must reach the card
                // edges, or the ripple stops short and the row reads as partly clickable.
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // One child — FormaCard spaces its slot by FormaUI's 16dp with no way to override it.
                Column(modifier = Modifier.fillMaxWidth()) {
                    matches.forEach { recipient ->
                        FormaListItem(
                            headline = recipient.name,
                            supporting = "${recipient.handle} · ${recipient.currency.code}",
                            onClick = { onRecipientSelected(recipient) },
                            leading = {
                                FormaAvatar(
                                    initials = recipient.initials,
                                    size = FormaAvatarSize.Medium,
                                )
                            },
                            trailing = {
                                // null callback: the row owns the click and the semantics, so the
                                // control is display-only and is not a second target.
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
        }
    }

    if (reviewing && selectedRecipient != null) {
        TransferReviewSheet(
            sendAmount = sendAmount,
            rate = rate,
            recipient = selectedRecipient,
            onDismiss = { reviewing = false },
            onConfirm = {
                reviewing = false
                onConfirm()
            },
        )
    }
}

/**
 * The review: what leaves, what it costs, what arrives.
 *
 * Two lines the old sheet did not show. **Total debited** is the number that actually leaves the
 * account, which is the amount plus the fee — the one figure a user checks against their balance and
 * the only one the previous sheet made them add up themselves. **Rate** is shown because the brand's
 * entire positioning is that the rate is the real one, so hiding it here would undercut the fee line
 * sitting right above it.
 */
@Composable
private fun TransferReviewSheet(
    sendAmount: String,
    rate: ExchangeRate,
    recipient: Recipient,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val send = sendAmount.toMoneyOrZero(rate.from)
    val fee = rate.feeOn(send)
    val received = rate.convert(send)

    FormaBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        ) {
            Text("Review transfer", style = FormaTheme.typography.material.headlineSmall)
            ReviewLine("To", recipient.name)
            ReviewLine("Handle", recipient.handle)
            ReviewLine("You send", send.formatWithSymbol())
            ReviewLine("Fee", fee.formatWithSymbol())
            ReviewLine("Rate", rate.displayLine)
            FormaDivider()
            ReviewLine("Total debited", (send + fee).formatWithSymbol())
            ReviewLine("They receive", received.formatWithSymbol(), emphasise = true)

            FormaButton(
                onClick = onConfirm,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth().padding(top = FinTechTheme.spacing.sm),
            ) {
                Text("Confirm and send")
            }
            FormaButton(
                onClick = onDismiss,
                variant = FormaButtonVariant.Text,
                colors = BrandDefaults.textButtonColors,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ReviewLine(label: String, value: String, emphasise: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = if (emphasise) FormaTheme.typography.material.titleMedium
            else FormaTheme.typography.material.bodyMedium,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = FormaTheme.typography.numeric.merge(
                if (emphasise) FormaTheme.typography.material.titleMedium
                else FormaTheme.typography.material.bodyMedium,
            ),
        )
    }
}

@Preview(name = "Transfer recipient · light", heightDp = 800)
@Composable
private fun TransferRecipientScreenPreview() {
    FinTechTheme {
        TransferRecipientScreen(
            sendAmount = "1000.00",
            rate = SampleData.usdToEur,
            recipients = SampleData.recipients,
            selectedRecipient = SampleData.recipients.first(),
            onRecipientSelected = {},
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Transfer recipient · dark", heightDp = 800)
@Composable
private fun TransferRecipientScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        TransferRecipientScreen(
            sendAmount = "1000.00",
            rate = SampleData.usdToEur,
            recipients = SampleData.recipients,
            selectedRecipient = null,
            onRecipientSelected = {},
            onBack = {},
            onConfirm = {},
        )
    }
}

/** Nothing selected and nothing matching — the two states the CTA and the list have to handle. */
@Preview(name = "Transfer recipient · no matches", heightDp = 800)
@Composable
private fun TransferRecipientScreenEmptyPreview() {
    FinTechTheme {
        TransferRecipientScreen(
            sendAmount = "1000.00",
            rate = SampleData.usdToEur,
            recipients = emptyList(),
            selectedRecipient = null,
            onRecipientSelected = {},
            onBack = {},
            onConfirm = {},
        )
    }
}
