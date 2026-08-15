/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.components.switch.FormaSwitch
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A settings/menu row: leading icon, title, optional value, and either a chevron (navigates)
 * or a caller-supplied trailing control (e.g. a switch).
 *
 * When [trailing] is supplied the row does not also draw a chevron — a control and a
 * navigation affordance on the same row would each imply a different tap behaviour.
 *
 * The text styles are overridden for the same reason [dev.formaui.fintechuikit.components
 * .transaction.TransactionRow] overrides them: M3's `ListItem` sets its headline in `bodyLarge`
 * and its supporting line in `bodyMedium`, which on this brand's scale are 20sp and 16sp — lead
 * paragraph sizes. A settings menu is a dense list of labels, not prose, so both step down one
 * slot. Kept identical to `TransactionRow` so every list row in the app reads at one size.
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    FormaListItem(
        headline = title,
        supporting = value,
        modifier = modifier,
        onClick = onClick,
        leading = {
            FormaAvatar(size = FormaAvatarSize.Small) {
                // The title states the setting; the icon is a visual aid, not new information.
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        },
        trailing = {
            when {
                trailing != null -> trailing()
                onClick != null -> Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null, // the row is already a button; chevron is decor
                )
            }
        },
        headlineTextStyle = FormaTheme.typography.material.bodyMedium,
        supportingTextStyle = FormaTheme.typography.material.bodySmall,
    )
}

/**
 * A settings row that toggles a switch.
 *
 * The **whole row** is the toggle target, via `Modifier.toggleable(role = Role.Switch)`, and
 * the inner [FormaSwitch] is passed `onCheckedChange = null` so it renders display-only. This
 * is the pattern the FormaUI reference documents: one merged 48dp target that a screen reader
 * announces as "title, switch, on/off", instead of a separate tiny hit area on the control.
 */
@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
) {
    FormaListItem(
        headline = title,
        supporting = value,
        modifier = modifier.toggleable(
            value = checked,
            onValueChange = onCheckedChange,
            role = Role.Switch,
        ),
        leading = {
            FormaAvatar(size = FormaAvatarSize.Small) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        },
        trailing = {
            // Display-only: the row's toggleable owns the interaction and the semantics.
            FormaSwitch(checked = checked, onCheckedChange = null)
        },
        // Matched to SettingsRow so a toggle row and a chevron row sit at the same size.
        headlineTextStyle = FormaTheme.typography.material.bodyMedium,
        supportingTextStyle = FormaTheme.typography.material.bodySmall,
    )
}

@Preview
@Composable
private fun SettingsRowPreview() {
    FinTechTheme {
        Column {
            SettingsRow(Icons.Filled.Security, "Security", value = "Face ID enabled", onClick = {})
            SettingsRow(Icons.Filled.Language, "Language", value = "English", onClick = {})
            SettingsToggleRow(
                Icons.Filled.Notifications,
                "Push notifications",
                checked = true,
                onCheckedChange = {},
            )
        }
    }
}
