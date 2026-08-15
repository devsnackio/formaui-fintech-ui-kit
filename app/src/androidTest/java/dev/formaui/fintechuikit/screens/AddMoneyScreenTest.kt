package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Topping the balance up. Every [TopUpSource] must be reachable and must report itself — a picker
 * whose options all resolve to the same value looks correct and is not.
 *
 * The segments are labelled with the enum's `name`, not its `label`: the label is the full
 * counterparty string ("Visa •••• 4821"), too long for a segment, and appears in the summary line
 * underneath instead.
 */
class AddMoneyScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun offersEveryFundingSource() {
        compose.showAddMoney()

        TopUpSource.entries.forEach { source ->
            compose.onNodeWithText(source.name).assertExists()
        }
    }

    /** The chosen source is spelled out in full below the segments, where there is room for it. */
    @Test
    fun theChosenSourceIsNamedInFull() {
        compose.showAddMoney()

        compose.onNodeWithText(TopUpSource.Card.label, substring = true).assertExists()
    }

    /**
     * One composition, every segment tapped in turn — a test rule can only set content once, and
     * the screen is stateless anyway, so the selected source never moves. Collecting into a list
     * rather than asserting per tap means a picker that reports the same value for all three fails
     * on the comparison instead of on whichever segment happened to be checked first.
     */
    @Test
    fun eachSourceReportsItself() {
        val reported = mutableListOf<TopUpSource>()
        compose.showAddMoney(onSourceChange = { reported += it })

        TopUpSource.entries.forEach { source ->
            compose.onNodeWithText(source.name).performClick()
        }

        assertEquals(TopUpSource.entries.toList(), reported)
    }

    /**
     * The cross-link to Receive is optional, and omitted rather than inert when the caller has
     * nowhere to send the user.
     */
    @Test
    fun theReceiveLinkIsAbsentUnlessSupplied() {
        compose.showAddMoney()

        compose.onNodeWithText("Get paid by someone else").assertDoesNotExist()
    }

    @Test
    fun theReceiveLinkFiresWhenSupplied() {
        var opened = false
        compose.showAddMoney(onReceive = { opened = true })

        // No scrollTo: this sits in FlowScreen's pinned footer, which is outside the scrolling body.
        compose.onNodeWithText("Get paid by someone else").performClick()

        assertTrue(opened)
    }
}

private fun ComposeContentTestRule.showAddMoney(
    amount: String = "200.00",
    onSourceChange: (TopUpSource) -> Unit = {},
    onConfirm: () -> Unit = {},
    onReceive: (() -> Unit)? = null,
) {
    setThemedContent {
        AddMoneyScreen(
            amount = amount,
            onAmountChange = {},
            source = TopUpSource.Card,
            onSourceChange = onSourceChange,
            destination = SampleData.primaryAccount,
            onBack = {},
            onConfirm = onConfirm,
            onReceive = onReceive,
        )
    }
}
