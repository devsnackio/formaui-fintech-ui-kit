package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** The details someone else needs in order to pay you. */
class ReceiveScreenTest {

    private val handle = "alex.morgan@forma.example"

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTheHandleAndItsCurrency() {
        compose.setThemedContent {
            ReceiveScreen(account = SampleData.primaryAccount, handle = handle, onBack = {})
        }

        compose.onNodeWithText(handle).assertExists()
        // The account name appears as both the Account row's value and the Currency row's, so this
        // asks for the first rather than for exactly one.
        compose.onAllNodesWithText(SampleData.primaryAccount.label).onFirst().assertExists()
    }

    /** Copy is a real clipboard write, not a placeholder — the label is only offered because it works. */
    @Test
    fun copyDetailsIsPresentAndClickable() {
        compose.setThemedContent {
            ReceiveScreen(account = SampleData.primaryAccount, handle = handle, onBack = {})
        }

        compose.onNodeWithText("Copy details", substring = true).performClick()
    }

    @Test
    fun theBackArrowInvokesOnBack() {
        var backs = 0
        compose.setThemedContent {
            ReceiveScreen(SampleData.primaryAccount, handle, onBack = { backs++ })
        }

        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}
