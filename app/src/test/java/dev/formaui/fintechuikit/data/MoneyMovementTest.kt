/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MoneyMovementTest {

    private val on = LocalDate.of(2026, 7, 24)
    private val kenji = Recipient("r2", "Kenji Watanabe", "kenji@wise.example", Currency.JPY)
    private fun usd(major: Long, minor: Int = 0) = Money.of(major, minor, Currency.USD)

    // --- direction -------------------------------------------------------------------------

    @Test
    fun `outgoing transfer is a debit filed under Transfer`() {
        val txn = outgoingTransfer("n1", kenji, usd(50), on)
        assertTrue(txn.amount.isDebit)
        assertEquals(-5_000L, txn.amount.minorUnits)
        assertEquals(SpendCategory.Transfer, txn.category)
        assertEquals("Kenji Watanabe", txn.merchant)
        assertEquals(TransactionStatus.Completed, txn.status)
    }

    @Test
    fun `incoming top-up is a credit`() {
        val txn = incomingTopUp("n2", "Visa •••• 4821", usd(200), on)
        assertTrue(txn.amount.isCredit)
        assertEquals(20_000L, txn.amount.minorUnits)
        assertEquals(SpendCategory.Income, txn.category)
    }

    @Test
    fun `pending request is a credit awaiting payment`() {
        val txn = pendingRequest("n3", kenji, usd(75), on, note = "Dinner")
        assertTrue(txn.amount.isCredit)
        assertEquals(TransactionStatus.Pending, txn.status)
        assertEquals("Dinner", txn.note)
    }

    /** The builders own the sign, so handing them a negative amount is a caller bug. */
    @Test
    fun `builders reject a pre-negated amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            outgoingTransfer("n4", kenji, usd(-50), on)
        }
    }

    // --- balances --------------------------------------------------------------------------

    private val accounts = listOf(
        Account("acc-usd", "US Dollar", usd(1_000), isPrimary = true),
        Account("acc-eur", "Euro", Money.of(500, 0, Currency.EUR)),
    )

    @Test
    fun `applyDelta touches only the matching currency`() {
        val after = accounts.applyDelta(usd(-250))
        assertEquals(75_000L, after[0].balance.minorUnits)
        assertEquals(50_000L, after[1].balance.minorUnits) // EUR untouched
    }

    @Test
    fun `applyDelta credits on a positive delta`() {
        assertEquals(120_000L, accounts.applyDelta(usd(200))[0].balance.minorUnits)
    }

    /** A currency the user holds no account in must not crash a confirmed transfer. */
    @Test
    fun `applyDelta leaves the list unchanged when no account holds the currency`() {
        assertEquals(accounts, accounts.applyDelta(Money.of(10, 0, Currency.SGD)))
    }

    @Test
    fun `canCover compares against the balance in the same currency`() {
        val usdAccount = accounts.first()
        assertTrue(usdAccount.canCover(usd(1_000)))
        assertFalse(usdAccount.canCover(usd(1_000, 1)))
        assertFalse(usdAccount.canCover(Money.of(1, 0, Currency.EUR)))
    }

    // --- rate lookup -----------------------------------------------------------------------

    @Test
    fun `rateFor resolves the stored pair`() {
        val rate = SampleData.rateFor(Currency.USD, Currency.JPY)
        assertNotNull(rate)
        assertEquals(Currency.JPY, rate!!.to)
    }

    /**
     * The bug this table was added to fix: a JPY payee used to convert at the USD→EUR rate.
     *
     * $50 is 5000¢; the 0.45% fee is 22.5¢ which rounds half-up to 23¢, leaving 4977¢. Times 157.20
     * is 782,384.4, and the two-place scale shift for a zero-decimal target brings that to ¥7,824.
     * The scale shift is the part worth pinning: without it the result would be 100x too large.
     */
    @Test
    fun `USD to JPY converts into whole yen`() {
        val converted = SampleData.rateFor(Currency.USD, Currency.JPY)!!.convert(usd(50))
        assertEquals(Currency.JPY, converted.currency)
        assertEquals(7_824L, converted.minorUnits)
    }

    @Test
    fun `rateFor inverts a stored pair asked for backwards`() {
        val rate = SampleData.rateFor(Currency.EUR, Currency.USD)
        assertNotNull(rate)
        assertEquals(Currency.EUR, rate!!.from)
        assertEquals(Currency.USD, rate.to)
        // 1 / 0.9210 ≈ 1.0857
        assertTrue(rate.rate.toDouble() in 1.08..1.09)
    }

    @Test
    fun `rateFor crosses two non-pivot currencies through USD`() {
        val rate = SampleData.rateFor(Currency.EUR, Currency.JPY)
        assertNotNull(rate)
        // 157.20 / 0.9210 ≈ 170.68
        assertTrue(rate!!.rate.toDouble() in 170.0..171.5)
    }

    @Test
    fun `rateFor returns an identity rate for the same currency`() {
        val rate = SampleData.rateFor(Currency.USD, Currency.USD)!!
        assertEquals(usd(100), rate.convert(usd(100)))
    }

    @Test
    fun `rateFor returns null for a pair the demo cannot reach`() {
        assertNull(SampleData.rateFor(Currency.AUD, Currency.SGD))
    }
}
