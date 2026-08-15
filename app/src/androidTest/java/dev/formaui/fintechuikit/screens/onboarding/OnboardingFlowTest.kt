package dev.formaui.fintechuikit.screens.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The two screens in front of the session: Welcome, and the paged tour.
 *
 * Both offer a way past themselves, which is the point of the tests — an onboarding flow with no
 * escape is the worst possible first impression, and Skip is the kind of control that quietly stops
 * working when the pager is refactored.
 */
class OnboardingFlowTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun welcomeOffersBothRoutes() {
        compose.setThemedContent { WelcomeScreen(onGetStarted = {}, onSignIn = {}) }

        compose.onNodeWithText("Get started").assertExists()
        compose.onNodeWithText("I already have an account").assertExists()
    }

    @Test
    fun welcomeGetStartedNavigates() {
        var started = false
        compose.setThemedContent { WelcomeScreen(onGetStarted = { started = true }, onSignIn = {}) }

        compose.onNodeWithText("Get started").performClick()

        assertTrue(started)
    }

    @Test
    fun welcomeSignInNavigates() {
        var signIn = false
        compose.setThemedContent { WelcomeScreen(onGetStarted = {}, onSignIn = { signIn = true }) }

        compose.onNodeWithText("I already have an account").performClick()

        assertTrue(signIn)
    }

    @Test
    fun theTourStartsOnItsFirstPage() {
        compose.setThemedContent { OnboardingScreen(onFinish = {}, onSkip = {}) }

        compose.onNodeWithText(OnboardingPages.first().title).assertExists()
        compose.onNodeWithContentDescription("Page 1 of ${OnboardingPages.size}").assertExists()
    }

    /** Next advances one page, and the indicator has to agree with the pager about which one. */
    @Test
    fun nextAdvancesThroughTheTour() {
        compose.setThemedContent { OnboardingScreen(onFinish = {}, onSkip = {}) }

        compose.onNodeWithText("Next").performClick()
        compose.waitForIdle()

        compose.onNodeWithText(OnboardingPages[1].title).assertExists()
        compose.onNodeWithContentDescription("Page 2 of ${OnboardingPages.size}").assertExists()
    }

    /**
     * The CTA becomes the exit on the last page rather than a "Next" that goes nowhere — a
     * disabled-looking button on the final slide would leave the tour with no visible end.
     */
    @Test
    fun theLastPageFinishesInsteadOfAdvancing() {
        var finished = false
        compose.setThemedContent { OnboardingScreen(onFinish = { finished = true }, onSkip = {}) }

        repeat(OnboardingPages.size - 1) {
            compose.onNodeWithText("Next").performClick()
            compose.waitForIdle()
        }

        compose.onNodeWithText("Next").assertDoesNotExist()
        compose.onNodeWithText("Get started").performClick()

        assertTrue(finished)
    }

    @Test
    fun skipLeavesTheTourImmediately() {
        var skipped = false
        compose.setThemedContent { OnboardingScreen(onFinish = {}, onSkip = { skipped = true }) }

        compose.onNodeWithText("Skip").performClick()

        assertEquals(true, skipped)
    }
}
