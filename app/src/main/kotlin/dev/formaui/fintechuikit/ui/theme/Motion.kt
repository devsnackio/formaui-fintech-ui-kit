/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * The app's motion vocabulary — the two durations and two easings every animation here shares.
 *
 * DESIGN.md specifies **no** motion at all: no durations, no easing, no transition specs. What it
 * does specify is a depth model — "surface contrast IS the elevation", Level 0 flat and shadowless —
 * and a palette that carries state through colour rather than shadow. So the motion here is
 * deliberately short and quiet; a brand with no shadows should not suddenly acquire dramatic motion.
 *
 * The numbers are pitched to sit alongside `FormaPressScaleDefaults`, the only motion spec already
 * running app-wide (a 100ms tween down, a medium-stiffness low-bounce spring back, on every
 * `FormaButton`, `FormaChip`, `FormaCard` and `FormaIconButton` press). New motion should read as the
 * same hand — brisk, barely bouncy — not as a second motion language layered on top.
 *
 * **There is deliberately no screen-entrance helper here.** Screen-level motion belongs entirely to
 * `FinTechNavHost`'s `screenMotion`; a screen that fades its own content in on top of the host's fade
 * compounds with it instead of reinforcing it, and the result reads as lag. That function carries the
 * full reasoning. What is left in this file is the shared vocabulary and the reduced-motion signal.
 */
object BrandMotion {

    /** Incoming screens and reveals. Long enough to read as motion, short enough not to gate a tap. */
    const val EnterMillis = 260

    /**
     * Outgoing screens. Deliberately shorter than [EnterMillis]: when the two overlap, the outgoing
     * screen clearing first is what stops the pair looking like a cross-dissolve smear.
     */
    const val ExitMillis = 200

    /** Entrances decelerate into place. */
    val EnterEasing = FastOutSlowInEasing

    /** Exits start at full speed and ease out, so departure feels like release rather than a pull. */
    val ExitEasing = LinearOutSlowInEasing
}

/**
 * Whether the user has asked the system for no animation.
 *
 * Provided by `FinTechTheme`; defaults to `false` so a composable used outside the theme (or in a
 * `@Preview`) still renders.
 *
 * The rule for anything reading this: when it is true, render the plain thing — do **not** animate to
 * a no-op. A zero-duration tween still allocates an `Animatable`, still runs a `LaunchedEffect`, and
 * still adds a layer; skipping the animation outright is both cheaper and the honest reading of "the
 * user does not want motion". `SuccessScreen`'s badge starts settled rather than springing to 1f,
 * `HelpScreen`'s chevron snaps, and `MonthlySpendCard` passes a null animation spec so the chart
 * draws its final frame.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Reads the platform's animator duration scale and reports whether animation is switched off.
 *
 * `ANIMATOR_DURATION_SCALE` is the setting behind Developer options → "Animator duration scale" and
 * the accessibility "Remove animations" toggle; `0f` means the user does not want motion. There is no
 * Compose API for this — `LocalAccessibilityManager` does not expose it — so it is read directly.
 *
 * Wrapped in `runCatching` because `Settings.Global` can throw on an unusual or restricted platform
 * image, and a missing setting must never take a screen down for the sake of an animation. Read once
 * per composition rather than observed: changing it restarts the process anyway.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    // Previews have no real Settings provider, and animating in a preview is pointless besides.
    val inspecting = LocalInspectionMode.current
    return remember(context, inspecting) {
        if (inspecting) {
            false
        } else {
            runCatching {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f,
                ) == 0f
            }.getOrDefault(false)
        }
    }
}
