/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.paymentcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.PageIndicator
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The wallet: the user's cards, one at a time, with the rest a swipe away.
 *
 * **Why a `HorizontalPager` and not `FormaCarousel`** — the reasoning is already written out at
 * `OnboardingScreen`'s doc comment and applies unchanged here: FormaUI's carousel is Material 3's
 * *browse* carousel, sized by an explicit `itemWidth`, and exposes no documented read of the settled
 * index. This screen needs that index for more than an indicator — every section below the wallet
 * (limit, activity, settings, chart) is scoped to whichever card is showing.
 *
 * Which is also why the selection is **hoisted** rather than kept in the pager. Onboarding's page
 * "dies with the screen"; this one decides what Details, Change PIN and Report lost act on, so it
 * belongs to the host.
 */
@Composable
fun CardWallet(
    cards: List<PaymentCard>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = selectedIndex.coerceIn(0, (cards.size - 1).coerceAtLeast(0)),
        pageCount = { cards.size },
    )

    // Report the settled page up. `currentPage` (not `targetPage`) so a swipe that is dragged back
    // does not leave the sections below describing a card the user never arrived at.
    LaunchedEffect(pagerState.currentPage) {
        onSelectedIndexChange(pagerState.currentPage)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            // The peek is the affordance: without an edge of the next card showing, a single card
            // face gives no hint that there is anything to swipe to. The horizontal inset lives
            // here rather than on the parent so the drag area still reaches both screen edges.
            contentPadding = PaddingValues(horizontal = FinTechTheme.spacing.xl),
            pageSpacing = FinTechTheme.spacing.md,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            PaymentCardArt(cards[page])
        }

        // One card is not a set to page through, so the dots would be a control with one position.
        if (cards.size > 1) {
            PageIndicator(pageCount = cards.size, currentPage = pagerState.currentPage)
        }
    }
}

@Composable
private fun WalletPreview(dark: Boolean, cards: List<PaymentCard> = SampleData.cards) {
    FinTechTheme(darkTheme = dark) {
        Column(
            Modifier
                .background(FormaTheme.colorScheme.background)
                .padding(vertical = FinTechTheme.spacing.lg),
        ) {
            CardWallet(cards = cards, selectedIndex = 0, onSelectedIndexChange = {})
        }
    }
}

@Preview(name = "CardWallet · light", heightDp = 300)
@Composable
private fun CardWalletPreview() = WalletPreview(dark = false)

@Preview(name = "CardWallet · dark", heightDp = 300)
@Composable
private fun CardWalletDarkPreview() = WalletPreview(dark = true)

/** A single card — the indicator should not appear. */
@Preview(name = "CardWallet · one card", heightDp = 300)
@Composable
private fun CardWalletSinglePreview() =
    WalletPreview(dark = false, cards = SampleData.cards.take(1))
