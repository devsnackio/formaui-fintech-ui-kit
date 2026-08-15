/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
// ExperimentalMaterial3Api is required by FormaTopAppBar's call site, not by anything here: its
// scrollBehavior parameter is typed TopAppBarScrollBehavior, which M3 still marks experimental, so
// the opt-in is needed even though this shell passes no scroll behaviour. The FormaUI reference
// documents this on the TopAppBar entry.
@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalMaterial3Api::class)

package dev.formaui.fintechuikit.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.components.topappbar.FormaTopAppBar
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The shell every non-tab flow screen sits in — a back bar, a scrolling body, and a pinned footer.
 *
 * It exists because the shell hands these screens the whole window. `FinTechNavHost` shows the
 * bottom bar and passes `innerPadding` only on the four tab destinations, so each flow screen has to
 * supply its own background and [safeDrawingPadding]; getting that wrong puts content under the
 * status bar or double-insets it. Rather than repeat the chain on six screens, it lives here once.
 *
 * The [footer] is **outside** the scrolling body on purpose. A flow's primary action should not be
 * something the user has to scroll to find, and on a form the keyboard already eats half the screen.
 * The body scrolls under a fixed CTA instead.
 *
 * A back affordance is required, not optional: these screens have no bottom bar, so without the bar
 * in the app bar the only way out is the system gesture — and on a multi-step flow that reads as
 * being trapped.
 *
 * @param title the app-bar title, which is also the screen's accessible label.
 * @param onBack invoked by the back arrow; wire it to `navController.popBackStack()`.
 * @param footer the pinned action area. Pass `null` for a screen with no primary action.
 * @param snackbarHost an optional host overlaid at the bottom, for screens that need to confirm
 *   or offer to undo something. `FormaSnackbarHost` is documented for a `Scaffold`'s slot and this
 *   shell is not a `Scaffold`, so it needs somewhere to live.
 * @param content the scrolling body, laid out in a `ColumnScope` with `{spacing.lg}` between items.
 */
@Composable
fun FlowScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
    snackbarHost: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        FlowScreenBody(title, onBack, footer, content)

        // The host overlays the whole screen rather than sitting in the column, so a snackbar floats
        // over the footer instead of shoving the CTA up when it appears and dropping it when it
        // leaves. That is how Scaffold places its own host, and it is the reason this is a Box.
        if (snackbarHost != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(FinTechTheme.spacing.lg),
            ) {
                snackbarHost()
            }
        }
    }
}

@Composable
private fun FlowScreenBody(
    title: String,
    onBack: () -> Unit,
    footer: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FormaTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        FormaTopAppBar(
            title = title,
            navigationIcon = {
                FormaIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            // Transparent, so the bar shows the background this screen paints two lines above rather
            // than M3's default `surface` — which is a different tone here and banded the top of
            // every flow screen. Shared with the tab shell via BrandDefaults so the two match.
            colors = BrandDefaults.topAppBarColors,
        )

        // No entrance animation here. A flow screen is a single question — an amount, a recipient —
        // so its body should enter as one block, and `FinTechNavHost` already does exactly that:
        // every route into this screen carries a `fadeIn`. Fading it again here would compound with
        // that rather than reinforce it; see `screenMotion` in FinTechNavHost for why.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = FinTechTheme.spacing.lg,
                    vertical = FinTechTheme.spacing.lg,
                ),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
            content = content,
        )

        if (footer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = FinTechTheme.spacing.lg,
                        end = FinTechTheme.spacing.lg,
                        // Less below than above: the safe-drawing inset already sits under this.
                        top = FinTechTheme.spacing.md,
                        bottom = FinTechTheme.spacing.lg,
                    ),
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
            ) {
                footer()
            }
        }
    }
}

@Preview(name = "FlowScreen · light", heightDp = 500)
@Composable
private fun FlowScreenPreview() {
    FinTechTheme {
        FlowScreen(
            title = "Send money",
            onBack = {},
            footer = { Text("footer / CTA sits here") },
        ) {
            Text("Body content, scrolls under the footer.")
        }
    }
}

@Preview(name = "FlowScreen · dark", heightDp = 500)
@Composable
private fun FlowScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        FlowScreen(
            title = "Send money",
            onBack = {},
            footer = { Text("footer / CTA sits here") },
        ) {
            Text("Body content, scrolls under the footer.")
        }
    }
}
