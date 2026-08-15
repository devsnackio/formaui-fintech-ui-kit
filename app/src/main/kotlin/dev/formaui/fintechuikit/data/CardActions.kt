/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.data.model.Transaction

/**
 * The rules behind the card screen's controls, as plain functions.
 *
 * Same reasoning as `MoneyMovement.kt`: these live in `data/` with no Compose types so they can be
 * unit-tested. Each one is a rule that is either right or wrong regardless of how it is drawn — what
 * counts as card spend, where a limit is allowed to sit, what a replacement card looks like — and
 * none of them would show up as wrong in a screenshot.
 */

/** The lowest monthly limit the slider offers, in minor units ($500). */
const val LimitFloorMinor: Long = 50_000

/** The highest monthly limit the slider offers, in minor units ($10,000). */
const val LimitCeilingMinor: Long = 1_000_000

/**
 * The transactions that went through the card with this [cardId].
 *
 * Reads `Transaction.cardId` rather than guessing from the category. Inferring it — "anything that
 * is not a transfer or income is card spend" — was the first attempt and it is wrong in two
 * directions at once: it claims rent paid by bank transfer for the card, and with more than one
 * card in the wallet it cannot tell them apart at all, so every card would show the same list and
 * swiping between them would change nothing.
 *
 * Pending and failed rows are kept. A declined card payment is card activity, and is probably the
 * entry someone opens this list to find.
 */
fun List<Transaction>.onCard(cardId: String): List<Transaction> = filter { txn ->
    txn.cardId == cardId
}

/**
 * The card with its monthly limit set to [minorUnits], clamped to the range the slider offers.
 *
 * Clamping here rather than at the slider means the bound holds for every caller, and the currency
 * comes from the card's existing limit so a limit can never end up denominated in something the
 * card does not spend in.
 */
fun PaymentCard.withLimit(minorUnits: Long): PaymentCard = copy(
    monthlyLimit = Money(
        minorUnits = minorUnits.coerceIn(LimitFloorMinor, LimitCeilingMinor),
        currency = monthlyLimit.currency,
    ),
)

/**
 * The replacement issued after a card is reported lost.
 *
 * A replacement arrives **active**, not frozen: freezing the old card is what protects the account,
 * and shipping the new one already frozen would leave the user with two cards they cannot spend on
 * and no obvious way back. Capabilities reset to the defaults for the kind rather than carrying over
 * — the settings belonged to a card that no longer exists.
 *
 * `spentThisMonth` carries over on purpose. The spending happened, the statement still counts it,
 * and zeroing it here would hand the user a fresh limit by losing a card.
 */
fun PaymentCard.replaced(last4: String, expiryYear: Int): PaymentCard = copy(
    last4 = last4,
    expiryYear = expiryYear,
    isFrozen = false,
    contactless = true,
    onlinePayments = true,
    atmWithdrawals = true,
)
