/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.TransactionStatus
import java.time.LocalDate

/**
 * The rules for what money movement does to the activity list and to a balance.
 *
 * These live in `data/` as plain Kotlin — no Compose types — so they stay unit-testable, which
 * matters more here than anywhere else in the app: they are the only place that decides the **sign**
 * of an amount, and the entire UI reads that sign to tell a credit from a debit. `TransactionRow`
 * colours by `Money.isCredit`, the donut filters on `isDebit`, and the detail screen picks its
 * amount colour the same way. Get a sign wrong here and it is wrong in four places at once.
 *
 * Every builder takes an explicit `id` rather than generating one. A caller-supplied id keeps these
 * functions pure — deterministic under test, and no hidden clock or RNG.
 *
 * All of them take the amount as a **positive** value and apply the direction themselves, so a
 * caller cannot accidentally double-negate a debit.
 */

/**
 * An outgoing transfer to a person: a debit, filed under [SpendCategory.Transfer].
 *
 * The counterparty goes in `merchant`. `Transaction` has no separate payee field — direction and
 * counterparty are carried by the amount's sign and by `merchant` — so a transfer and a card
 * purchase are the same shape, which is why `TransactionRow` can render both without branching.
 */
fun outgoingTransfer(
    id: String,
    to: Recipient,
    amount: Money,
    on: LocalDate,
    note: String? = null,
): Transaction {
    requirePositive(amount)
    return Transaction(
        id = id,
        merchant = to.name,
        category = SpendCategory.Transfer,
        amount = -amount,
        date = on,
        status = TransactionStatus.Completed,
        note = note,
    )
}

/** A top-up from an external source: a credit, filed under [SpendCategory.Income]. */
fun incomingTopUp(
    id: String,
    source: String,
    amount: Money,
    on: LocalDate,
    note: String? = null,
): Transaction {
    requirePositive(amount)
    return Transaction(
        id = id,
        merchant = source,
        category = SpendCategory.Income,
        amount = amount,
        date = on,
        status = TransactionStatus.Completed,
        note = note,
    )
}

/**
 * A payment request you have sent to someone — money you expect to receive.
 *
 * Modelled as a **Pending [Transaction]** rather than as its own type. A request is not yet a
 * transaction in accounting terms, but reusing this shape means it renders in the activity list,
 * groups by date, and gets a `StatusPill` for free, and it gives `TransactionStatus.Pending` its
 * first source that the user can actually create. The trade-off to know: because it is a positive
 * amount in the same list, a pending request counts toward nothing yet — it must **not** be applied
 * to a balance until it is paid, which is why no caller pairs this with [applyDelta].
 */
fun pendingRequest(
    id: String,
    from: Recipient,
    amount: Money,
    on: LocalDate,
    note: String? = null,
): Transaction {
    requirePositive(amount)
    return Transaction(
        id = id,
        merchant = from.name,
        category = SpendCategory.Transfer,
        amount = amount,
        date = on,
        status = TransactionStatus.Pending,
        note = note,
    )
}

/**
 * Applies a signed [delta] to whichever account holds that currency, leaving the rest untouched.
 *
 * Pass a negative delta to debit. Accounts in other currencies are returned as-is rather than
 * converted — moving money between the user's own currencies is a different operation than paying
 * someone, and it is not modelled here.
 *
 * If no account holds [delta]'s currency the list comes back unchanged. That is deliberate for a
 * demo: a missing account should not crash a confirmed transfer. Callers that need to know should
 * check for the account first.
 */
fun List<Account>.applyDelta(delta: Money): List<Account> = map { account ->
    if (account.balance.currency == delta.currency) {
        account.copy(balance = account.balance + delta)
    } else {
        account
    }
}

/** True when [account] can cover [amount] — the check the send flow's error state reads. */
fun Account.canCover(amount: Money): Boolean =
    balance.currency == amount.currency && balance.minorUnits >= amount.absolute.minorUnits

private fun requirePositive(amount: Money) = require(amount.minorUnits >= 0) {
    "Pass the amount as a positive value; direction is applied here (got ${amount.minorUnits})"
}
