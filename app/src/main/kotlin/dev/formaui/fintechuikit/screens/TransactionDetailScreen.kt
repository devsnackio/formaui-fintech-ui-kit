/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.divider.FormaDivider
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.common.icon
import dev.formaui.fintechuikit.components.common.rememberCopyToClipboard
import dev.formaui.fintechuikit.components.status.StatusPill
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.dateGroupLabel
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.TransactionStatus
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * One transaction in full — the destination behind every row's `onClick`, which was wired to `{}`
 * on both the Dashboard and the Activity tab.
 *
 * The amount colour follows the same rule the row list already uses and it is a brand rule, not a
 * stylistic one: a credit is `brandColors.positive`, **never** the CTA green. DESIGN.md reserves
 * Wise green for conversion, so tinting incoming money with it would make every payment received
 * compete with the primary action on the screen.
 *
 * Three things about the layout are deliberate:
 *
 *  - **Every fact appears exactly once.** The status used to be both a header pill and a "Status"
 *    row; it is now only the pill on the timeline card. The date used to be a "Date" row; it is now
 *    the first step of that timeline, which is the same information doing more work.
 *  - **The timeline is the screen's answer to "what now?"** A status word tells a user their payment
 *    is Pending; it does not tell them what is being waited on or that a Failed payment left their
 *    balance alone. The steps say both, and they give the three states a different *shape* rather
 *    than only a different colour — which is what made a failed payment read like a quiet one.
 *  - **[reference] is derived, not the raw id.** `transaction.id` is "t6", which under a
 *    "Reference" label reads as a bug rather than as a receipt number.
 *
 * [onRepeat] is offered only for a transfer, because repeating a card purchase is not something the
 * app can do. Any action that could not do something real was left off rather than shipped inert —
 * that is the whole point of this screen existing. The one control added here, copying the
 * reference, is wired to the clipboard exactly as `ReceiveScreen` and `CardDetailsScreen` are.
 */
@Composable
fun TransactionDetailScreen(
    transaction: Transaction,
    today: LocalDate,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRepeat: (() -> Unit)? = null,
) {
    val copy = rememberCopyToClipboard("Transaction reference")
    val reference = transaction.reference

    FlowScreen(
        title = "Transaction",
        onBack = onBack,
        modifier = modifier,
        footer = onRepeat?.let {
            {
                FormaButton(
                    onClick = it,
                    variant = FormaButtonVariant.Outlined,
                    shape = BrandDefaults.buttonShape,
                    contentPadding = BrandDefaults.buttonContentPadding,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Send again")
                }
            }
        },
    ) {
        // Deliberately unstaggered. This screen is the one route `FinTechNavHost` slides *vertically*
        // — it rises a sixth of the height as a single sheet — and a sheet whose contents separately
        // fade up while it is still travelling is two motions describing one arrival. Worse, the
        // stagger held each card undrawn for its delay and then ran 260ms of its own, so the cards
        // were still landing 80ms after the host transition had finished: the screen settled, and
        // the content was visibly late. Top-down resolve is worth having on a screen that arrives
        // without motion of its own; here the host's motion already is the entrance.
        TransactionHeader(transaction)

        StatusTimelineCard(
            transaction = transaction,
            today = today,
        )

        DetailsCard(
            transaction = transaction,
            reference = reference,
            // No confirmation snackbar, matching the other two copy actions in the app: Android 13+
            // shows its own clipboard toast, and a second one on top of it reads as a stutter.
            onCopyReference = { copy(reference) },
        )
    }
}

/**
 * A reference a user could actually quote back to support, built from what the model holds.
 *
 * The date plus a zero-padded id is a pure function of the transaction, so the same row always shows
 * the same reference — no clock, no random, nothing to persist. The `id.uppercase()` this replaces
 * rendered as "T6", which looks like an unfinished feature rather than a receipt number. Ids with no
 * digits in them fall back to the id itself rather than to "0000".
 */
