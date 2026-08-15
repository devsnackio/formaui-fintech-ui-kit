/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A conversion rate plus the transfer fee, mirroring how the brand presents a transfer: the
 * fee is shown separately and taken off the source amount before conversion, never hidden in a
 * marked-up rate.
 */
data class ExchangeRate(
    val from: Currency,
    val to: Currency,
    val rate: BigDecimal,
    /** Fee as a fraction of the source amount, e.g. 0.0045 for 0.45%. */
    val feeRate: BigDecimal,
) {
    fun feeOn(amount: Money): Money {
        require(amount.currency == from) { "Expected ${from.code}, got ${amount.currency.code}" }
        val fee = BigDecimal(amount.minorUnits).multiply(feeRate)
        return Money(fee.setScale(0, RoundingMode.HALF_UP).toLong(), from)
    }

    /**
     * Converts after deducting the fee.
     *
     * Scaling by the two currencies' decimal places keeps this correct across a pair like
     * USD→JPY, where the target has no minor units at all.
     */
    fun convert(amount: Money): Money {
        require(amount.currency == from) { "Expected ${from.code}, got ${amount.currency.code}" }
        val net = BigDecimal(amount.minorUnits - feeOn(amount).minorUnits)
        val scaleShift = to.decimalPlaces - from.decimalPlaces
        val converted = net.multiply(rate).movePointRight(scaleShift)
        return Money(converted.setScale(0, RoundingMode.HALF_UP).toLong(), to)
    }

    /**
     * The same pair the other way round — what the converter's swap button needs.
     *
     * The fee fraction carries over unchanged: it is a percentage of the source amount, so it does
     * not invert. Ten decimal places is enough to survive a round trip through a rate like 157.20
     * without visibly drifting.
     */
    fun inverted(): ExchangeRate = ExchangeRate(
        from = to,
        to = from,
        rate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP),
        feeRate = feeRate,
    )

    /** "1 USD = 0.9210 EUR" — the rate line under the converter. */
    val displayLine: String
        get() = "1 ${from.code} = ${rate.stripTrailingZeros().toPlainString()} ${to.code}"
}
