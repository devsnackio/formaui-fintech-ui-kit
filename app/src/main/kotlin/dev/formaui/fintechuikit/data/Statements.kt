/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import java.time.LocalDate

/** Which side of the ledger a statement is showing. */
enum class StatementDirection(val label: String) {
    All("All"),
    In("Money in"),
    Out("Money out"),
}

/** What a filtered statement adds up to. */
data class StatementTotals(
    val moneyIn: Money,
    val moneyOut: Money,
    val net: Money,
)

/**
 * The statement rules, kept here as plain Kotlin so they can be tested without a screen.
 *
 * These are the only place in the app that filters by date or by amount, and both have an edge that
 * is easy to get silently wrong — an exclusive boundary drops a day, and comparing money as a
 * floating-point number drifts. Neither failure is visible in a screenshot, which is exactly why the
 * logic does not live in the composable.
 */

/**
 * Transactions inside [start]..[end], **inclusive at both ends**.
 *
 * Inclusive matters: a user who picks 1–31 July means the whole of both days. An exclusive end
 * silently omits the last day of every statement, which is the kind of bug that only surfaces when
 * someone adds up the rows by hand.
 */
fun List<Transaction>.inDateRange(start: LocalDate, end: LocalDate): List<Transaction> {
    val from = minOf(start, end)
    val to = maxOf(start, end)
    return filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
}

/**
 * Transactions whose magnitude falls within [minMinor]..[maxMinor] minor units, inclusive.
 *
 * Compares on the **absolute** value, so a $50 debit and a $50 credit both sit at 50. The bounds
 * arrive from a slider as `Float`; they are converted to `Long` here and every comparison happens in
 * integer minor units. Money never touches the float — that is the whole reason [Money] is an
 * integer count in the first place.
 */
fun List<Transaction>.inAmountRange(minMinor: Long, maxMinor: Long): List<Transaction> =
    filter { it.amount.absolute.minorUnits in minMinor..maxMinor }

/** Narrows to one side of the ledger. [StatementDirection.All] is the identity. */
fun List<Transaction>.inDirection(direction: StatementDirection): List<Transaction> = when (direction) {
    StatementDirection.All -> this
    StatementDirection.In -> filter { it.amount.isCredit }
    StatementDirection.Out -> filter { it.amount.isDebit }
}

/**
 * Money in, money out, and the net, over whatever list is handed in.
 *
 * `moneyOut` is reported as a **positive** magnitude because the label already says "out" — a minus
 * sign next to the word would read as a double negative. `net` keeps its sign, since that is the one
 * figure whose direction is the answer.
 */
fun List<Transaction>.statementTotals(currency: Currency): StatementTotals {
    val inMinor = filter { it.amount.isCredit }.sumOf { it.amount.minorUnits }
    val outMinor = filter { it.amount.isDebit }.sumOf { it.amount.absolute.minorUnits }
    return StatementTotals(
        moneyIn = Money(inMinor, currency),
        moneyOut = Money(outMinor, currency),
        net = Money(inMinor - outMinor, currency),
    )
}

/**
 * The statement as plain text, for the clipboard.
 *
 * This is what "export" produces. Not a PDF and not a share sheet — a clipboard copy is something the
 * demo can genuinely do, and a button that produced no file would be the kind of empty promise the
 * rest of this app has had removed.
 */
fun List<Transaction>.asPlainTextStatement(
    start: LocalDate,
    end: LocalDate,
    currency: Currency,
): String {
    val totals = statementTotals(currency)
    val rows = sortedByDescending { it.date }.joinToString("\n") { txn ->
        "${txn.date}  ${txn.merchant.padEnd(24).take(24)}  ${txn.amount.formatWithSymbol(signed = true)}"
    }
    return buildString {
        appendLine("Statement  $start to $end")
        appendLine("-".repeat(56))
        if (rows.isBlank()) appendLine("No transactions in this range.") else appendLine(rows)
        appendLine("-".repeat(56))
        appendLine("Money in   ${totals.moneyIn.formatWithSymbol()}")
        appendLine("Money out  ${totals.moneyOut.formatWithSymbol()}")
        appendLine("Net        ${totals.net.formatWithSymbol(signed = true)}")
    }
}