private val Transaction.reference: String
    get() {
        val tail = id.filter(Char::isDigit).takeIf { it.isNotEmpty() }?.padStart(4, '0')
        return "TX-${date.format(ReferenceDateFormat)}-${tail ?: id.uppercase(Locale.US)}"
    }

private val ReferenceDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US)

@Composable
private fun TransactionHeader(transaction: Transaction, modifier: Modifier = Modifier) {
    val brand = FinTechTheme.brandColors
    val failed = transaction.status == TransactionStatus.Failed
    val amountColor = when {
        failed -> brand.mute
        transaction.amount.isCredit -> brand.positive
        else -> FormaTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
    ) {
        TransactionAvatar(transaction)

        Text(
            text = transaction.amount.formatWithSymbol(signed = transaction.amount.isCredit),
            style = FormaTheme.typography.numeric.merge(
                FormaTheme.typography.material.headlineMedium,
            ),
            color = amountColor,
            // Struck through only when the money never moved. Mute grey alone left a failed payment
            // looking like a quiet one; a rule drawn through the figure cancels it outright, and the
            // timeline below spells the same thing out for TalkBack, which does not read decoration.
            textDecoration = if (failed) TextDecoration.LineThrough else null,
        )

        Text(
            text = transaction.merchant,
            style = FormaTheme.typography.material.titleMedium,
        )
    }
}

/**
 * The header's mark: initials for a person, the category icon for everything else, tinted by status.
 *
 * The tone mapping is [StatusPill]'s, so the mark at the top of the screen and the pill under it
 * agree at a glance — including Failed, which reads the same `errorContainer`/`onErrorContainer`
 * pair the pill now does.
 */
@Composable
private fun TransactionAvatar(transaction: Transaction) {
    val brand = FinTechTheme.brandColors
    val scheme = FormaTheme.colorScheme
    val container: Color
    val content: Color
    when (transaction.status) {
        TransactionStatus.Completed -> {
            container = scheme.primaryContainer
            content = scheme.onPrimaryContainer
        }
        TransactionStatus.Pending -> {
            container = brand.warning
            content = brand.warningContent
        }
        TransactionStatus.Failed -> {
            container = scheme.errorContainer
            content = scheme.onErrorContainer
        }
    }

    // A transfer's `merchant` is a person, and two letters identify a person better than the generic
    // swap glyph does. Everywhere else the counterparty is a shop or a service, where the category
    // icon — the same one the row in the list carries — is the more specific mark.
    if (transaction.category == SpendCategory.Transfer) {
        FormaAvatar(
            initials = transaction.initials,
            size = FormaAvatarSize.Large,
            containerColor = container,
            contentColor = content,
        )
    } else {
        FormaAvatar(
            size = FormaAvatarSize.Large,
            containerColor = container,
            contentColor = content,
        ) {
            // The category is spelled out in the details card below, so repeating it here would
            // make TalkBack read it twice.
            Icon(imageVector = transaction.category.icon, contentDescription = null)
        }
    }
}

@Composable
private fun StatusTimelineCard(
    transaction: Transaction,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val steps = timelineSteps(transaction, dateGroupLabel(transaction.date, today))

    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
        // The pill goes in the header slot rather than the content slot: the card's own stacking
        // then supplies the gap above the rail, and the content slot is left holding one child.
        header = { StatusPill(transaction.status) },
    ) {
        // One child: FormaCard's 16dp content arrangement is not overridable, and the rail has to
        // own the spacing between its steps or the connector cannot reach the next dot.
        Column(Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, step ->
                TimelineStepRow(step = step, isLast = index == steps.lastIndex)
            }
        }
    }
}

/** Where a step sits in the payment's life — which decides how its dot is drawn. */
private enum class StepMark { Done, Current, Blocked, Upcoming }

private data class TimelineStep(val label: String, val detail: String, val mark: StepMark)

