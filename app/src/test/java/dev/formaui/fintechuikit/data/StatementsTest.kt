/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Notification
import dev.formaui.fintechuikit.data.model.NotificationKind
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.inboxOrder
import dev.formaui.fintechuikit.data.model.unreadCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatementsTest {

    private val day = LocalDate.of(2026, 7, 15)
    private fun usd(major: Long, minor: Int = 0) = Money.of(major, minor, Currency.USD)

    private fun txn(id: String, amount: Money, on: LocalDate) = Transaction(
        id = id,
        merchant = "M-$id",
        category = SpendCategory.Shopping,
        amount = amount,
        date = on,
    )

    private val ledger = listOf(
        txn("a", usd(-100), day.minusDays(1)),  // day before
        txn("b", usd(-50), day),                // on the start boundary
        txn("c", usd(200), day.plusDays(5)),
        txn("d", usd(-25), day.plusDays(10)),   // on the end boundary
        txn("e", usd(-999), day.plusDays(11)),  // day after
    )

    // --- date range ---------------------------------------------------------------------

    /**
     * The bug this guards against: an exclusive end silently drops the last day of every statement,
     * which nobody notices until they add the rows up by hand.
     */
    @Test
    fun `date range includes both boundary days`() {
        val kept = ledger.inDateRange(day, day.plusDays(10)).map { it.id }
        assertEquals(listOf("b", "c", "d"), kept)
    }

    @Test
    fun `date range excludes the days either side`() {
        val kept = ledger.inDateRange(day, day.plusDays(10)).map { it.id }
        assertTrue("a" !in kept)
        assertTrue("e" !in kept)
    }

    /** A picker can hand back the ends the wrong way round; the range should not come back empty. */
    @Test
    fun `date range tolerates a reversed pair`() {
        assertEquals(
            ledger.inDateRange(day, day.plusDays(10)),
            ledger.inDateRange(day.plusDays(10), day),
        )
    }

    @Test
    fun `a single-day range keeps that day`() {
        assertEquals(listOf("b"), ledger.inDateRange(day, day).map { it.id })
    }

    // --- amount range -------------------------------------------------------------------

    /**
     * Compares magnitude, so the +$200 credit ("c", 20000 minor) qualifies alongside the debits —
     * a filter that only caught outgoing money would quietly hide every payment received.
     */
    @Test
    fun `amount range compares the absolute value in minor units`() {
        // abs minor units: a=10000, b=5000, c=20000, d=2500, e=99900
        val kept = ledger.inAmountRange(minMinor = 2_500, maxMinor = 20_000).map { it.id }
        assertEquals(listOf("a", "b", "c", "d"), kept)
    }

    @Test
    fun `amount range is inclusive at both ends`() {
        // "d" sits exactly on the lower bound (2500) and "a" exactly on the upper (10000).
        assertEquals(
            listOf("a", "b", "d"),
            ledger.inAmountRange(2_500, 10_000).map { it.id }.sorted(),
        )
    }

    // --- direction and totals -----------------------------------------------------------

    @Test
    fun `direction narrows to one side and All is the identity`() {
        assertEquals(listOf("c"), ledger.inDirection(StatementDirection.In).map { it.id })
        assertEquals(4, ledger.inDirection(StatementDirection.Out).size)
        assertEquals(ledger, ledger.inDirection(StatementDirection.All))
    }

    @Test
    fun `money out is reported as a positive magnitude and net keeps its sign`() {
        val totals = ledger.statementTotals(Currency.USD)
        assertEquals(20_000L, totals.moneyIn.minorUnits)
        assertTrue("money out should not be negative", totals.moneyOut.minorUnits > 0)
        assertEquals(117_400L, totals.moneyOut.minorUnits)
        assertEquals(20_000L - 117_400L, totals.net.minorUnits)
    }

    @Test
    fun `in minus out equals net`() {
        val t = ledger.statementTotals(Currency.USD)
        assertEquals(t.net.minorUnits, t.moneyIn.minorUnits - t.moneyOut.minorUnits)
    }

    @Test
    fun `totals of an empty statement are all zero`() {
        val t = emptyList<Transaction>().statementTotals(Currency.USD)
        assertEquals(0L, t.moneyIn.minorUnits)
        assertEquals(0L, t.moneyOut.minorUnits)
        assertEquals(0L, t.net.minorUnits)
    }

    // --- export -------------------------------------------------------------------------

    @Test
    fun `plain-text export carries the totals and every row`() {
        val text = ledger.asPlainTextStatement(day, day.plusDays(10), Currency.USD)
        assertTrue(text.contains("Money in"))
        assertTrue(text.contains("Net"))
        ledger.forEach { assertTrue("missing ${it.merchant}", text.contains(it.merchant)) }
    }

    @Test
    fun `plain-text export says so when there is nothing in range`() {
        val text = emptyList<Transaction>().asPlainTextStatement(day, day, Currency.USD)
        assertTrue(text.contains("No transactions in this range."))
    }

    // --- inbox ordering -----------------------------------------------------------------

    private fun note(id: String, on: LocalDate, read: Boolean) = Notification(
        id = id,
        kind = NotificationKind.Payment,
        title = "T-$id",
        body = "B-$id",
        date = on,
        read = read,
    )

    @Test
    fun `inbox puts unread first then most recent`() {
        val inbox = listOf(
            note("old-read", day.minusDays(5), read = true),
            note("new-read", day, read = true),
            note("old-unread", day.minusDays(5), read = false),
            note("new-unread", day, read = false),
        )
        assertEquals(
            listOf("new-unread", "old-unread", "new-read", "old-read"),
            inbox.inboxOrder().map { it.id },
        )
    }

    @Test
    fun `unread count ignores read entries`() {
        val inbox = listOf(
            note("a", day, read = false),
            note("b", day, read = true),
            note("c", day, read = false),
        )
        assertEquals(2, inbox.unreadCount)
    }
}
