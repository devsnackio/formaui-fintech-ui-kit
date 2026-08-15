package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The detail screen, whose whole reason for existing is that it ships nothing inert — so most of
 * what is worth testing is which controls are *absent*.
 */
class TransactionDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    /** Sofia Almeida — a transfer, the only kind of transaction the app can actually repeat. */
    private val transfer = SampleData.transactions.first { it.id == "t6" }

    /** Pacific Gas — a failed card purchase, dated two days before [SampleData.today]. */
    private val failed = SampleData.transactions.first { it.id == "t8" }

    /**
     * "Send again" is offered only when the caller supplies an action. Repeating a card purchase is
     * not something the app can do, so for those the button is omitted rather than shown disabled.
     */
    @Test
    fun sendAgainIsAbsentWithoutAnAction() {
        compose.setThemedContent {
            TransactionDetailScreen(transfer, SampleData.today, onBack = {})
        }

        compose.onNodeWithText("Send again").assertDoesNotExist()
    }

    @Test
    fun sendAgainAppearsAndFiresWhenSupplied() {
        var repeats = 0
        compose.setThemedContent {
            TransactionDetailScreen(transfer, SampleData.today, onBack = {}, onRepeat = { repeats++ })
        }

        compose.onNodeWithText("Send again").performClick()

        assertEquals(1, repeats)
    }

    /**
     * The reference is derived from the date and the id, not taken from the id itself — `t8` under a
     * "Reference" label read as an unfinished feature. Pinned here because it is a pure function of
     * the fixture: `t8` is dated 2026-07-22, so it can only ever produce this string.
     */
    @Test
    fun referenceIsDerivedFromDateAndId() {
        compose.setThemedContent {
            TransactionDetailScreen(failed, SampleData.today, onBack = {})
        }

        compose.onNodeWithText("TX-20260722-0008", substring = true).assertExists()
    }

    /**
     * These screens have no bottom bar, so without the app-bar arrow the only way out is the system
     * gesture — which on a multi-step flow reads as being trapped.
     */
    @Test
    fun theBackArrowInvokesOnBack() {
        var backs = 0
        compose.setThemedContent {
            TransactionDetailScreen(transfer, SampleData.today, onBack = { backs++ })
        }

        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}