/**
 * The steps for a transaction, newest last.
 *
 * The copy is direction-aware because the same status means different things either way: a Pending
 * debit is waiting on the other side, while a Pending credit is money someone else has not sent yet.
 * `pendingRequest` is the app's only source of pending credits, which is why an unfinished incoming
 * amount is labelled "Requested" — it is one.
 *
 * The Failed path ends on a **reassurance**, not on the failure: the single most useful fact about a
 * declined payment is that the balance is untouched, and it was previously buried in the tail of a
 * status string.
 */
private fun timelineSteps(transaction: Transaction, dateLabel: String): List<TimelineStep> {
    val credit = transaction.amount.isCredit
    val opened = when {
        !credit -> "Sent"
        transaction.status == TransactionStatus.Completed -> "Received"
        else -> "Requested"
    }
    val first = TimelineStep(opened, dateLabel, StepMark.Done)

    return when (transaction.status) {
        TransactionStatus.Completed -> listOf(
            first,
            TimelineStep(
                label = "Completed",
                detail = if (credit) {
                    "The money is in your account."
                } else {
                    "${transaction.merchant} was paid."
                },
                mark = StepMark.Done,
            ),
        )

        TransactionStatus.Pending -> listOf(
            first,
            TimelineStep(
                label = if (credit) "Waiting on ${transaction.merchant}" else "Processing",
                detail = if (credit) "They have not paid yet." else "Waiting on the other side.",
                mark = StepMark.Current,
            ),
            TimelineStep(
                label = if (credit) "Arrives" else "Completes",
                detail = "Most payments clear within one working day.",
                mark = StepMark.Upcoming,
            ),
        )

        TransactionStatus.Failed -> listOf(
            first,
            TimelineStep(
                label = "Failed",
                // A failed transaction's note *is* the reason it failed — "Card declined" — so it
                // belongs on the step it explains. DetailsCard drops its "Note" row to match.
                detail = transaction.note ?: "The payment did not go through.",
                mark = StepMark.Blocked,
            ),
            TimelineStep(
                label = if (credit) "Nothing arrived" else "Nothing was charged",
                detail = if (credit) {
                    "Your balance is unchanged."
                } else {
                    "The money is still in your account."
                },
                mark = StepMark.Done,
            ),
        )
    }
}

@Composable
private fun TimelineStepRow(step: TimelineStep, isLast: Boolean) {
    val brand = FinTechTheme.brandColors
    val scheme = FormaTheme.colorScheme
    val markColor = when (step.mark) {
        StepMark.Done -> brand.positive
        StepMark.Current -> brand.warningDeep
        StepMark.Blocked -> scheme.error
        StepMark.Upcoming -> scheme.outlineVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // The row is measured at its minimum intrinsic height so the connector below can take
            // whatever height the step's text claims via weight(); without it the Column is
            // unbounded and weight has nothing to divide.
            .height(IntrinsicSize.Min),
    ) {
        Column(
            modifier = Modifier.width(TimelineDotSize),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.height(TimelineLabelLineHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (step.mark == StepMark.Upcoming) {
                    // Drawn as an outline: it has not happened, so it should not read as
                    // filled-in progress.
                    Box(
                        Modifier
                            .size(TimelineDotSize)
                            .border(TimelineStroke, markColor, CircleShape),
                    )
                } else {
                    Box(Modifier.size(TimelineDotSize).background(markColor, CircleShape))
                }
            }

            if (!isLast) {
                Box(
                    Modifier
                        .width(TimelineStroke)
                        .weight(1f)
                        .background(scheme.outlineVariant),
                )
            }
        }

        Spacer(Modifier.width(FinTechTheme.spacing.md))

        Column(
            modifier = Modifier
                .weight(1f)
                // The gap between steps lives inside the text column rather than in an arrangement,
                // so the rail — which fills the row — spans it and the dots stay connected.
                .padding(bottom = if (isLast) 0.dp else FinTechTheme.spacing.lg),
        ) {
            Text(step.label, style = FormaTheme.typography.material.titleSmall)
            Text(
                text = step.detail,
                style = FormaTheme.typography.material.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailsCard(
    transaction: Transaction,
    reference: String,
    onCopyReference: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A failed transaction's note is its failure reason, and the timeline above already shows it
    // against the step it belongs to.
    val note = transaction.note.takeIf { transaction.status != TransactionStatus.Failed }

    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        // One child: FormaCard's 16dp content arrangement is not overridable.
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {
            DetailRow("Category", transaction.category.displayName)
            if (note != null) {
                FormaDivider()
                DetailRow("Note", note)
            }
            FormaDivider()
            ReferenceRow(reference = reference, onCopy = onCopyReference)
        }
    }
}

/**
 * The one detail row with an action on it.
 *
 * The value takes the remaining width and ends flush right so a long reference wraps inside its own
 * slot instead of shoving the copy button off the card; it is set a step down from the other values
 * for the same reason — a reference is a code to be read once, not a fact to be scanned.
 */
@Composable
private fun ReferenceRow(reference: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Reference",
            style = FormaTheme.typography.material.bodyMedium,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        // The value slot takes the rest of the row rather than the row arranging SpaceBetween:
        // with a weighted child there is no free space left for an arrangement to distribute.
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = reference,
                style = FormaTheme.typography.material.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f, fill = false),
            )
            FormaIconButton(onClick = onCopy) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy reference")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = FormaTheme.typography.material.bodyMedium,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = FormaTheme.typography.material.bodyMedium)
    }
}

