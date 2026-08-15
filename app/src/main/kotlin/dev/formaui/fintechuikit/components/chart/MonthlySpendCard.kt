/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.chart.FormaBarChart
import dev.formaui.components.chart.FormaChartDefaults
import dev.formaui.components.chart.FormaChartEntry
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/**
 * Outgoings per month as a bar chart.
 *
 * Bars take the brand primary by default. That is acceptable for a passive chart (see the
 * note in [BalanceTrendCard]); the rule DESIGN.md actually guards is CTA-green on *actions*.
 */
@Composable
fun MonthlySpendCard(
    monthlySpend: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
) {
    val entries = monthlySpend.map { (month, amount) -> FormaChartEntry(month, amount) }

    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {
            Text("Monthly spend", style = FormaTheme.typography.material.headlineSmall)
            FormaBarChart(
                entries = entries,
                showValueLabels = false,
                contentDescription = "Monthly spend over ${entries.size} months",
                // FormaUI's default entry animation is 800ms — by far the longest motion in the
                // app, and the one piece a NavHost-level reduced-motion switch cannot reach. null
                // renders the static final frame, which the reference explicitly supports.
                animationSpec = if (LocalReducedMotion.current) {
                    null
                } else {
                    FormaChartDefaults.EntryAnimationSpec
                },
            )
        }
    }
}

@Preview(name = "MonthlySpend · light", heightDp = 360)
@Composable
private fun MonthlySpendCardPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            MonthlySpendCard(SampleData.monthlySpend)
        }
    }
}

@Preview(name = "MonthlySpend · dark", heightDp = 360)
@Composable
private fun MonthlySpendCardDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            MonthlySpendCard(SampleData.monthlySpend)
        }
    }
}
