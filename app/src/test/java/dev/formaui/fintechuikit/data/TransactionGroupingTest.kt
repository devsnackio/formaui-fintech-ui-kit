/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TransactionGroupingTest {

    private val today = LocalDate.of(2026, 7, 24)

    @Test
    fun `labels today and yesterday by name`() {
        assertEquals("Today", dateGroupLabel(today, today))
        assertEquals("Yesterday", dateGroupLabel(today.minusDays(1), today))
    }

    @Test
    fun `labels older dates in the same year without the year`() {
        assertEquals("22 July", dateGroupLabel(today.minusDays(2), today))
    }

    @Test
    fun `includes the year for dates outside the current year`() {
        assertEquals("31 December 2025", dateGroupLabel(LocalDate.of(2025, 12, 31), today))
    }

    /** Crossing a year boundary must not be mistaken for "same year" and lose the year. */
    @Test
    fun `yesterday still wins across a year boundary`() {
        val newYearsDay = LocalDate.of(2026, 1, 1)
        assertEquals("Yesterday", dateGroupLabel(LocalDate.of(2025, 12, 31), newYearsDay))
    }

    @Test
    fun `groups newest first and preserves order`() {
        val groups = SampleData.transactions.groupedByDate(today)
        assertEquals(listOf("Today", "Yesterday", "22 July", "21 July", "20 July", "19 July", "18 July", "17 July"), groups.map { it.first })
        assertEquals(3, groups.first().second.size)
    }
}
