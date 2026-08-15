package dev.formaui.fintechuikit.components

import dev.formaui.fintechuikit.components.amount.filterAmountInput
import dev.formaui.fintechuikit.components.converter.toMoneyOrZero
import dev.formaui.fintechuikit.data.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {

    @Test
    fun `strips non-numeric characters`() {
        assertEquals("1234.56", "1a2b3c4.5x6".filterAmountInput(Currency.USD))
    }

    @Test
    fun `keeps only the first decimal point`() {
        assertEquals("12.34", "12.3.4".filterAmountInput(Currency.USD))
    }

    @Test
    fun `caps fraction digits at the currency scale`() {
        assertEquals("12.34", "12.3456".filterAmountInput(Currency.USD))
    }

    /** JPY has no minor unit, so a decimal point must be dropped entirely, not just capped. */
    @Test
    fun `zero-decimal currency rejects the decimal point`() {
        assertEquals("1234", "12.34".filterAmountInput(Currency.JPY))
    }

    @Test
    fun `parses typed text to exact minor units`() {
        assertEquals(124_065L, "1240.65".toMoneyOrZero(Currency.USD).minorUnits)
        assertEquals(1_240L, "1240".toMoneyOrZero(Currency.JPY).minorUnits)
    }

    @Test
    fun `unparseable mid-typing states become zero`() {
        assertEquals(0L, "".toMoneyOrZero(Currency.USD).minorUnits)
        assertEquals(0L, ".".toMoneyOrZero(Currency.USD).minorUnits)
    }
}
