/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The accent an onboarding illustration is tinted with.
 *
 * DESIGN.md forbids a second brand accent but carves out one exception: `{colors.accent-orange}`
 * and `{colors.accent-cyan}` are defined as *illustration* accents, so a slide plate is one of
 * the few surfaces allowed to cycle them. Modelling the choice as an enum rather than a [Color]
 * keeps the slide list a plain data list — the colour resolves against the live theme at the
 * call site instead of being baked in at file-init time, which is what makes the same list
 * render correctly in both schemes.
 */
enum class OnboardingAccent { Primary, Cyan, Orange }

/** Resolves an [OnboardingAccent] against the brand palette. */
val OnboardingAccent.color: Color
    @Composable @ReadOnlyComposable get() = when (this) {
        OnboardingAccent.Primary -> FormaTheme.colorScheme.primary
        OnboardingAccent.Cyan -> FinTechTheme.brandColors.accentCyan
        OnboardingAccent.Orange -> FinTechTheme.brandColors.accentOrange
    }

/**
 * The illustration for one onboarding slide, composed from shapes rather than a drawable.
 *
 * Three layers, in DESIGN.md's own vocabulary: a `{rounded.xl}` plate carrying a wash of the
 * accent, a full-saturation `{rounded.full}` dot breaking the plate's top-end corner so the
 * silhouette is not a plain square, and the glyph centred on top.
 *
 * The plate is the accent at low alpha rather than a fixed pale tint so one value works in both
 * schemes — it composites toward white on the sage canvas and toward ink on the dark page,
 * which keeps the glyph's `onSurface` contrast intact either way. Everything here is decorative:
 * the slide's title carries the meaning, so the icon takes `contentDescription = null`.
 */
@Composable
fun OnboardingIllustration(
    icon: ImageVector,
    accent: OnboardingAccent,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    val accentColor = accent.color

    Box(modifier = modifier.size(size)) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(FormaTheme.shapes.xl)
                .background(accentColor.copy(alpha = PlateTintAlpha)),
        )

        Box(
            Modifier
                .align(Alignment.TopEnd)
                // Pushed out over the plate's corner — the overlap is what stops the pair
                // reading as one flat badge.
                .offset(x = size * 0.08f, y = -size * 0.08f)
                .size(size * DotFraction)
                .clip(FormaTheme.shapes.full)
                .background(accentColor),
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FormaTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.Center)
                .size(size * GlyphFraction),
        )
    }
}

/** Light enough that the plate reads as a tint of the page, not a filled card. */
private const val PlateTintAlpha = 0.20f
private const val DotFraction = 0.26f
private const val GlyphFraction = 0.36f

@Preview(name = "OnboardingIllustration · light")
@Composable
private fun OnboardingIllustrationPreview() {
    FinTechTheme {
        IllustrationRow()
    }
}

@Preview(name = "OnboardingIllustration · dark")
@Composable
private fun OnboardingIllustrationDarkPreview() {
    FinTechTheme(darkTheme = true) {
        IllustrationRow()
    }
}

@Composable
private fun IllustrationRow() {
    Row(
        Modifier
            .background(FormaTheme.colorScheme.background)
            .padding(FinTechTheme.spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl),
    ) {
        OnboardingIllustration(Icons.Filled.SwapHoriz, OnboardingAccent.Primary, size = 120.dp)
        OnboardingIllustration(Icons.Filled.Language, OnboardingAccent.Cyan, size = 120.dp)
        OnboardingIllustration(Icons.Filled.CreditCard, OnboardingAccent.Orange, size = 120.dp)
    }
}
