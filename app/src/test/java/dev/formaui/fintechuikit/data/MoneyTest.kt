package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.formatAmount
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats two-decimal currency with grouping`() {
        assertEquals("1,240.00", Money.of(1_240, 0, Currency.USD).formatAmount())
        assertEquals("12,480.65", Money.of(12_480, 65, Currency.USD).formatAmount())
        assertEquals("0.05", Money(5, Currency.USD).formatAmount())
    }

    /** JPY has no minor unit — a fixed two-decimal formatter would render this 100x too small. */
    @Test
    fun `formats zero-decimal currency without a decimal point`() {
        assertEquals("486,300", Money.of(486_300, currency = Currency.JPY).formatAmount())
    }

    @Test
    fun `formatAmount is unsigned and formatWithSymbol carries the sign`() {
        val debit = Money.of(-86, -42, Currency.USD)
        assertEquals("86.42", debit.formatAmount())
        assertEquals("-$86.42", debit.formatWithSymbol())
        assertEquals("-$86.42", debit.formatWithSymbol(signed = true))
        assertEquals("+$4,200.00", Money.of(4_200, 0, Currency.USD).formatWithSymbol(signed = true))
        assertEquals("$4,200.00", Money.of(4_200, 0, Currency.USD).formatWithSymbol())
    }

    /**
     * The reason money is not a Double: 0.1 + 0.2 != 0.3 in binary floating point, so a running
     * total of prices drifts. Minor units make this exact.
     */
    @Test
    fun `summing amounts is exact`() {
        val total = listOf(10, 20, 30, 40, 50)
            .map { Money(it.toLong(), Currency.USD) }
            .reduce(Money::plus)
        assertEquals(150L, total.minorUnits)
        assertEquals("1.50", total.formatAmount())
    }

    @Test
    fun `rejects mixing currencies`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.of(1, 0, Currency.USD) + Money.of(1, 0, Currency.EUR)
        }
    }

    @Test
    fun `of composes major and minor units per currency scale`() {
        assertEquals(124_065L, Money.of(1_240, 65, Currency.USD).minorUnits)
        // JPY has scale 0, so the major value passes through untouched.
        assertEquals(1_240L, Money.of(1_240, 0, Currency.JPY).minorUnits)
    }
}
