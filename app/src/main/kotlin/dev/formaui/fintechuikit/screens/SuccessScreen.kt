/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.BrandTokens
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/**
 * The completion moment, shared by every flow that moves money.
 *
 * The one screen in the app that flips polarity: a dark surface with the headline in Wise green.
 * Completing a transfer is the moment worth spending that on, and everywhere else the green is
 * reserved for the CTA, which is why no in-app screen looks like this one.
 *
 * The surface is **`BrandTokens.InkDeep` (#163300), not `Ink`**. DESIGN.md describes ink-deep as "a
 * deep forest-green ink used on positive-state surfaces", which is exactly what this is; `Ink` is
 * #0E0F0C and made the screen read as a plain black rectangle rather than a branded one. The
 * `hero-band-dark` spec that does name `{colors.ink}` is for marketing hero bands, not for a
 * positive-state confirmation.
 *
 * The colours are **fixed, not theme-derived** — `BrandTokens.*` rather than `colorScheme.*`. A
 * polarity flip that follows the theme is not a flip: in dark mode the background would resolve back
 * to a dark surface and the contrast would vanish. So this screen renders identically in light and
 * dark, and both previews below show that on purpose.
 *
 * Contrast on the deep green, all comfortably past 4.5:1 — green headline 9.4:1, amount 11.3:1,
 * subtitle 7.2:1.
 *
 * Like every non-tab destination it owns the full window, painting its own background and applying
 * [safeDrawingPadding] — the shell withholds the `Scaffold`'s insets from it.
 *
 * @param headline the past-tense verb for what happened — "Sent.", "Added.", "Requested."
 * @param amount the sum that moved, in `typography.numeric` for tabular figures. `null` for a
 *   completion that moved no money — the figure is then omitted rather than shown as zero.
 * @param subtitle who it involved, e.g. "to Kenji Watanabe".
 * @param onDone dismisses the flow; the caller is responsible for clearing it off the back stack.
 * @param onViewReceipt optional secondary action. Pass `null` to omit it rather than supplying a
 *   no-op, so the screen never shows a button that does nothing.
 */
@Composable
fun SuccessScreen(
    headline: String,
    amount: Money?,
    subtitle: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onViewReceipt: (() -> Unit)? = null,
) {
    val reduced = LocalReducedMotion.current
    // Starts settled when motion is off, so the badge simply exists rather than animating to 1f.
    val badgeScale = remember { Animatable(if (reduced) 1f else 0.6f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        // The confirmation the platform reserves for exactly this — a completed action — and the
        // one part of the arrival that is felt rather than seen. Deliberately NOT gated on
        // `reduced`: that setting is about motion on screen, and a user who turned animations off
        // still wants to know their money moved. It fires once per arrival because the whole
        // screen is one arrival; nothing here re-triggers it.
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        if (!reduced) {
            badgeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandTokens.InkDeep)
            .safeDrawingPadding()
            .padding(
                horizontal = FinTechTheme.spacing.xl,
                vertical = FinTechTheme.spacing.section,
            ),
    ) {
        // The hero takes the slack so the actions stay at the bottom edge, matching WelcomeScreen.
        // It scrolls in case a large font scale overruns it — the actions must never be pushed off.
        //
        // Centred, unlike the start-aligned welcome hero, for the same reason OnboardingSlide is:
        // this is one self-contained moment rather than a page you read down. Start-aligning it left
        // the badge and three short lines hanging in the top-left of a large dark field.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // The screen had no imagery at all, which is most of why it read as an empty dark
            // rectangle. A filled green disc with a check is the one mark this moment needs.
            //
            // The badge is the only spring in the app; every nav transition is a plain decelerating
            // tween, which is what makes one bounce here register as an event rather than as the
            // house style. It is also now the *whole* of this screen's own motion — the staggered
            // entrance the text used to carry was removed because it double-faded against the host
            // transition, and the bounce turns out to be the better beat anyway: it is a scale, so
            // it composes with the host's scale instead of fighting its fade.
            FormaAvatar(
                size = FormaAvatarSize.Large,
                containerColor = BrandTokens.Primary,
                contentColor = BrandTokens.InkDeep,
                modifier = Modifier.graphicsLayer {
                    scaleX = badgeScale.value
                    scaleY = badgeScale.value
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    // The headline says what happened; the mark repeats it visually.
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(FinTechTheme.spacing.xl))

            // Unstaggered, like every other screen. `FinTechNavHost` fades and scales this one in as
            // a whole, and a second fade underneath it multiplies rather than adds — see
            // `screenMotion` there. The beat this screen wants is real; the badge above supplies it.
            Text(
                text = headline,
                style = FormaTheme.typography.material.headlineMedium,
                color = BrandTokens.Primary,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(Modifier.height(FinTechTheme.spacing.sm))

            // The amount leads, at the display tier. In a money app the figure is the point of the
            // screen and the verb labels it — the previous order had a 36sp "Sent." over a 28sp
            // amount, which made the word the subject.
            //
            // Omitted entirely when there is no amount. Not every completion moved money — changing
            // a PIN is one — and rendering a zero there put a large "$0.00" on screen, which reads
            // as a figure the user should care about rather than as an absence.
            if (amount != null) {
                Text(
                    text = amount.formatWithSymbol(),
                    style = FormaTheme.typography.numeric.merge(
                        FormaTheme.typography.material.displaySmall,
                    ),
                    color = BrandTokens.CanvasSoft,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(FinTechTheme.spacing.xs))
            }

            Text(
                text = subtitle,
                style = FormaTheme.typography.material.bodyLarge,
                // The derived "secondary text on ink" token, so the counterparty reads as support
                // rather than competing with the amount.
                color = BrandTokens.BodyOnDark,
                textAlign = TextAlign.Center,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
            FormaButton(
                onClick = onDone,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Done")
            }
            if (onViewReceipt != null) {
                FormaButton(
                    onClick = onViewReceipt,
                    variant = FormaButtonVariant.Text,
                    // Not BrandDefaults.textButtonColors: that resolves to onSurface, which is ink
                    // on this ink surface. The label has to come from the fixed palette too.
                    colors = BrandDefaults.onInkTextButtonColors,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View receipt")
                }
            }
        }
    }
}

@Preview(name = "Success · sent", heightDp = 720)
@Composable
private fun SuccessScreenSentPreview() {
    FinTechTheme {
        SuccessScreen(
            headline = "Sent.",
            amount = Money.of(50, 0, Currency.USD),
            subtitle = "to Kenji Watanabe",
            onDone = {},
            onViewReceipt = {},
        )
    }
}

/**
 * Dark mode renders identically — the polarity flip is fixed, not theme-derived. This preview exists
 * to prove that, since a difference here would be the bug.
 */
/** The no-amount case: a completion that moved no money must not show a figure. */
@Preview(name = "Success · no amount", heightDp = 720)
@Composable
private fun SuccessScreenNoAmountPreview() {
    FinTechTheme {
        SuccessScreen(
            headline = "PIN changed.",
            amount = null,
            subtitle = "Use it next time you pay in a shop",
            onDone = {},
        )
    }
}

@Preview(name = "Success · added (dark theme)", heightDp = 720)
@Composable
private fun SuccessScreenAddedDarkPreview() {
    FinTechTheme(darkTheme = true) {
        SuccessScreen(
            headline = "Added.",
            amount = Money.of(200, 0, Currency.USD),
            subtitle = "from Visa •••• 4821",
            onDone = {},
        )
    }
}
