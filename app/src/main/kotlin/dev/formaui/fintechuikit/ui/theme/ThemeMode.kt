/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * The user's appearance preference.
 *
 * [System] defers to the OS setting; [Light] and [Dark] pin the app regardless of it. The
 * choice is resolved to a plain `darkTheme` boolean by [resolveDark] and handed to
 * [FinTechTheme], so the theme itself stays a pure function of that boolean.
 */
enum class ThemeMode(val displayName: String) {
    System("System"),
    Light("Light"),
    Dark("Dark"),
}

/**
 * Resolves the preference to `darkTheme`.
 *
 * `@Composable` because [System] reads [isSystemInDarkTheme], which is composition state — when
 * the OS flips light/dark, a `System`-mode app recomposes and follows along.
 */
@Composable
fun ThemeMode.resolveDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}
