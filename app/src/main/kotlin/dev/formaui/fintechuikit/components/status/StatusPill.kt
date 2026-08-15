@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.model.TransactionStatus
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Which semantic family a [StatusPill] draws from.
 *
 * These map to DESIGN.md's `badge-positive` / `badge-negative` and the warning family. Note
 * that [Positive] uses the pale-green surface with deep-green text, **not** the CTA green:
 * the doc is explicit that Wise green is the conversion colour and must never double as a
 * success indicator.
 */
enum class StatusTone { Positive, Warning, Negative, Neutral }

/**
 * A small status marker — "Pending", "Failed", "Completed".
 *
 * Rendered as a `{rounded.pill}` capsule with `{spacing.xs} {spacing.md}` padding, per the
 * doc's `badge-positive` spec.
 */
@Composable
fun StatusPill(
    label: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
) {
    val brand = FinTechTheme.brandColors
    val container: Color
    val content: Color
    when (tone) {
        StatusTone.Positive -> {
            container = brand.primaryPale
            content = brand.positiveDeep
        }
        StatusTone.Warning -> {
            container = brand.warning
            content = brand.warningContent
        }
        StatusTone.Negative -> {
            // The error-*container* pair, not `negativeBg`/`onError`. Those two are not a pair:
            // `onError` is the label colour for the solid error fill and flips to near-black ink in
            // the dark scheme, where it lands on the unchanged maroon at 1.07:1. The container roles
            // are defined together and re-derived per scheme, so they stay legible in both.
            container = FormaTheme.colorScheme.errorContainer
            content = FormaTheme.colorScheme.onErrorContainer
        }
        StatusTone.Neutral -> {
            // Same trap, quieter: `canvasSoft` is a light surface in both schemes while `body`
            // lightens for dark, so the brand pair collapses to 1.6:1 there. The surface-variant
            // roles resolve to exactly these two tokens in light and to the ink pair in dark.
            container = FormaTheme.colorScheme.surfaceVariant
            content = FormaTheme.colorScheme.onSurfaceVariant
        }
    }

    Row(
        modifier = modifier
            .background(container, BrandDefaults.badgeShape)
            .padding(BrandDefaults.badgeContentPadding),
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
    ) {
        Text(label, style = FormaTheme.typography.material.titleSmall, color = content)
    }
}

/** Convenience overload for the common case of describing a transaction's state. */
@Composable
fun StatusPill(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (label, tone) = when (status) {
        TransactionStatus.Completed -> "Completed" to StatusTone.Positive
        TransactionStatus.Pending -> "Pending" to StatusTone.Warning
        TransactionStatus.Failed -> "Failed" to StatusTone.Negative
    }
    StatusPill(label = label, tone = tone, modifier = modifier)
}

/**
 * All four tones, on the card surface they are actually drawn on.
 *
 * The surface matters: a pill is a fill as well as a label, so a preview against the page
 * background can show a chip that disappears into a card in the real screen. This preview is
 * paired light and dark on purpose — the Negative tone was unreadable in dark for exactly as long
 * as there was only a light one.
 */
@Composable
private fun StatusPillPreview(dark: Boolean) {
    FinTechTheme(darkTheme = dark) {
        Row(
            Modifier
                .background(FormaTheme.colorScheme.surface)
                .padding(FinTechTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
        ) {
            StatusPill("Completed", StatusTone.Positive)
            StatusPill("Pending", StatusTone.Warning)
            StatusPill("Failed", StatusTone.Negative)
            StatusPill("Draft", StatusTone.Neutral)
        }
    }
}

@Preview(name = "StatusPill · light")
@Composable
private fun StatusPillLightPreview() = StatusPillPreview(dark = false)

@Preview(name = "StatusPill · dark")
@Composable
private fun StatusPillDarkPreview() = StatusPillPreview(dark = true)
