/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A section title with an optional trailing text action ("Recent activity" · "See all").
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = FormaTheme.typography.material.headlineSmall)
        if (actionLabel != null && onActionClick != null) {
            FormaButton(
                onClick = onActionClick,
                variant = FormaButtonVariant.Text,
                colors = BrandDefaults.textButtonColors,
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Preview
@Composable
private fun SectionHeaderPreview() {
    FinTechTheme {
        Row(Modifier.padding(FinTechTheme.spacing.lg)) {
            SectionHeader(title = "Recent activity", actionLabel = "See all", onActionClick = {})
        }
    }
}
