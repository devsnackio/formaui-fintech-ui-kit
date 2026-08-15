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
import dev.formaui.components.chart.FormaChartDefaults
import dev.formaui.components.chart.FormaLineChart
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/**
 * Balance-over-time as a smooth area line.
 *
 * The line uses the brand primary (the chart's default), which is fine here: a trend line is a
 * data visualisation, not a call to action, so it does not collide with the CTA-green rule the
 * way a coloured button would.
 */
@Composable
fun BalanceTrendCard(
    values: List<Float>,
    modifier: Modifier = Modifier,
    xLabels: List<String> = emptyList(),
    currency: Currency = Currency.USD,
) {
    val latest = values.lastOrNull() ?: 0f
    val first = values.firstOrNull() ?: 0f
    val deltaPct = if (first != 0f) (latest - first) / first * 100 else 0f

    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md)) {
            Text("Balance trend", style = FormaTheme.typography.material.headlineSmall)
            Row2(
                left = Money(latest.toLong() * minorFactor(currency), currency).formatWithSymbol(),
                right = "%+.1f%%".format(deltaPct),
                rightPositive = deltaPct >= 0,
            )
            FormaLineChart(
                values = values,
                xLabels = xLabels,
                showPoints = false,
                contentDescription = "Balance trend over ${values.size} weeks, latest " +
                    Money(latest.toLong() * minorFactor(currency), currency).formatWithSymbol(),
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

/** The trend values are whole major units; scale them to minor units for [Money]. */
private fun minorFactor(currency: Currency): Long =
    generateSequence(1L) { it * 10 }.elementAt(currency.decimalPlaces)

@Composable
private fun Row2(left: String, right: String, rightPositive: Boolean) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(left, style = FormaTheme.typography.numeric.merge(FormaTheme.typography.material.titleLarge))
        Text(
            right,
            style = FormaTheme.typography.numeric.merge(FormaTheme.typography.material.titleMedium),
            color = if (rightPositive) FinTechTheme.brandColors.positive
            else FormaTheme.colorScheme.error,
        )
    }
}

@Preview(name = "BalanceTrend · light", heightDp = 360)
@Composable
private fun BalanceTrendCardPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            BalanceTrendCard(SampleData.balanceTrend, xLabels = SampleData.balanceTrendLabels)
        }
    }
}

@Preview(name = "BalanceTrend · dark", heightDp = 360)
@Composable
private fun BalanceTrendCardDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            BalanceTrendCard(SampleData.balanceTrend, xLabels = SampleData.balanceTrendLabels)
        }
    }
}
