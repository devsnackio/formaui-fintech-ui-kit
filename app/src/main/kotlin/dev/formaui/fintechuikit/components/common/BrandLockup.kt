@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.R
import dev.formaui.fintechuikit.ui.theme.BrandTokens
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The product lockup: the Forma mark beside the "Forma" wordmark.
 *
 * The mark is an open ring with a dot already leaving it — a value moving out of a balance. It
 * sits on a green tile, which is the only non-button use of `{colors.primary}` on the welcome
 * screen and the one DESIGN.md names as sanctioned ("the brand's logo accent").
 *
 * The mark itself is [BrandTokens.Ink] rather than `onPrimary`: the mark's handoff fixes it on a
 * green field at `{colors.ink}` #0E0F0C, and reserves `{colors.ink-deep}` #163300 — what this
 * scheme's `onPrimary` resolves to — for the *wordmark* when that sits on green. The tile and the
 * wordmark do read from the scheme, so the lockup still follows light and dark.
 *
 * The mark carries no content description; the [Row] announces the pair as one "Forma" label via
 * [clearAndSetSemantics], which also keeps anything added inside later from leaking a second
 * announcement next to a wordmark that already says the same word.
 *
 * [size] scales the wordmark as well as the mark. It has to: a lockup is one object, and a 40dp
 * mark beside an unchanged 32sp wordmark reads as a small avatar with a large label next to it.
 * The scale matters at the call site — on an auth form the lockup sits directly above a
 * `headlineMedium` page title, and at [FormaAvatarSize.Medium] the wordmark would be both larger
 * and heavier than the title it is supposed to sit under.
 */
@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    size: FormaAvatarSize = FormaAvatarSize.Medium,
) {
    val markSize = when (size) {
        FormaAvatarSize.Small -> 28.dp
        FormaAvatarSize.Medium -> 40.dp
        FormaAvatarSize.Large -> 56.dp
    }

    // Two cuts of the mark, not one asset scaled. Under 32dp the dot fuses with the ring stroke,
    // so the small step takes the ring-only cut with its wider gap. The tile's corner is a
    // percentage rather than a fixed {rounded.xl} 24dp because the radius is authored in the
    // mark's own 100-unit box and has to shrink with it; the small cut's 26 holds the same
    // optical roundness at 28dp that 24 holds at 40dp.
    val isSmall = size == FormaAvatarSize.Small
    val markCut = if (isSmall) R.drawable.ic_forma_mark_24 else R.drawable.ic_forma_mark_tile
    val tileCornerPercent = if (isSmall) 26 else 24

    // Weight 900 throughout — the brand's display ceiling, and a wordmark is a display moment at
    // any size. Only the size steps.
    val wordmarkStyle = when (size) {
        FormaAvatarSize.Small -> FormaTheme.typography.material.titleLarge
        FormaAvatarSize.Medium -> FormaTheme.typography.material.headlineLarge
        FormaAvatarSize.Large -> FormaTheme.typography.material.displaySmall
    }.copy(
        fontWeight = FontWeight.Black,
        // The brand tightens tracking as type grows: -0.03em from 32sp up, -0.02em below it.
        // Expressed in em so it stays correct if the type scale moves under it.
        letterSpacing = if (isSmall) (-0.02).em else (-0.03).em,
    )

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "Forma" },
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(markSize)
                .background(
                    color = FormaTheme.colorScheme.primary,
                    shape = RoundedCornerShape(percent = tileCornerPercent),
                ),
        ) {
            // The mark is drawn in the same 100-unit box as the tile, so it fills it rather than
            // being inset — the clear space is part of the artwork.
            Icon(
                painter = painterResource(markCut),
                contentDescription = null,
                tint = BrandTokens.Ink,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            "Forma",
            style = wordmarkStyle,
            color = FormaTheme.colorScheme.onBackground,
        )
    }
}

@Preview(name = "BrandLockup · light")
@Composable
private fun BrandLockupPreview() {
    FinTechTheme {
        LockupStack()
    }
}

@Preview(name = "BrandLockup · dark")
@Composable
private fun BrandLockupDarkPreview() {
    FinTechTheme(darkTheme = true) {
        LockupStack()
    }
}

@Composable
private fun LockupStack() {
    Column(
        Modifier
            .background(FormaTheme.colorScheme.background)
            .padding(FinTechTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
    ) {
        BrandLockup(size = FormaAvatarSize.Small)
        BrandLockup()
        BrandLockup(size = FormaAvatarSize.Large)
    }
}
