package dev.formaui.fintechuikit.data.model

import java.time.LocalDate

/**
 * One entry in the in-app inbox.
 *
 * Plain Kotlin with no Compose or Android types, like the rest of `data/`, so the read/unread and
 * ordering rules can be exercised without rendering anything.
 *
 * The inbox deliberately mirrors things that already happened in the activity list rather than
 * inventing its own events — a notification about a payment the app has no record of would be a
 * second, contradictory source of truth.
 */
data class Notification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val date: LocalDate,
    val read: Boolean = false,
)

/**
 * What a notification is about. Drives the row's icon and tone — a declined payment should not look
 * like a receipt.
 */
enum class NotificationKind {
    /** Money moved: sent, received, or a card payment. */
    Payment,

    /** A payment request you sent or received. */
    Request,

    /** Something the user may need to act on — a decline, a new sign-in. */
    Security,
}

/** Unread first, then most recent — the order an inbox is expected to arrive in. */
fun List<Notification>.inboxOrder(): List<Notification> =
    sortedWith(compareBy<Notification> { it.read }.thenByDescending { it.date })

/** How many still need attention. Drives the badge on the Home app bar. */
val List<Notification>.unreadCount: Int get() = count { !it.read }
