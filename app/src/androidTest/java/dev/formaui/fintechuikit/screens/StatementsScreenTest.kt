package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.StatementDirection
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * The statement view — the one screen that answers "what did I spend in July?".
 *
 * Its rows look identical to the ones on Dashboard and Activity, and for a while they were the only
 * ones that did nothing when tapped. That is what [rowsAreTappableLikeEverywhereElse] guards.
 */
class StatementsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    /**
     * "Money in" and "Money out" each appear twice — once as a direction filter tab, once as a
     * totals label — so these count rather than demand a single node. `Net` is the totals row's own
     * word and has no tab, which is what makes it a useful third assertion.
     */
    @Test
    fun totalsAreLabelled() {
        compose.showStatements()

        compose.onAllNodesWithText("Money in").assertCountEquals(2)
        compose.onAllNodesWithText("Money out").assertCountEquals(2)
        compose.onNodeWithText("Net").assertExists()
    }

    @Test
    fun theInDirectionKeepsOnlyCredits() {
        compose.showStatements(direction = StatementDirection.In)

        compose.onNodeWithText("Acme Corp").assertExists()
        compose.onNodeWithText("Whole Foods").assertDoesNotExist()
    }

    @Test
    fun theOutDirectionKeepsOnlyDebits() {
        compose.showStatements(direction = StatementDirection.Out)

        compose.onNodeWithText("Whole Foods").assertExists()
        compose.onNodeWithText("Acme Corp").assertDoesNotExist()
    }

    /** A range with nothing in it explains itself and says how to widen it. */
    @Test
    fun anEmptyRangeSaysSo() {
        compose.showStatements(
            start = LocalDate.of(2020, 1, 1),
            end = LocalDate.of(2020, 1, 31),
        )

        compose.onNodeWithText("Nothing in this range").assertExists()
    }

    @Test
    fun rowsAreTappableLikeEverywhereElse() {
        var tapped: Transaction? = null
        compose.showStatements(
            direction = StatementDirection.In,
            onTransactionClick = { tapped = it },
        )

        compose.onNodeWithText("Acme Corp").performScrollTo().performClick()

        assertEquals("t3", tapped?.id)
    }

    @Test
    fun theStatementCanBeCopied() {
        compose.showStatements()

        // No scrollTo: the export action lives in FlowScreen's pinned footer, outside the scroll.
        compose.onNodeWithText("Copy statement", substring = true).assertExists()
    }
}

private fun ComposeContentTestRule.showStatements(
    start: LocalDate = SampleData.today.minusMonths(1),
    end: LocalDate = SampleData.today,
    direction: StatementDirection = StatementDirection.All,
    onTransactionClick: (Transaction) -> Unit = {},
) {
    setThemedContent {
        StatementsScreen(
            transactions = SampleData.transactions,
            startDate = start,
            endDate = end,
            onRangeChange = { _, _ -> },
            direction = direction,
            onDirectionChange = {},
            // The full width of the screen's own slider — `MaxAmountMinor`, mirrored here because it
            // is private. Every fixture sits inside it, so this filters nothing out.
            amountRange = 0f..500_000f,
            onAmountRangeChange = {},
            onTransactionClick = onTransactionClick,
            onBack = {},
        )
    }
}
