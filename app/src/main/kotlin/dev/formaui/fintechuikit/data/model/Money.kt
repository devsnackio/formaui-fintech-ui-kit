/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data.model

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * A monetary amount, held as an exact integer count of the currency's smallest unit.
 *
 * Money is never a `Double` here. Binary floating point cannot represent 0.10 exactly, so
 * summing a list of prices drifts — the classic `0.1 + 0.2 != 0.3`. Minor units (cents, pence,
 * whole yen) keep arithmetic exact, and [decimal] converts losslessly for display only.
 *
 * A negative [minorUnits] is a debit; positive is a credit.
 */
data class Money(
    val minorUnits: Long,
    val currency: Currency,
) {
    init {
        // Adding USD to JPY is meaningless, so the operators below reject it. Catching a bad
        // construction early beats discovering it in a total.
        require(minorUnits != Long.MIN_VALUE) { "minorUnits overflows on negation" }
    }

    /** Exact decimal value — `BigDecimal(unscaled, scale)` needs no division and cannot drift. */
    val decimal: BigDecimal get() = BigDecimal.valueOf(minorUnits, currency.decimalPlaces)

    val isCredit: Boolean get() = minorUnits > 0
    val isDebit: Boolean get() = minorUnits < 0

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    operator fun unaryMinus(): Money = copy(minorUnits = -minorUnits)

    val absolute: Money get() = if (minorUnits < 0) -this else this

    private fun requireSameCurrency(other: Money) = require(currency == other.currency) {
        "Cannot combine ${currency.code} with ${other.currency.code}"
    }

    companion object {
        fun of(major: Long, minor: Int = 0, currency: Currency): Money {
            val factor = generateSequence(1L) { it * 10 }.elementAt(currency.decimalPlaces)
            return Money(major * factor + minor, currency)
        }

        fun zero(currency: Currency) = Money(0, currency)
    }
}

/**
 * Formats the amount without a currency symbol — "1,240.00", "124,000" for JPY.
 *
 * Grouping and decimal separators are pinned to [Locale.US] so the kit renders identically
 * whatever the device locale is; a real app would use the user's locale here.
 */
fun Money.formatAmount(): String = amountFormatter(currency.decimalPlaces).format(decimal.abs())

/**
 * Formats with the currency symbol, e.g. "$1,240.00".
 *
 * @param signed when true, prefixes an explicit `+` or `−` — used by transaction rows where the
 *   direction of the money matters as much as the value.
 */
fun Money.formatWithSymbol(signed: Boolean = false): String {
    val sign = when {
        !signed -> if (isDebit) "-" else ""
        isDebit -> "-"
        else -> "+"
    }
    return "$sign${currency.symbol}${formatAmount()}"
}

private fun amountFormatter(decimalPlaces: Int) = DecimalFormat().apply {
    minimumFractionDigits = decimalPlaces
    maximumFractionDigits = decimalPlaces
    isGroupingUsed = true
    decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
}
