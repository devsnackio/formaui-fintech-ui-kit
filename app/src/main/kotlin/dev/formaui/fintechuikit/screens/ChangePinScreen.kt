/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
// ExperimentalMaterial3Api: FormaTopAppBar's scrollBehavior parameter type is still experimental
// in M3, so the opt-in is needed even though none is passed.
@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalMaterial3Api::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.components.topappbar.FormaTopAppBar
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.auth.AuthErrorBanner
import dev.formaui.fintechuikit.components.auth.PinDefaults
import dev.formaui.fintechuikit.components.auth.PinDots
import dev.formaui.fintechuikit.components.auth.PinKeypad
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.BrandMotion
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/** Which half of the change-PIN flow the user is on. */
enum class PinStage { Choose, Confirm }

/**
 * Set a new card PIN: choose it, then confirm it.
 *
 * Both halves are the same surface with different copy, which is why this is one screen and one
 * `pin` value rather than two destinations — the user is answering one question twice, and a second
 * back-stack entry would let them land on "confirm" with nothing chosen.
 *
 * The mismatch path drives `PinDots`' documented contract exactly: the caller sets `isError` on the
 * false→true edge, clears the entry, and clears the error again on the next keystroke. The shake keys
 * on that edge, so a latched error would make the *second* wrong confirmation silent.
 *
 * There is no "current PIN" step. The demo has no stored card PIN to check against — `DemoPin` in
 * `MainActivity` gates app entry, not this card — and prompting for something nothing can verify
 * would be theatre.
 */
@Composable
fun ChangePinScreen(
    pin: String,
    onPinChange: (String) -> Unit,
    stage: PinStage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    length: Int = PinDefaults.Length,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    val reduced = LocalReducedMotion.current

    // Not FlowScreen, and not scrollable — the same reason PinEntryScreen isn't either. FlowScreen's
    // body is a vertical scroll, and content inside a scroll cannot be centred: it measures against
    // an unbounded height, so `Arrangement.Center` has no slack to distribute and everything stacks
    // at the top with the keypad stranded under it. Weighted spacers give the prompt the space above
    // the keypad and keep the keypad under the thumb.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FormaTheme.colorScheme.background)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FormaTopAppBar(
            title = "Change PIN",
            navigationIcon = {
                FormaIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = BrandDefaults.topAppBarColors,
        )

        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl),
        ) {
            // The same anchor PinEntryScreen uses, so the two PIN surfaces read as siblings. Static
            // across the step change: the text block below is what moves, and animating both would
            // make a two-line copy swap look like a screen transition.
            FormaAvatar(size = FormaAvatarSize.Large) {
                // The heading right below says what this is.
                Icon(Icons.Filled.Lock, contentDescription = null)
            }

            // The whole header moves as one, not just the prompt line. The step counter, title and
            // subtitle all change together, so animating them separately would stagger three pieces
            // of a single sentence.
            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    if (reduced) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        val enter = tween<Float>(
                            BrandMotion.EnterMillis,
                            easing = BrandMotion.EnterEasing,
                        )
                        val exit = tween<Float>(
                            BrandMotion.ExitMillis,
                            easing = BrandMotion.ExitEasing,
                        )
                        val enterOffset = tween<IntOffset>(
                            BrandMotion.EnterMillis,
                            easing = BrandMotion.EnterEasing,
                        )
                        val exitOffset = tween<IntOffset>(
                            BrandMotion.ExitMillis,
                            easing = BrandMotion.ExitEasing,
                        )
                        // Matches the flow-step motion the nav shell uses, so moving between the two
                        // halves of this screen feels like the same kind of forward step.
                        (
                            slideInHorizontally(enterOffset) { width -> width / 3 } +
                                fadeIn(enter)
                            ) togetherWith (
                            slideOutHorizontally(exitOffset) { width -> -width / 3 } +
                                fadeOut(exit)
                            )
                    }
                },
                label = "pinStage",
            ) { current ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
                ) {
                    // A two-step flow that never said it was two steps. The counter is the only
                    // thing telling the user that confirming is the last thing left, which matters
                    // most on the way back from a mismatch.
                    Text(
                        text = "Step ${if (current == PinStage.Choose) 1 else 2} of 2",
                        style = FormaTheme.typography.material.labelSmall,
                        color = FormaTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = when (current) {
                            PinStage.Choose -> "Choose a new PIN"
                            PinStage.Confirm -> "Enter it again"
                        },
                        style = FormaTheme.typography.material.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = when (current) {
                            PinStage.Choose -> "$length digits you'll remember at a card machine."
                            PinStage.Confirm -> "Just to be sure we got it right."
                        },
                        style = FormaTheme.typography.material.bodySmall,
                        color = FormaTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            PinDots(filled = pin.length, length = length, isError = isError)

            if (errorMessage != null) {
                // AuthErrorBanner, not a bare Text — it is the error surface every other auth screen
                // uses, and a red sentence here would have been the odd one out. No fillMaxWidth:
                // under centred dots the banner hugs its text rather than stretching across a screen
                // with no card to align to, matching PinEntryScreen.
                AuthErrorBanner(errorMessage)
            }
        }

        Spacer(Modifier.weight(1f))

        PinKeypad(
            onDigit = { digit -> if (pin.length < length) onPinChange(pin + digit) },
            onBackspace = { if (pin.isNotEmpty()) onPinChange(pin.dropLast(1)) },
            // No biometric slot: a fingerprint cannot tell you what digits to type.
            onBiometric = null,
            modifier = Modifier.padding(bottom = FinTechTheme.spacing.lg),
        )
    }
}

@Preview(name = "Change PIN · choose", heightDp = 820)
@Composable
private fun ChangePinChoosePreview() {
    FinTechTheme {
        ChangePinScreen(pin = "12", onPinChange = {}, stage = PinStage.Choose, onBack = {})
    }
}

/**
 * After a mismatch. The stage is [PinStage.Choose], not Confirm — the host sends the user back to
 * the start rather than re-asking them to confirm against a PIN they may have mistyped first, so a
 * Confirm-plus-error preview would show a state the flow never reaches.
 */
@Preview(name = "Change PIN · mismatch", heightDp = 820)
@Composable
private fun ChangePinMismatchPreview() {
    FinTechTheme {
        ChangePinScreen(
            pin = "",
            onPinChange = {},
            stage = PinStage.Choose,
            onBack = {},
            isError = true,
            errorMessage = "That didn't match. Choose a PIN again.",
        )
    }
}

@Preview(name = "Change PIN · confirm dark", heightDp = 820)
@Composable
private fun ChangePinConfirmDarkPreview() {
    FinTechTheme(darkTheme = true) {
        ChangePinScreen(pin = "1234", onPinChange = {}, stage = PinStage.Confirm, onBack = {})
    }
}
