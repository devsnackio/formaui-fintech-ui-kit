/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.components

import dev.formaui.fintechuikit.components.chart.topSlices
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.SpendCategory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The rule that keeps the spending donut to one arc per palette colour.
 *
 * Worth a test because the failure it prevents is silent: too many categories and the chart simply
 * reuses a colour, drawing two different categories as the same swatch. Nothing throws, and the
 * card still looks like a chart.
 */
class SpendingSlicesTest {

    private fun usd(major: Int) = Money(major * 100L, Currency.USD)

    private fun spend(vararg pairs: Pair<SpendCategory, Int>) =
        pairs.map { (category, major) -> category to usd(major) }

    /** The palette this is bounded by has six colours; the app's own sample spend has eight. */
    private val paletteSize = 6

    @Test
    fun aListThatAlreadyFitsIsLeftWhole() {
        val slices = spend(
            SpendCategory.Housing to 900,
            SpendCategory.Groceries to 300,
        ).topSlices(limit = paletteSize)

        assertEquals(listOf("Housing" to usd(900), "Groceries" to usd(300)), slices)
    }

    /** Exactly at the limit is still a fit: nothing is given up to an "Other" of one item. */
    @Test
    fun aListExactlyAtTheLimitKeepsEveryCategory() {
        val slices = spend(
            SpendCategory.Housing to 600,
            SpendCategory.Groceries to 500,
            SpendCategory.Dining to 400,
        ).topSlices(limit = 3)

        assertEquals(listOf("Housing", "Groceries", "Dining"), slices.map { it.first })
    }

    @Test
    fun theTailFoldsIntoOneOtherSliceThatCarriesItsFullValue() {
        val slices = spend(
            SpendCategory.Housing to 500,
            SpendCategory.Groceries to 400,
            SpendCategory.Dining to 30,
            SpendCategory.Transport to 20,
            SpendCategory.Utilities to 10,
        ).topSlices(limit = 3)

        assertEquals(
            listOf("Housing" to usd(500), "Groceries" to usd(400), "Other" to usd(60)),
            slices,
        )
    }

    /** Biggest first, whatever order the caller hands over — the fold depends on the ranking. */
    @Test
    fun slicesComeBackLargestFirst() {
        val slices = spend(
            SpendCategory.Dining to 100,
            SpendCategory.Housing to 900,
            SpendCategory.Groceries to 400,
        ).topSlices(limit = paletteSize)

        assertEquals(listOf("Housing", "Groceries", "Dining"), slices.map { it.first })
    }

    /**
     * The case the fold exists for. Eight categories against six colours is what the app actually
     * renders, so this asserts on the real fixture rather than a contrived list.
     */
    @Test
    fun theSampleSpendFitsThePalette() {
        val slices = SampleData.spendByCategory.topSlices(limit = paletteSize)

        assertEquals(paletteSize, slices.size)
        assertEquals("Other", slices.last().first)
        // Grouped, not dropped: the arcs still add up to the total shown in the donut's hole.
        assertEquals(
            SampleData.spendByCategory.sumOf { it.second.minorUnits },
            slices.sumOf { it.second.minorUnits },
        )
    }
}
