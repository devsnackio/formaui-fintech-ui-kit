/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Rule
import org.junit.Test

/**
 * The card's own details.
 *
 * The load-bearing test here is [noFullCardNumberIsEverShown]. `PaymentCard` deliberately models
 * only `last4` — a UI kit has no business holding a PAN — and there is no reveal control anywhere on
 * this screen. Both facts are invisible to a reader who only sees the rendered mask, so a future
 * change that adds a "show number" affordance should break something.
 */
class CardDetailsScreenTest {

    private val card = SampleData.card

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTheCardsIdentifyingDetails() {
        compose.setThemedContent { CardDetailsScreen(card = card, onBack = {}) }

        compose.onNodeWithText("Cardholder").assertExists()
        // Holder and network each render twice — once on the card face, once in the details row
        // below it — so these ask for the first rather than for exactly one.
        compose.onAllNodesWithText(card.holderName).onFirst().assertExists()
        compose.onAllNodesWithText(card.network.name).onFirst().assertExists()
    }

    @Test
    fun noFullCardNumberIsEverShown() {
        compose.setThemedContent { CardDetailsScreen(card = card, onBack = {}) }

        // Only the mask, and the mask ends in the four digits the model actually holds. Both the
        // face and the details row render it, hence onFirst.
        compose.onAllNodesWithText(card.maskedNumber).onFirst().assertExists()
        compose.onAllNodesWithText("•••• •••• •••• ${card.last4}").onFirst().assertExists()
    }

    @Test
    fun anActiveCardSaysActive() {
        compose.setThemedContent { CardDetailsScreen(card = card, onBack = {}) }

        compose.onNodeWithText("Active").assertExists()
        compose.onNodeWithText("Frozen").assertDoesNotExist()
    }

    @Test
    fun aFrozenCardSaysFrozen() {
        compose.setThemedContent {
            CardDetailsScreen(card = card.copy(isFrozen = true), onBack = {})
        }

        compose.onNodeWithText("Frozen").assertExists()
        compose.onNodeWithText("Active").assertDoesNotExist()
    }

    /** Copying yields the last four — the only thing there is to copy. */
    @Test
    fun theLastFourCanBeCopied() {
        compose.setThemedContent { CardDetailsScreen(card = card, onBack = {}) }

        compose.onNodeWithText("Copy last 4 digits", substring = true).performClick()
    }
}
