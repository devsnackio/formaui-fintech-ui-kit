/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaColorScheme
import dev.formaui.core.theme.FormaSpacing
import dev.formaui.core.theme.FormaTheme

/**
 * The FormaFinTechUiKit brand theme — DESIGN.md's design system layered over `FormaTheme`.
 *
 * Wrap the app root, every standalone screen, and every `@Preview` in this rather than in
 * `FormaTheme` or `MaterialTheme` directly.
 *
 * Dynamic color is off by design: the brand's identity rests on a single lime-green accent,
 * and letting the device wallpaper recolour it would defeat the point.
 */
@Composable
fun FinTechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Keep the system-bar icon tint in sync with the *app's* resolved theme. enableEdgeToEdge()
    // decides this once from the system setting at startup, so without this the status/nav-bar
    // icons don't flip when the user changes Appearance in-app (dark icons on a dark bar, etc.).
    // Runs as a SideEffect on every darkTheme change; skipped in @Preview (no host Activity).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity().window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    FormaTheme(
        colorScheme = FormaColorScheme(
            light = BrandLightColorScheme,
            dark = BrandDarkColorScheme,
        ),
        typography = BrandTypography,
        shapes = BrandShapes,
        darkTheme = darkTheme,
        dynamicColor = false,
    ) {
        CompositionLocalProvider(
            LocalBrandSpacing provides BrandSpacing,
            LocalBrandColors provides if (darkTheme) DarkBrandColors else LightBrandColors,
            // Resolved once here so every screen, the nav shell, and the charts read the same
            // answer, instead of each reading Settings.Global for itself.
            LocalReducedMotion provides rememberReducedMotion(),
            content = content,
        )
    }
}

/**
 * Unwraps the host [Activity] from a view's [Context].
 *
 * `LocalView.current.context` is often a `ContextWrapper` (not the Activity directly), so a raw
 * `as Activity` cast can throw. Walking the wrapper chain is the safe way to reach the window.
 */
private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("No Activity found from context")
}

/**
 * Accessors for the brand tokens `FormaTheme` has no room for.
 *
 * Deliberately mirrors how `FormaTheme` exposes `colorScheme` / `typography` / `shapes` /
 * `spacing`, so call sites read the same way — `FinTechTheme.spacing.lg` alongside
 * `FormaTheme.shapes.xl`.
 */
object FinTechTheme {
    /**
     * The brand spacing scale.
     *
     * Prefer this over `FormaTheme.spacing`, which is one step coarser and cannot be
     * overridden — see [BrandSpacing].
     */
    val spacing: FormaSpacing
        @Composable @ReadOnlyComposable get() = LocalBrandSpacing.current

    /** DESIGN.md colour tokens with no Material 3 slot — the semantic and accent families. */
    val brandColors: BrandColors
        @Composable @ReadOnlyComposable get() = LocalBrandColors.current
}
