/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.divider.FormaDivider
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.common.rememberCopyToClipboard
import dev.formaui.fintechuikit.components.paymentcard.PaymentCardArt
import dev.formaui.fintechuikit.components.status.StatusPill
import dev.formaui.fintechuikit.components.status.StatusTone
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The card's own details, behind the "Details" control.
 *
 * **There is no reveal here, and that is the point.** `PaymentCard` models only `last4` — its own
 * comment says "a UI kit has no business holding a PAN" — so there is no full number and no CVV to
 * unmask. A reveal toggle would either need the kit to start carrying a card number in sample data,
 * or would be a button that uncovers nothing. Neither is worth it, so this screen shows what the card
 * actually is and copies the only identifier it has.
 *
 * That constraint is why the screen leads with [PaymentCardArt] rather than a table: the card face is
 * the most informative thing available, and it is already built.
 */
@Composable
fun CardDetailsScreen(
    card: PaymentCard,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = rememberCopyToClipboard("Card digits")

    FlowScreen(
        title = "Card details",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = { copy(card.last4) },
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Text("  Copy last 4 digits")
            }
        },
    ) {
        PaymentCardArt(card)

        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            contentPadding = BrandDefaults.cardContentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child: FormaCard's 16dp content arrangement cannot be overridden.
            Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {
                DetailRow("Cardholder", card.holderName)
                FormaDivider()
                DetailRow("Number", card.maskedNumber)
                FormaDivider()
                DetailRow("Expires", card.expiry)
                FormaDivider()
                DetailRow("Network", card.network.displayName)
                FormaDivider()
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Status",
                        style = FormaTheme.typography.material.bodyMedium,
                        color = FormaTheme.colorScheme.onSurfaceVariant,
                    )
                    // Frozen is the state worth flagging, so it gets the warning pill; active is
                    // unremarkable and reads as plain text.
                    if (card.isFrozen) {
                        StatusPill(label = "Frozen", tone = StatusTone.Warning)
                    } else {
                        Text("Active", style = FormaTheme.typography.material.bodyMedium)
                    }
                }
            }
        }

        Text(
            text = "Your full card number is never shown in the app. Freeze the card from the " +
                "Card tab if you think it has been used without you.",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = FormaTheme.typography.material.bodyMedium,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = FormaTheme.typography.material.bodyMedium)
    }
}

@Preview(name = "Card details · light", heightDp = 820)
@Composable
private fun CardDetailsScreenPreview() {
    FinTechTheme {
        CardDetailsScreen(card = SampleData.card, onBack = {})
    }
}

@Preview(name = "Card details · frozen dark", heightDp = 820)
@Composable
private fun CardDetailsScreenFrozenPreview() {
    FinTechTheme(darkTheme = true) {
        CardDetailsScreen(card = SampleData.card.copy(isFrozen = true), onBack = {})
    }
}
