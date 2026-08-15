/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
// ExperimentalMaterial3Api: TopAppBarDefaults is still experimental in M3.
@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalMaterial3Api::class)

package dev.formaui.fintechuikit.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme

/**
 * Brand-correct values for the places FormaUI's own defaults disagree with DESIGN.md.
 *
 * FormaUI's `*Defaults` are composable getters that read the theme, so most brand tokens flow
 * into its components automatically. Three do not:
 *
 * | FormaUI default                        | resolves to | brand wants        |
 * |----------------------------------------|-------------|--------------------|
 * | `FormaButtonDefaults.shape`            | `shapes.md` 12dp | `{rounded.xl}` 24dp |
 * | `FormaCardDefaults.shape`              | `shapes.lg` 16dp | `{rounded.xl}` 24dp |
 * | `FormaButtonDefaults.contentPadding`   | `lg`/`xs` 24/8dp | 24dp / 12dp        |
 * | `FormaCardDefaults.contentPadding`     | `md` 16dp        | `{spacing.xl}` 24dp |
 *
 * (`FormaTextFieldDefaults.outlinedShape` also reads `shapes.md`, but 12dp is exactly what
 * the brand wants for form inputs, so text fields need no correction.)
 *
 * Pass these explicitly at `Forma*` call sites. The shape slots cannot be reassigned to fix
 * the radii, because buttons and text fields both read `shapes.md` but want different values;
 * and the padding cannot be fixed at all through the theme, because FormaUI's spacing scale is
 * not overridable — see [BrandSpacing].
 *
 * [textButtonColors] is a different kind of correction: the offending default is Material 3's,
 * not FormaUI's, and the problem is contrast rather than geometry.
 */
object BrandDefaults {

    /** `{rounded.xl}` 24dp — the doc calls the pill geometry on CTAs "non-negotiable". */
    val buttonShape: Shape
        @Composable @ReadOnlyComposable get() = FormaTheme.shapes.xl

    /** `{spacing.md} {spacing.xl}` — 12dp vertical, 24dp horizontal, per `button-primary`. */
    val buttonContentPadding: PaddingValues
        @Composable @ReadOnlyComposable get() = PaddingValues(
            horizontal = FinTechTheme.spacing.xl,
            vertical = FinTechTheme.spacing.md,
        )

    /** `{rounded.xl}` 24dp — the brand's canonical card radius. */
    val cardShape: Shape
        @Composable @ReadOnlyComposable get() = FormaTheme.shapes.xl

    /** `{spacing.xl}` 24dp on all sides, per `card-content`. */
    val cardContentPadding: PaddingValues
        @Composable @ReadOnlyComposable get() = PaddingValues(FinTechTheme.spacing.xl)

    /** `{rounded.pill}` — status pills and full-radius accents. */
    val badgeShape: Shape
        @Composable @ReadOnlyComposable get() = FormaTheme.shapes.pill

    /** `{spacing.xs} {spacing.md}` — 4dp vertical, 12dp horizontal, per `badge-positive`. */
    val badgeContentPadding: PaddingValues
        @Composable @ReadOnlyComposable get() = PaddingValues(
            horizontal = FinTechTheme.spacing.md,
            vertical = FinTechTheme.spacing.xs,
        )

    /**
     * Colours for a **text** button — the one variant whose M3 default fails contrast here.
     *
     * M3 draws a text button's label in `colorScheme.primary`. For most palettes that is a
     * saturated accent and reads fine; this brand's primary is Wise green `#9fe870`, which on the
     * white canvas computes to **1.47:1** — nowhere near WCAG's 4.5:1 floor for body text. The
     * green is legible only as a *background* behind `onPrimary` ink, which is exactly how the
     * filled CTA uses it.
     *
     * `onSurface` is the fix, and it is also what DESIGN.md asks for independently: its `nav-link`
     * spec sets links in ink, and the doc reserves the green for the conversion pill. Using a
     * theme role rather than a fixed colour keeps it correct in dark mode too.
     *
     * Filled and outlined buttons need no equivalent — their labels already resolve to
     * `onPrimary` / `onSurface` against their own containers.
     */
    val textButtonColors: ButtonColors
        @Composable get() = ButtonDefaults.textButtonColors(
            contentColor = FormaTheme.colorScheme.onSurface,
        )

