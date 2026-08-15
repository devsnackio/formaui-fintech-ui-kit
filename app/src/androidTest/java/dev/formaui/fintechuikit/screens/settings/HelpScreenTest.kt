package dev.formaui.fintechuikit.screens.settings

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Rule
import org.junit.Test

/**
 * The FAQ list. Each row is its own disclosure, and the state has to reach a screen reader as well
 * as the eye — the chevron rotating is not information TalkBack can see, which is why the row
 * carries an explicit Expanded/Collapsed state description.
 */
class HelpScreenTest {

    private val question = "When will my transfer arrive?"

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun groupsTheQuestionsByTopic() {
        compose.setThemedContent { HelpScreen(onBack = {}) }

        compose.onNodeWithText("Sending money").assertExists()
        compose.onNodeWithText("Your card").performScrollTo().assertExists()
        compose.onNodeWithText("Staying safe").performScrollTo().assertExists()
    }

    @Test
    fun answersAreHiddenUntilTheQuestionIsTapped() {
        compose.setThemedContent { HelpScreen(onBack = {}) }

        compose.onNodeWithText("Most transfers land within minutes", substring = true)
            .assertDoesNotExist()

        compose.onNodeWithText(question).performScrollTo().performClick()

        compose.onNodeWithText("Most transfers land within minutes", substring = true)
            .assertExists()
    }

    /**
     * The disclosure state is announced, not just drawn. A rotating chevron is nothing TalkBack can
     * report, so the row carries a state description — matched here through the semantics property
     * rather than by text, which is the only way to tell the two apart.
     */
    @Test
    fun theOpenStateIsExposedToAccessibility() {
        compose.setThemedContent { HelpScreen(onBack = {}) }

        compose.onNodeWithText(question).performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))

        compose.onNodeWithText(question).performClick()

        compose.onNodeWithText(question)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
    }
}
