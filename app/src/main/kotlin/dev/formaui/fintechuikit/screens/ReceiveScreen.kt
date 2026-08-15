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
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The details to hand someone so they can pay you.
 *
 * Copy is wired to Compose's `LocalClipboard` rather than left as a placeholder. This screen
 * exists partly because the alternative — a "Share" button that does nothing — is the exact pattern
 * the rest of this work is removing; a copy action is something a demo can genuinely do, with no
 * permission, no intent plumbing, and no `Context`.
 *
 * There is no QR code here. The Dashboard's "Request" action carries a QR icon and the project has
 * no QR support anywhere, so drawing a fake one would be worse than not offering it.
 */
@Composable
fun ReceiveScreen(
    account: Account,
    handle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = rememberCopyToClipboard("Payment details")

    FlowScreen(
        title = "Get paid",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = {
                    copy("$handle · ${account.label} · ${account.balance.currency.code}")
                },
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Text("  Copy details")
            }
        },
    ) {
        Text(
            text = "Share these with anyone who needs to pay you. Money arrives in your " +
                "${account.label} balance.",
            style = FormaTheme.typography.material.bodyLarge,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )

        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            contentPadding = BrandDefaults.cardContentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child: FormaCard's own 16dp arrangement is not overridable.
            Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {
                DetailLine("Handle", handle)
                FormaDivider()
                DetailLine("Account", account.label)
                FormaDivider()
                DetailLine("Currency", account.balance.currency.displayName)
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
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

@Preview(name = "Receive · light", heightDp = 640)
@Composable
private fun ReceiveScreenPreview() {
    FinTechTheme {
        ReceiveScreen(
            account = SampleData.primaryAccount,
            handle = "alex.morgan@forma.example",
            onBack = {},
        )
    }
}

@Preview(name = "Receive · dark", heightDp = 640)
@Composable
private fun ReceiveScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        ReceiveScreen(
            account = SampleData.primaryAccount,
            handle = "alex.morgan@forma.example",
            onBack = {},
        )
    }
}