    /**
     * Text-button colours for an action that destroys something — "Report lost", and its like.
     *
     * [textButtonColors]' `onSurface` is right for an ordinary text button and wrong here: it makes
     * a destructive action look exactly like a neutral one, so the only warning left is the wording.
     * `colorScheme.error` is the role defined for precisely this, and it is re-derived per scheme —
     * `NegativeOnDark` in dark, where the light scheme's `#D03238` would sit at 3.1:1 on the ink
     * card.
     */
    val destructiveTextButtonColors: ButtonColors
        @Composable get() = ButtonDefaults.textButtonColors(
            contentColor = FormaTheme.colorScheme.error,
        )

    /**
     * Text-button colours for a surface painted a fixed ink, not a theme colour.
     *
     * [textButtonColors] resolves to `onSurface`, which is correct on every themed surface and wrong
     * on exactly one: the success screen, whose background is the fixed deep green
     * [BrandTokens.InkDeep] regardless of theme. There `onSurface` is ink in light mode — near-black
     * on dark green, unreadable. A screen that opts out of the theme for its background has to opt
     * out for its content colours too.
     */
    val onInkTextButtonColors: ButtonColors
        @Composable get() = ButtonDefaults.textButtonColors(
            contentColor = BrandTokens.CanvasSoft,
        )

    /**
     * Slider colours. M3's defaults leave a slider all but invisible on this palette's cards.
     *
     * Two failures, both on the white `card-content` a slider actually sits on:
     *
     *  - **The inactive track** defaults to the secondary-container role, which this brand maps to
     *    the sage `canvas-soft`. Sage on a white card is **1.20:1** — the empty half of the track
     *    simply is not there. Dark fares no better: the ink-lifted tone on the ink card is 1.13:1.
     *  - **The active track and thumb** default to `colorScheme.primary`, Wise green, which on white
     *    computes to **1.47:1** — the same number that made M3's text buttons unreadable and sent
     *    [textButtonColors] to `onSurface`. It only looks right in dark, where green on ink is 11.9:1.
     *
     * The filled half and the thumb take [BrandColors.accentOnSurface] — the brand green resolved
     * for whichever card the scheme puts behind it. Not `onSurface`: that is the right answer for
     * [textButtonColors], because a *label* is ink and needs 4.5:1, but a slider's track is an
     * accent mark held to 3:1, and any ink there reads as a plain black bar rather than as part of
     * the brand. Worth stating because the trap is subtle — the inks that clear 3:1 most easily are
     * exactly the ones that lose the green, so passing the check is not evidence of a good colour.
     *
     * The empty half takes `mute` — the brand's own lowest-priority tone, which is exactly what an
     * inactive track is, and which lifts per scheme (3.6:1 on the white card, 6.2:1 on the ink one).
     */
    val sliderColors: SliderColors
        @Composable get() = SliderDefaults.colors(
            thumbColor = FinTechTheme.brandColors.accentOnSurface,
            activeTrackColor = FinTechTheme.brandColors.accentOnSurface,
            inactiveTrackColor = FinTechTheme.brandColors.mute,
        )

    /**
     * Top-app-bar colours: a transparent container, so the bar sits on the page instead of banding it.
     *
     * M3 defaults a top app bar's container to `surface`, which in this palette is a different tone
     * from the `background` every screen behind the bar is painted with — the bar reads as a strip of
     * chrome stuck to the top rather than as the page's own heading. Transparent lets the screen's own
     * background through, whatever that screen is.
     *
     * Shared by the tab shell (`FinTechNavHost`) and by every flow screen (`FlowScreen`) so the two
     * cannot drift apart; both sit on `colorScheme.background`.
     *
     * No `scrolledContainerColor` on purpose: the bar is meant to look the same whether or not the
     * content below it has moved. That only stays readable because content is inset to stop at the
     * bar rather than travelling behind it — if a screen ever lets content scroll under the bar, it
     * needs an opaque container, not this.
     */
    val topAppBarColors: TopAppBarColors
        @Composable get() = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
}