/**
 * The timeline rail's geometry.
 *
 * These are marks rather than layout, so they take no spacing token. [TimelineLabelLineHeight]
 * matches `titleSmall`'s 20sp line box so the dot centres on the step's first line of text.
 */
private val TimelineDotSize = 10.dp
private val TimelineStroke = 2.dp
private val TimelineLabelLineHeight = 20.dp

/**
 * Renders one sample transaction, mirroring the call site's rule that only a transfer is repeatable.
 *
 * Shared by the previews below so each is four lines and the set can cover every status in both
 * schemes without the file turning into previews.
 */
@Composable
private fun TransactionDetailPreview(id: String, dark: Boolean = false) {
    FinTechTheme(darkTheme = dark) {
        val transaction = SampleData.transactions.first { it.id == id }
        val repeatAction: () -> Unit = {}
        TransactionDetailScreen(
            transaction = transaction,
            today = SampleData.today,
            onBack = {},
            onRepeat = repeatAction.takeIf { transaction.category == SpendCategory.Transfer },
        )
    }
}

@Preview(name = "Transaction detail · completed transfer", heightDp = 820)
@Composable
private fun TransactionDetailCompletedPreview() = TransactionDetailPreview("t6")

/** A credit: `brandColors.positive` on the amount, never the CTA green. */
@Preview(name = "Transaction detail · credit (dark)", heightDp = 820)
@Composable
private fun TransactionDetailCreditDarkPreview() = TransactionDetailPreview("t3", dark = true)

/** Pending: the rail stops on the current step and names what is still to come. */
@Preview(name = "Transaction detail · pending", heightDp = 820)
@Composable
private fun TransactionDetailPendingPreview() = TransactionDetailPreview("t4")

@Preview(name = "Transaction detail · pending (dark)", heightDp = 820)
@Composable
private fun TransactionDetailPendingDarkPreview() = TransactionDetailPreview("t4", dark = true)

/** Failed: struck-through mute amount, and the rail ends on "nothing was charged". */
@Preview(name = "Transaction detail · failed", heightDp = 820)
@Composable
private fun TransactionDetailFailedPreview() = TransactionDetailPreview("t8")

@Preview(name = "Transaction detail · failed (dark)", heightDp = 820)
@Composable
private fun TransactionDetailFailedDarkPreview() = TransactionDetailPreview("t8", dark = true)
