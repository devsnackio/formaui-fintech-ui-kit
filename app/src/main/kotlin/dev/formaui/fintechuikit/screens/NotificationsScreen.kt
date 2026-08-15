@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalMaterial3Api::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.badge.FormaBadge
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.divider.FormaDivider
import dev.formaui.components.emptystate.FormaEmptyState
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.components.snackbar.FormaSnackbarHost
import dev.formaui.components.swipedismiss.FormaSwipeToDismiss
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.dateGroupLabel
import dev.formaui.fintechuikit.data.model.Notification
import dev.formaui.fintechuikit.data.model.NotificationKind
import dev.formaui.fintechuikit.data.model.inboxOrder
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import java.time.LocalDate

/**
 * The in-app inbox — everything the app has already done that the user might want to know about.
 *
 * It exists because the app produced notifications with nowhere to put them: creating a payment
 * request appends a **Pending** transaction and never mentions it again, and a declined payment was
 * discoverable only by scrolling the activity list. The entries mirror transactions that already
 * exist rather than inventing events, so the inbox and the ledger cannot tell different stories.
 *
 * **Dismiss is undoable, and that is not optional.** A swipe is easy to trigger by accident and this
 * one destroys the only copy of the item. `FormaSwipeToDismiss` without an undo would be a
 * destructive gesture with no way back — so the removal is reported up, held by the host, and offered
 * back through the snackbar.
 *
 * There is **no pull-to-refresh**. The list is local sample data with nothing to fetch, so a pull
 * would spin an indicator and change nothing — motion theatre, and the same class of lie as a button
 * that does nothing.
 */
@Composable
fun NotificationsScreen(
    notifications: List<Notification>,
    onDismiss: (Notification) -> Unit,
    onMarkRead: (Notification) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    today: LocalDate = SampleData.today,
) {
    FlowScreen(
        title = "Notifications",
        onBack = onBack,
        modifier = modifier,
        snackbarHost = { FormaSnackbarHost(snackbarHostState) },
    ) {
        if (notifications.isEmpty()) {
            FormaEmptyState(
                title = "You're all caught up",
                description = "Payments, requests and security alerts will show up here.",
                icon = { Icon(Icons.Filled.NotificationsNone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            FormaCard(
                variant = FormaCardVariant.Filled,
                shape = BrandDefaults.cardShape,
                // Zero: the rows are full-width tap targets, and a swipe needs the whole width to
                // travel across — an inset would leave dead gutters the gesture ignores.
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // One child: FormaCard's 16dp content arrangement cannot be overridden, and a gap
                // between swipeable rows would show the card through the gesture.
                Column(modifier = Modifier.fillMaxWidth()) {
                    val ordered = notifications.inboxOrder()
                    ordered.forEachIndexed { index, notification ->
                        if (index > 0) FormaDivider()
                        NotificationRow(
                            notification = notification,
                            today = today,
                            onDismiss = { onDismiss(notification) },
                            onClick = { onMarkRead(notification) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: Notification,
    today: LocalDate,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    // key() on the id so a dismissed row does not hand its half-swiped state to the row that takes
    // its place in the list.
    val state = key(notification.id) { rememberSwipeToDismissBoxState() }

    // The dismissal is reported through the box's own `onDismiss`, which fires once, when the row
    // has settled past the threshold. The `confirmValueChange` callback this replaces was not
    // one-shot: the anchored-draggable asked it to confirm every candidate target while the gesture
    // settled, so a single swipe ran it three times and reported three dismissals of the same
    // notification. Removing an id three times is harmless, but each report also pushes an undo
    // snackbar — so one swipe left two stale "Undo" offers behind it, and the last one to be
    // actioned would restore a notification the user had already finished dismissing.
    FormaSwipeToDismiss(
        state = state,
        backgroundContent = { DismissBackground(state.dismissDirection) },
        onDismiss = { onDismiss() },
    ) {
        FormaListItem(
            headline = notification.title,
            supporting = "${notification.body}  ·  ${dateGroupLabel(notification.date, today)}",
            onClick = onClick,
            leading = {
                FormaAvatar(
                    size = FormaAvatarSize.Medium,
                    containerColor = notification.kind.container(),
                    contentColor = notification.kind.content(),
                ) {
                    Icon(notification.kind.icon(), contentDescription = null)
                }
            },
            trailing = {
                // A dot, not a count: one row is one notification, so a number would always read 1.
                if (!notification.read) FormaBadge()
            },
            headlineTextStyle = FormaTheme.typography.material.bodyMedium,
            supportingTextStyle = FormaTheme.typography.material.bodySmall,
        )
    }
}

/** The surface revealed under a swiping row. Mirrored so the icon leads the gesture either way. */
@Composable
private fun RowScope.DismissBackground(direction: SwipeToDismissBoxValue) {
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        else -> Alignment.CenterEnd
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(FormaTheme.colorScheme.errorContainer)
            .padding(horizontal = FinTechTheme.spacing.lg),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null, // the gesture itself is the affordance
            tint = FormaTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun NotificationKind.icon() = when (this) {
    NotificationKind.Payment -> Icons.Filled.CreditCard
    NotificationKind.Request -> Icons.Filled.RequestQuote
    NotificationKind.Security -> Icons.Filled.Shield
}

@Composable
private fun NotificationKind.container() = when (this) {
    // Security is the one kind that may need acting on, so it is the one that looks different.
    NotificationKind.Security -> FormaTheme.colorScheme.errorContainer
    else -> FormaTheme.colorScheme.surfaceVariant
}

@Composable
private fun NotificationKind.content() = when (this) {
    NotificationKind.Security -> FormaTheme.colorScheme.onErrorContainer
    else -> FormaTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun NotificationsPreview(
    items: List<Notification> = SampleData.notifications,
    dark: Boolean = false,
) {
    FinTechTheme(darkTheme = dark) {
        NotificationsScreen(
            notifications = items,
            onDismiss = {},
            onMarkRead = {},
            onBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(name = "Notifications · light", heightDp = 700)
@Composable
private fun NotificationsLightPreview() = NotificationsPreview()

@Preview(name = "Notifications · dark", heightDp = 700)
@Composable
private fun NotificationsDarkPreview() = NotificationsPreview(dark = true)

/** The cleared inbox — the state the empty copy exists for. */
@Preview(name = "Notifications · all caught up", heightDp = 500)
@Composable
private fun NotificationsEmptyPreview() = NotificationsPreview(items = emptyList())
