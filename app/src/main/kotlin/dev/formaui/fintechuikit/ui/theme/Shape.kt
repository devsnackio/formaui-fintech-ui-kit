/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaShapes

/**
 * DESIGN.md's border-radius scale, mapped onto FormaUI's shape slots.
 *
 * The brand never uses sharp corners on UI elements — the generous radius is, in the doc's
 * words, its "friendliness signature" — and [xl] 24dp is the canonical card and button shape.
 *
 * Note that FormaUI's own component defaults do **not** all resolve to the slot the brand
 * wants: `FormaTextField` reads `md` (12dp, correct), but `FormaButton` also reads `md` and
 * `FormaCard` reads `lg`, both of which should be 24dp per the brand. Skewing this scale to
 * fix buttons would break text fields, so the scale stays truthful to the doc and
 * [BrandDefaults] supplies the corrected values instead.
 */
val BrandShapes = FormaShapes(
    none = RoundedCornerShape(0.dp),           // {rounded.none} — full-bleed bands
    // FormaUI slot with no DESIGN.md equivalent; half of {rounded.sm} keeps the scale even.
    xs = RoundedCornerShape(4.dp),
    sm = RoundedCornerShape(8.dp),             // {rounded.sm} — inline pills, small badges
    md = RoundedCornerShape(12.dp),            // {rounded.md} — form inputs, smaller chrome
    lg = RoundedCornerShape(16.dp),            // {rounded.lg} — mid-size cards
    xl = RoundedCornerShape(24.dp),            // {rounded.xl} — canonical button + card
    pill = RoundedCornerShape(percent = 50),   // {rounded.pill} — status pills
    full = RoundedCornerShape(percent = 50),   // {rounded.full} — circular icon containers
)
