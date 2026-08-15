package dev.formaui.fintechuikit.screens

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The first step of a transfer, and the app's only affordability check.
 *
 * The CTA's `enabled` state is the whole product rule here: it gates on *funds plus fee*, not on the
 * amount alone, so an amount the balance could just about cover still fails once the fee is added.
 * That is the case a hand-check would miss and the one worth pinning.
 */
class TransferScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val funding: Account = SampleData.primaryAccount

    @Test
    fun continuingIsBlockedUntilAnAmountIsEntered() {
        compose.showTransfer(sendAmount = "")

        compose.onNodeWithText("Choose recipient").assertIsNotEnabled()
    }

    @Test
    fun aFundedAmountEnablesTheCta() {
        compose.showTransfer(sendAmount = "1000.00")

        compose.onNodeWithText("Choose recipient").assertIsEnabled()
    }

    /** $99,000 against a $12,480.65 balance — the "Transfer · insufficient funds" preview, asserted. */
    @Test
    fun anUnfundedAmountBlocksTheCta() {
        compose.showTransfer(sendAmount = "99000.00")

        compose.onNodeWithText("Choose recipient").assertIsNotEnabled()
    }

    /**
     * With no funding account there is nothing to check against, so the screen must not *invent* a
     * failure — `canCover` is only consulted when an account is actually present.
     */
    @Test
    fun anAmountIsAllowedWhenThereIsNoFundingAccountToCheck() {
        compose.showTransfer(sendAmount = "1000.00", fundingAccount = null)

        compose.onNodeWithText("Choose recipient").assertIsEnabled()
    }

    @Test
    fun theCtaLeadsToRecipientSelection() {
        var continued = false
        compose.showTransfer(sendAmount = "1000.00", onContinue = { continued = true })

        compose.onNodeWithText("Choose recipient").performClick()

        assertTrue(continued)
    }

    @Test
    fun theBackArrowInvokesOnBack() {
        var backs = 0
        compose.showTransfer(onBack = { backs++ })

        compose.onNodeWithText("Send money").assertExists()
        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}

private fun ComposeContentTestRule.showTransfer(
    sendAmount: String = "1000.00",
    fundingAccount: Account? = SampleData.primaryAccount,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
) {
    setThemedContent {
        TransferScreen(
            sendAmount = sendAmount,
            onSendAmountChange = {},
            rate = SampleData.usdToEur,
            fundingAccount = fundingAccount,
            onSourceCurrencyChange = {},
            onTargetCurrencyChange = {},
            onSwapCurrencies = {},
            onBack = onBack,
            onContinue = onContinue,
        )
    }
}
