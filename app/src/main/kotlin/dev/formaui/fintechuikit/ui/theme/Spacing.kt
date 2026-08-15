@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaSpacing

/**
 * DESIGN.md's 4px-based spacing scale, mapped onto FormaUI's eight spacing slots.
 *
 * The brand's scale sits one notch finer than FormaUI's throughout — FormaUI uses
 * 4/8/12/16/24/32/48/96 where the brand wants 2/4/8/12/16/24/32/48 — and the brand's `2xl`
 * and `3xl` land in FormaUI's `xxl` and `section` slots.
 *
 * **This does not replace FormaUI's spacing.** `FormaTheme` accepts no spacing parameter and
 * its `LocalFormaSpacing` is `internal`, so the library's own scale cannot be overridden from
 * outside it. The consequence is a split that call sites have to respect:
 *
 *  - `FinTechTheme.spacing` — the brand scale below. Use this everywhere in this project.
 *  - `FormaTheme.spacing` — FormaUI's fixed scale, one step coarser. Avoid it; it is what
 *    `FormaButton`/`FormaCard` fall back to internally, which is exactly why [BrandDefaults]
 *    exists to pass brand padding explicitly.
 *
 * It reuses FormaUI's [FormaSpacing] type (whose constructor *is* public) so the two scales
 * stay interchangeable at any API that expects one.
 */
val BrandSpacing = FormaSpacing(
    xxs = 2.dp,     // {spacing.xxs}
    xs = 4.dp,      // {spacing.xs} — the base unit
    sm = 8.dp,      // {spacing.sm}
    md = 12.dp,     // {spacing.md}
    lg = 16.dp,     // {spacing.lg}
    xl = 24.dp,     // {spacing.xl} — card interior padding
    xxl = 32.dp,    // {spacing.2xl}
    section = 48.dp // {spacing.3xl} — band top/bottom padding
)

val LocalBrandSpacing = staticCompositionLocalOf { BrandSpacing }
