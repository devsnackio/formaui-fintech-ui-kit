package dev.formaui.fintechuikit.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Notification
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The bell's inbox. Its rows are both tappable and swipeable, which is a combination worth pinning:
 * a swipe that also registers as a tap would mark a notification read on its way to being deleted.
 */
class NotificationsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listsTheNotifications() {
        compose.showNotifications()

        compose.onNodeWithText("Card payment declined").assertExists()
        compose.onNodeWithText("Payment pending").assertExists()
    }

    /** An empty inbox is a good state, not a failure, so it says so rather than showing a blank. */
    @Test
    fun emptyInboxSaysSo() {
        compose.showNotifications(notifications = emptyList())

        compose.onNodeWithText("You're all caught up").assertExists()
    }

    @Test
    fun tappingARowMarksItRead() {
        var read: Notification? = null
        compose.showNotifications(onMarkRead = { read = it })

        compose.onNodeWithText("Card payment declined").performClick()

        assertEquals("n1", read?.id)
    }

    /**
     * A settled swipe reports the dismissal exactly once, and reports the row that was actually
     * swiped. The screen offers undo through a snackbar precisely because this gesture is easy to
     * trigger by accident.
     */
    @Test
    fun swipingARowDismissesThatRow() {
        val dismissed = mutableListOf<Notification>()
        compose.showNotifications(onDismiss = { dismissed += it })

        compose.onNodeWithText("Card payment declined").performTouchInput { swipeRight() }
        compose.waitForIdle()

        assertEquals(listOf("n1"), dismissed.map { it.id })
    }
}

private fun ComposeContentTestRule.showNotifications(
    notifications: List<Notification> = SampleData.notifications,
    onDismiss: (Notification) -> Unit = {},
    onMarkRead: (Notification) -> Unit = {},
) {
    setThemedContent {
        NotificationsScreen(
            notifications = notifications,
            onDismiss = onDismiss,
            onMarkRead = onMarkRead,
            onBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
