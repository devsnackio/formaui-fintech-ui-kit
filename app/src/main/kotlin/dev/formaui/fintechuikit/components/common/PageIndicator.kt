@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A pager position indicator: one dot per page, the current one stretched to a pill.
 *
 * Lives in `common/` because it has two unrelated consumers — the onboarding deck and the card
 * wallet — and nothing about it is specific to either.
 *
 * Wise green on the active dot is deliberate and is the one place it appears outside a CTA —
 * DESIGN.md's `ex-app-shell-row` sanctions exactly this ("active state uses brand primary as the
 * indicator"), and the dot is far too small to compete with a pill button for the eye.
 *
 * Purely decorative to look at, but not to a screen reader: a pager announces nothing about where
 * the user is in the set, so the row collapses to a single "Page 2 of 4" label via
 * [clearAndSetSemantics] rather than exposing four unlabelled boxes.
 */
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Page ${currentPage + 1} of $pageCount"
        },
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val active = page == currentPage
            // Animating width and colour (rather than swapping two dots) keeps the stretch
            // continuous while a half-finished swipe is still settling.
            val width by animateDpAsState(
                targetValue = if (active) ActiveDotWidth else DotSize,
                label = "indicatorWidth",
            )
            val color by animateColorAsState(
                targetValue = if (active) {
                    FormaTheme.colorScheme.primary
                } else {
                    FormaTheme.colorScheme.outlineVariant
                },
                label = "indicatorColor",
            )
            Box(
                Modifier
                    .height(DotSize)
                    .width(width)
                    .clip(FormaTheme.shapes.pill)
                    .background(color),
            )
        }
    }
}

private val DotSize = 8.dp
private val ActiveDotWidth = 24.dp

@Preview(name = "PageIndicator · light")
@Composable
private fun PageIndicatorPreview() {
    FinTechTheme {
        IndicatorStack()
    }
}

@Preview(name = "PageIndicator · dark")
@Composable
private fun PageIndicatorDarkPreview() {
    FinTechTheme(darkTheme = true) {
        IndicatorStack()
    }
}

@Composable
private fun IndicatorStack() {
    Column(
        Modifier
            .background(FormaTheme.colorScheme.background)
            .padding(FinTechTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
    ) {
        repeat(4) { page ->
            PageIndicator(pageCount = 4, currentPage = page)
        }
    }
}
