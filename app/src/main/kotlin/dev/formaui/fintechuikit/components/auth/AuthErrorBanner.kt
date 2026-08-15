/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The auth flow's error surface — "Those details don't match an account", "That PIN is wrong".
 *
 * Colour comes from the M3 error-container roles rather than [dev.formaui.fintechuikit.ui.theme.BrandColors]
 * because in this brand those roles already *are* the doc's destructive callout: the light scheme
 * maps `errorContainer` to `{colors.negative-bg}` with a white label, which is exactly
 * `badge-negative`. Reading them from the scheme means the banner follows any future retune of
 * the palette instead of pinning a second copy of the same two tokens — as it already has, since
 * the dark scheme had to swap that maroon for `{colors.negative}` to stay visible on ink.
 *
 * The radius is `shapes.md` 12dp — the form-input radius, not the 24dp card radius — so the
 * banner lines up with the fields it sits above rather than reading as a card inside a card.
 *
 * Width is left to the caller: the form screens stretch it across the card, while the PIN screen
 * lets it hug its text under the dots.
 */
@Composable
fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(FormaTheme.colorScheme.errorContainer, FormaTheme.shapes.md)
            .padding(
                horizontal = FinTechTheme.spacing.lg,
                vertical = FinTechTheme.spacing.md,
            )
            // An auth error appears without the user moving focus — it is the response to a
            // submit they already made — so nothing would announce it otherwise.
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            // The message states the problem; describing the glyph would only repeat it.
            contentDescription = null,
            tint = FormaTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = message,
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onErrorContainer,
        )
    }
}

@Preview(name = "AuthErrorBanner · light")
@Composable
private fun AuthErrorBannerPreview() {
    FinTechTheme {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        ) {
            AuthErrorBanner("That email and password don't match an account.", Modifier.fillMaxWidth())
            AuthErrorBanner("Incorrect PIN. 2 attempts left.")
        }
    }
}

@Preview(name = "AuthErrorBanner · dark")
@Composable
private fun AuthErrorBannerDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        ) {
            AuthErrorBanner("That email and password don't match an account.", Modifier.fillMaxWidth())
            AuthErrorBanner("Incorrect PIN. 2 attempts left.")
        }
    }
}
