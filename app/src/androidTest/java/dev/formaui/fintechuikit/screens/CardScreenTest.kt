package dev.formaui.fintechuikit.screens

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The Card tab's product rules — the ones that are decisions rather than layout.
 *
 * Card 0 is the physical Visa, card 1 the virtual Mastercard. That pairing is what makes the
 * capability rules testable at all: a virtual card has no chip and no plastic, so two of the three
 * settings rows must not merely be off, they must not be there.
 */
class CardScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun physicalCardOffersEveryCapability() {
        compose.showCardScreen(selectedIndex = 0)

        compose.onNodeWithText("Contactless").assertExists()
        compose.onNodeWithText("Online payments").assertExists()
        compose.onNodeWithText("ATM withdrawals").assertExists()
    }

    /**
     * A permanently-off switch for something the card cannot do is the disabled-control version of a
     * dead affordance, so the rows are absent rather than unchecked.
     */
    @Test
    fun virtualCardHidesTheCapabilitiesItCannotHave() {
        compose.showCardScreen(selectedIndex = 1)

        compose.onNodeWithText("Contactless").assertDoesNotExist()
        compose.onNodeWithText("ATM withdrawals").assertDoesNotExist()
        // A virtual card still pays online — that one is a real setting.
        compose.onNodeWithText("Online payments").assertExists()
    }

    @Test
    fun togglingACapabilityReportsTheNewValue() {
        var reported: Boolean? = null
        compose.showCardScreen(onOnlinePaymentsChange = { reported = it })

        compose.onNodeWithText("Online payments").performScrollTo().performClick()

        // The fixture ships with online payments on, so the first tap turns it off.
        assertEquals(false, reported)
    }

    @Test
    fun spendingPastTheLimitIsCalledOut() {
        val physical = SampleData.cards[0]
        val overspent = physical.copy(
            spentThisMonth = physical.monthlyLimit.copy(minorUnits = 400_000L),
        )
        compose.showCardScreen(cards = listOf(overspent))

        compose.onNodeWithText("Over limit").assertExists()
    }

    /** Cancelling a card is destructive and irreversible in the demo, so it asks first. */
    @Test
    fun reportingLostAsksBeforeActing() {
        var reported = false
        compose.showCardScreen(onReportLost = { reported = true })

        compose.onNodeWithText("Report lost or stolen", substring = true)
            .performScrollTo()
            .performClick()

        compose.onNodeWithText("Report this card lost?").assertExists()
        assertFalse("The card must not be cancelled until the dialog is confirmed", reported)
    }

    @Test
    fun dismissingTheDialogLeavesTheCardAlone() {
        var reported = false
        compose.showCardScreen(onReportLost = { reported = true })

        compose.onNodeWithText("Report lost or stolen", substring = true)
            .performScrollTo()
            .performClick()
        compose.onNodeWithText("Keep card").performClick()

        compose.onNodeWithText("Report this card lost?").assertDoesNotExist()
        assertFalse(reported)
    }

    @Test
    fun confirmingTheDialogCancelsTheCard() {
        var reported = false
        compose.showCardScreen(onReportLost = { reported = true })

        compose.onNodeWithText("Report lost or stolen", substring = true)
            .performScrollTo()
            .performClick()
        // Exact match: the trigger button's label is "  Report lost or stolen", not this.
        compose.onNodeWithText("Report lost").performClick()

        assertTrue(reported)
    }

    /**
     * The slider is continuous — 94 tick marks made the track unreadable — so the $100 step is
     * enforced on the way out instead. This is the test that keeps the two facts consistent.
     */
    @Test
    fun theLimitSliderReportsWholeHundreds() {
        var reported: Long? = null
        compose.showCardScreen(onLimitChange = { reported = it })

        // The slider is the only node that can be *set*; the limit meter above it publishes a range
        // but no SetProgress action, so this matches one node.
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(237_431f) }

        assertEquals(240_000L, reported)
    }
}

/**
 * `CardScreen` takes thirteen callbacks; naming only the ones a given test cares about keeps each
 * test about its own rule.
 */
private fun ComposeContentTestRule.showCardScreen(
    cards: List<PaymentCard> = SampleData.cards,
    selectedIndex: Int = 0,
    onLimitChange: (Long) -> Unit = {},
    onOnlinePaymentsChange: (Boolean) -> Unit = {},
    onReportLost: () -> Unit = {},
) {
    setThemedContent {
        CardScreen(
            cards = cards,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = {},
            monthlySpendByCard = SampleData.monthlySpendByCard,
            transactions = SampleData.transactions,
            onToggleFreeze = {},
            onLimitChange = onLimitChange,
            onContactlessChange = {},
            onOnlinePaymentsChange = onOnlinePaymentsChange,
            onAtmChange = {},
            onReportLost = onReportLost,
            onTransactionClick = {},
            onSeeAllActivity = {},
        )
    }
}
