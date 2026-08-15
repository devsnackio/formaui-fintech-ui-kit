/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTypography
import dev.formaui.fintechuikit.R

/**
 * Inter, the brand's utility face.
 *
 * DESIGN.md pairs a proprietary display face (Wise Sans, weight 900) with Inter for
 * everything else, and names Inter weight 900 as the sanctioned open-source substitute for
 * the display tier — so one family covers both roles here.
 *
 * These are static instances rather than Inter's variable font on purpose: variation axes
 * need API 26+ and this project is minSdk 24, where a variable font would silently render at
 * its default w400 and drop the hero out of the weight-900 the brand requires.
 */
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_black, FontWeight.Black),
)

/**
 * The brand type scale, adapted from DESIGN.md's web scale to mobile.
 *
 * The doc's display tier runs 126 / 96 / 64 px, which is unusable on a phone, so the display
 * sizes are compressed. What is preserved is everything that carries the brand's voice:
 *
 *  - **Weight** — 900 for the display tier, 600 for sub-displays and labels, per the doc's
 *    "weight 900 for hero, weight 600 for everything else" principle.
 *  - **Line-height ratio** — the display tier's distinctive sub-1 leading (126/107.1,
 *    64/54.4 and 40/34 all land on 0.85), 1.2–1.3 for headings, 1.5 for body.
 *  - **Tracking** — the doc's negative letter-spacing on the semibold headings
 *    (-0.96px at 32px ≈ -0.03em, -0.48px at 24px ≈ -0.02em).
 *
 * The body tier (20 / 16 / 14 / 12) is already mobile-appropriate and maps literally.
 */
private val BrandMaterialTypography = Typography(
    // Display — DESIGN.md {typography.display-mega} / -xxl / -xl, compressed. Weight 900.
    displayLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Black,
        fontSize = 45.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.sp,
    ),

    // Headline — {typography.display-md} stays weight 900 (section / card headlines);
    // {typography.display-sm} and {typography.display-xs} are the Inter-rendered weight-600 tier.
    headlineLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.84).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.48).sp,
    ),

    // Title — the strong body variants, {typography.body-md-strong} / -sm-strong.
    titleLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),

    // Body — {typography.body-lg} / -md / -sm, literal.
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),

    // Label — M3 renders button text with labelLarge, so that slot carries
    // {typography.button-md}. labelSmall carries {typography.caption}.
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
)

/**
 * Tabular figures for monetary amounts.
 *
 * `tnum` fixes every digit to the same advance width so columns of amounts align and a
 * changing balance does not shift its own layout. CLAUDE.md requires this style for all money.
 */
private val BrandNumericTextStyle = TextStyle(
    fontFamily = InterFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum",
)

val BrandTypography = FormaTypography(
    material = BrandMaterialTypography,
    numeric = BrandNumericTextStyle,
)
