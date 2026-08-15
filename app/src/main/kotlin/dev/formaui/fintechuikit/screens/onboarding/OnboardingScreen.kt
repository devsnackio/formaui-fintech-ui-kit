/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonDefaults
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.onboarding.OnboardingIllustration
import dev.formaui.fintechuikit.components.common.PageIndicator
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import kotlinx.coroutines.launch

/**
 * The paged intro: [pages] slides over a Skip action, a position indicator, and one CTA.
 *
 * **Why a `HorizontalPager` and not `FormaCarousel`.** FormaUI's carousel is Material 3's
 * *browse* carousel: `MultiBrowse` flexes its items to large/medium/small so neighbours stay
 * visible at the edges, `Uncontained` defaults to a no-snap fling, and both are sized by an
 * explicit `itemWidth` rather than filling the viewport. It is built for a strip of things to
 * look through, not for full-bleed pages that settle one at a time. It also gives no documented
 * read of the settled index or way to advance programmatically, which this screen needs twice
 * over — to drive the indicator and to make Next move the pager. `HorizontalPager` supplies both
 * through `PagerState`.
 *
 * The pager position is remembered here rather than hoisted. It matches how `TransferScreen`
 * owns its review-sheet flag: which slide you are looking at dies with the screen and means
 * nothing to the rest of the app, unlike the session state `MainActivity` holds.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    pages: List<OnboardingPage> = OnboardingPages,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val onLastPage = pagerState.currentPage >= pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            // Onboarding runs outside the tab Scaffold, so this screen owns its own surface and
            // insets — see WelcomeScreen for the full note.
            .background(FormaTheme.colorScheme.background)
            .safeDrawingPadding()
            // Vertical only. The `{spacing.xl}` 24dp gutter is applied per child below instead of
            // here, because a horizontal inset on this Column would inset the pager with it — and
            // a pager narrower than the screen turns the outer 24dp into dead zones that swallow
            // edge swipes, which is where a thumb naturally starts one.
            .padding(vertical = FinTechTheme.spacing.lg),
    ) {
        // Skip retires on the last slide, where the CTA already is the way out. The row keeps
        // the button's own touch-target height either way, so the deck below doesn't jump up a
        // row on the final swipe.
        Box(
            modifier = Modifier
                // Padding before fillMaxWidth: it shrinks the space the row then fills, insetting
                // the row itself. The other order would leave the row full-width and inset only
                // its contents.
                .padding(horizontal = FinTechTheme.spacing.xl)
                .fillMaxWidth()
                .height(FormaButtonDefaults.MinTouchTargetSize),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (!onLastPage) {
                FormaButton(
                    onClick = onSkip,
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                ) {
                    Text("Skip")
                }
            }
        }

        // Full width by design — the slide's own gutter lives inside OnboardingSlide, so the
        // pager's drag area reaches both screen edges.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingSlide(pages[page])
        }

        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = FinTechTheme.spacing.xl),
        )

        FormaButton(
            onClick = {
                if (onLastPage) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            shape = BrandDefaults.buttonShape,
            contentPadding = BrandDefaults.buttonContentPadding,
            modifier = Modifier
                .padding(horizontal = FinTechTheme.spacing.xl)
                .fillMaxWidth(),
        ) {
            Text(if (onLastPage) "Get started" else "Next")
        }
    }
}

/**
 * One slide: illustration, title, body — centred, unlike the start-aligned welcome hero, because
 * a pager reads as a sequence of self-contained cards rather than one continuous page.
 *
 * Each slide scrolls independently so a long body at a large font scale can't push the
 * illustration off its own page.
 *
 * The slide carries its own `{spacing.xl}` gutter rather than inheriting one from the screen: the
 * page fills the pager edge to edge so the whole width is draggable, and only the content within
 * it is inset.
 */
@Composable
private fun OnboardingSlide(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FinTechTheme.spacing.xl)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingIllustration(icon = page.icon, accent = page.accent)
        Spacer(Modifier.height(FinTechTheme.spacing.xxl))
        Text(
            page.title,
            style = FormaTheme.typography.material.headlineMedium,
            color = FormaTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(FinTechTheme.spacing.md))
        Text(
            page.body,
            style = FormaTheme.typography.material.bodyLarge,
            color = FormaTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Onboarding · light", heightDp = 800)
@Composable
private fun OnboardingScreenPreview() {
    FinTechTheme {
        OnboardingScreen(onFinish = {}, onSkip = {})
    }
}

@Preview(name = "Onboarding · dark", heightDp = 800)
@Composable
private fun OnboardingScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        OnboardingScreen(onFinish = {}, onSkip = {})
    }
}

/**
 * The terminal state: Skip retired, CTA reading "Get started".
 *
 * Rendered as a one-slide deck because the screen owns its pager position, so a preview cannot
 * start it on page four — hence the single indicator dot, which is the deck being short rather
 * than the last page losing its siblings.
 */
@Preview(name = "Onboarding · last slide", heightDp = 800)
@Composable
private fun OnboardingScreenLastPagePreview() {
    FinTechTheme {
        OnboardingScreen(
            onFinish = {},
            onSkip = {},
            pages = OnboardingPages.takeLast(1),
        )
    }
}
