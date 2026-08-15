/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion
import kotlin.math.roundToInt

/** Sizing shared by [PinDots] and the screens that host it. */
object PinDefaults {
    /** Shortest PIN the dot row supports. */
    const val MinLength = 4

    /** Longest. Past six the row stops being countable at a glance, which is its whole job. */
    const val MaxLength = 6

    /** The kit's default — six digits. */
    const val Length = 6
}

private val DotSize = 14.dp
private val DotBorderWidth = 1.5.dp

/** How far the row travels at the peak of the wrong-PIN shake. */
private val ShakeDistance = 10.dp

/**
 * The filled/empty dots that stand in for a PIN's text field.
 *
 * A PIN entry has no visible input to focus, so the dots have to carry the whole state of the
 * field: how many digits are in, and whether the last attempt was rejected.
 *
 * **The shake is keyed on [isError] going false → true**, which makes the host's error handling
 * part of this component's contract: clear the error as soon as new input arrives (the natural
 * place is the screen's `onPinChange`), or a second wrong attempt in a row leaves [isError]
 * already true and the row sits still instead of shaking. The animation clock lives here rather
 * than being hoisted because it is derived entirely from [isError] — no caller has a reason to
 * read or drive it.
 *
 * Each dot animates between empty and filled rather than swapping instantly, so a keypress has
 * visible feedback in the field itself — the keypad's own press-scale confirms the *button* was
 * hit, not that the digit was accepted. Backspace runs the same animation backwards.
 *
 * Colour deliberately avoids `colorScheme.primary`: the filled dots are progress, not a primary
 * action, and DESIGN.md is explicit that Wise green belongs to the CTA and to nothing else.
 */
@Composable
fun PinDots(
    filled: Int,
    modifier: Modifier = Modifier,
    length: Int = PinDefaults.Length,
    isError: Boolean = false,
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (!isError) return@LaunchedEffect
        // A decaying left-right swing: overshoot hard, then settle. Ends on 0f so the row is
        // back on its layout position whether or not the error is still showing.
        shake.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 400
                0f at 0
                -1f at 60
                1f at 120
                -0.6f at 180
                0.6f at 240
                -0.25f at 310
                0f at 400
            },
        )
    }

    val filledColor = if (isError) {
        FormaTheme.colorScheme.error
    } else {
        FormaTheme.colorScheme.onSurface
    }
    val emptyColor = if (isError) {
        FormaTheme.colorScheme.error
    } else {
        FormaTheme.colorScheme.outlineVariant
    }

    Row(
        modifier = modifier
            .offset { IntOffset(x = (shake.value * ShakeDistance.toPx()).roundToInt(), y = 0) }
            .semantics {
                // The dots are the field, so they owe the announcement a text field would give.
                // Polite, so each keypress reports progress without cutting off the key's label.
                contentDescription = "$filled of $length digits entered"
                liveRegion = LiveRegionMode.Polite
            },
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
    ) {
        val reduced = LocalReducedMotion.current
        repeat(length) { index ->
            // One value per dot: 0 empty, 1 filled. Animating it rather than swapping the modifier
            // is what makes a keypress feel like it landed — the dot grows into place instead of
            // appearing between frames, and backspace shrinks it away again.
            val fill by animateFloatAsState(
                targetValue = if (index < filled) 1f else 0f,
                animationSpec = if (reduced) {
                    snap()
                } else {
                    // Deliberately a tween, not a spring. The success screen's badge is the app's
                    // only spring, and that is what makes it read as an event; a bouncing PIN dot
                    // would spend the same trick on a keystroke.
                    tween(durationMillis = 140, easing = FastOutSlowInEasing)
                },
                label = "pinDotFill",
            )

            Box(
                Modifier
                    .size(DotSize)
                    .drawBehind {
                        val maxRadius = size.minDimension / 2f

                        // The outline fades as the fill grows, so the two never both read at full
                        // strength and the dot has one appearance at any moment.
                        if (fill < 1f) {
                            drawCircle(
                                color = emptyColor,
                                radius = maxRadius - DotBorderWidth.toPx() / 2f,
                                alpha = 1f - fill,
                                style = Stroke(width = DotBorderWidth.toPx()),
                            )
                        }
                        if (fill > 0f) {
                            drawCircle(color = filledColor, radius = maxRadius * fill)
                        }
                    },
            )
        }
    }
}

@Preview(name = "PinDots · light")
@Composable
private fun PinDotsPreview() {
    FinTechTheme {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            PinDots(filled = 0)
            PinDots(filled = 3)
            PinDots(filled = 6)
            PinDots(filled = 4, length = PinDefaults.MinLength)
            PinDots(filled = 6, isError = true)
        }
    }
}

@Preview(name = "PinDots · dark")
@Composable
private fun PinDotsDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            PinDots(filled = 0)
            PinDots(filled = 3)
            PinDots(filled = 6)
            PinDots(filled = 4, length = PinDefaults.MinLength)
            PinDots(filled = 6, isError = true)
        }
    }
}
