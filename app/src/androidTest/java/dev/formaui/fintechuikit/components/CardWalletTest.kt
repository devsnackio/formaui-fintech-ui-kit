/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.components

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import dev.formaui.fintechuikit.components.paymentcard.CardWallet
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The wallet pager. Its reported index is load-bearing far beyond itself: every section of the Card
 * tab — limit, activity, settings, chart — is scoped to it, so a swipe that fails to report leaves
 * the screen describing a card that is no longer on it.
 */
class CardWalletTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun swipingReportsTheSettledPage() {
        var selected = -1
        compose.setThemedContent {
            CardWallet(
                cards = SampleData.cards,
                selectedIndex = 0,
                onSelectedIndexChange = { selected = it },
            )
        }

        compose.onNodeWithContentDescription("Page 1 of 2").assertExists()

        compose.onNode(hasScrollAction()).performTouchInput { swipeLeft() }
        compose.waitForIdle()

        assertEquals(1, selected)
        compose.onNodeWithContentDescription("Page 2 of 2").assertExists()
    }

    /** One card is not a set to page through, so the dots would be a control with one position. */
    @Test
    fun singleCardShowsNoIndicator() {
        compose.setThemedContent {
            CardWallet(
                cards = SampleData.cards.take(1),
                selectedIndex = 0,
                onSelectedIndexChange = {},
            )
        }

        compose.onNodeWithContentDescription("Page 1 of 1").assertDoesNotExist()
    }
}
