/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.paymentcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.components.iconbutton.FormaIconButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/** One circular action under the card — freeze, lock PIN, show details. */
@Composable
fun CardControlAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
    ) {
        FormaIconButton(
            onClick = onClick,
            variant = if (selected) FormaIconButtonVariant.Filled else FormaIconButtonVariant.Tonal,
        ) {
            // The label below states the action, so the icon itself needn't repeat it.
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        }
        Text(
            label,
            style = FormaTheme.typography.material.labelSmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A row of card controls. Stateless — the hosting screen owns freeze/lock state. */
@Composable
fun CardControlRow(
    frozen: Boolean,
    onToggleFreeze: () -> Unit,
    onChangePin: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl),
    ) {
        CardControlAction(
            icon = Icons.Filled.AcUnit,
            label = if (frozen) "Unfreeze" else "Freeze",
            onClick = onToggleFreeze,
            selected = frozen,
        )
        // "Lock PIN" named no action this app performs. The control opens the change-PIN flow, so
        // the label says that.
        CardControlAction(icon = Icons.Filled.Lock, label = "Change PIN", onClick = onChangePin)
        CardControlAction(icon = Icons.Filled.Visibility, label = "Details", onClick = onShowDetails)
    }
}

@Preview
@Composable
private fun CardControlRowPreview() {
    FinTechTheme {
        Row(Modifier.padding(FinTechTheme.spacing.lg)) {
            CardControlRow(
                frozen = false,
                onToggleFreeze = {},
                onChangePin = {},
                onShowDetails = {},
            )
        }
    }
}
