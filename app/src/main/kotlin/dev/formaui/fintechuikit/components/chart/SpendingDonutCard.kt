@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.chart.FormaChartDefaults
import dev.formaui.components.chart.FormaChartEntry
import dev.formaui.components.chart.FormaChartLegend
import dev.formaui.components.chart.FormaDonutChart
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/**
 * The ring's diameter, fixed rather than left to fill the card.
 *
 * The card spans the screen; a ring that grew with it would take the whole width on a tablet while
 * the legend under it stayed the same size.
 */
private val DonutSize = 180.dp

/**
 * The ring's thickness — 12dp against FormaUI's 24dp default, and thin on purpose.
 *
 * This is not a styling choice, it is the price of the rounded ends the ring is drawn with. A round
 * cap bulges half a stroke width past its arc at each end, and `FormaDonutChart` insets the drawn
 * arc by that overhang so the rounded ends land on the segment's true bounds. The consequence is a
 * floor: no segment can render narrower than the two caps that finish it, and one that tries
 * collapses to a dot.
 *
 * The floor is twice `stroke / (size − stroke)` radians: 17.6° at FormaUI's 24dp default, which is
 * **5.1%** of the 348° the six slices share, and four of this card's six fall under it. At 12dp it
 * is 8.2°, or **2.4%**, clearing the smallest slice the sample spend produces (Shopping, at 2.8%).
 *
 * So the thickness is load-bearing: thicken the ring and the small categories start reading as
 * dots again. [topSlices] handles the far tail; this handles what survives it.
 */
private val DonutStroke = 12.dp

/**
 * Spending-by-category as a donut with a total in the hole and a legend beside it.
 *
 * FormaUI charts never embed a legend, so [FormaChartLegend] is placed explicitly with the
 * **same entries and palette** — that pairing is what makes the legend swatches resolve to the
 * same colours the donut drew.
 *
 * The card draws at most one arc per palette colour; [topSlices] is what holds it to that.
 */
@Composable
fun SpendingDonutCard(
    spend: List<Pair<SpendCategory, Money>>,
    modifier: Modifier = Modifier,
) {
    val currency = spend.firstOrNull()?.second?.currency ?: Currency.USD
    val total = Money(spend.sumOf { it.second.minorUnits }, currency)

    // One palette, shared by the chart and the legend, and the ceiling on how many arcs there can
    // be. Indexing it directly rather than cycling it with `%` is the point: the slices are already
    // bounded by its size, and a modulo would quietly paper over the day that stops being true.
    val palette = brandChartPalette()
    val slices = spend.topSlices(limit = palette.size)
    val entries = slices.mapIndexed { i, (label, amount) ->
        FormaChartEntry(
            label = label,
            value = amount.minorUnits.toFloat(),
            color = palette[i],
        )
    }

    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg)) {
            Text("Spending", style = FormaTheme.typography.material.headlineSmall)
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
            ) {
                FormaDonutChart(
                    entries = entries,
                    segmentColors = palette,
                    modifier = Modifier.size(DonutSize),
                    contentDescription = "Spending by category, ${total.formatWithSymbol()} total",
                    // These two travel together — see [DonutStroke]. Rounded ends impose a minimum
                    // drawable arc, and the thin ring is what keeps that minimum below the smallest
                    // slice on the card. Passed explicitly even though Round is FormaUI's default,
                    // so the pairing is visible to whoever next reaches for the stroke width.
                    strokeWidth = DonutStroke,
                    strokeCap = StrokeCap.Round,
                    // FormaUI's default entry sweep is 800ms — the longest motion in the app, and the
                    // one piece a NavHost-level reduced-motion switch cannot reach. null renders the
                    // static final frame, which the reference explicitly supports.
                    animationSpec = if (LocalReducedMotion.current) {
                        null
                    } else {
                        FormaChartDefaults.EntryAnimationSpec
                    },
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Total",
                                style = FormaTheme.typography.material.labelSmall,
                                color = FormaTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                total.formatWithSymbol(),
                                style = FormaTheme.typography.numeric.merge(
                                    FormaTheme.typography.material.titleLarge,
                                ),
                            )
                        }
                    },
                )
                FormaChartLegend(
                    entries = entries,
                    colors = palette,
                    valueFormatter = { value ->
                        Money(value.toLong(), currency).formatWithSymbol()
                    },
                )
            }
        }
    }
}

/**
 * The [limit] largest categories, biggest first, with the tail summed into a single "Other".
 *
 * Two things go wrong without it, and both were on screen — this app's own sample spend covers
 * **eight** categories against a **six**-colour palette:
 *
 *  - **Repeated colours.** A seventh arc wraps back to the first colour, so the donut drew two
 *    cyan wedges and the legend showed the same swatch under two different names. A chart whose
 *    key does not identify its segments is a chart that lies about its data.
 *  - **A key longer than the chart.** Three of those eight categories were under 1.5% of the
 *    total, and each still claimed a full legend row with a name and an amount — a third of the
 *    key spent naming arcs no reader can pick out, in the taller half of the card.
 *
 * Nothing is hidden, only grouped: "Other" carries the exact sum of what it replaces, so the arcs
 * still account for the total in the hole. The fold happens **only when it must** — a list that
 * already fits comes back whole, rather than surrendering its last real category to a needless
 * "Other" that would be smaller than the thing it replaced.
 *
 * Sorting here rather than trusting the caller is deliberate: which categories survive depends on
 * the order, so the rule that decides it belongs next to the rule that applies it.
 */
internal fun List<Pair<SpendCategory, Money>>.topSlices(limit: Int): List<Pair<String, Money>> {
    require(limit >= 2) { "A donut needs room for at least one category and Other (got $limit)" }

    val ordered = sortedByDescending { it.second.minorUnits }
    val kept = if (ordered.size <= limit) ordered else ordered.take(limit - 1)
    val slices = kept.map { (category, amount) -> category.displayName to amount }

    val tail = ordered.drop(kept.size)
    if (tail.isEmpty()) return slices
    // The tail's own currency, not the head's: they are the same list, so taking it from the
    // entries actually being summed keeps the sum and its unit from ever coming apart.
    val other = Money(tail.sumOf { it.second.minorUnits }, tail.first().second.currency)
    return slices + ("Other" to other)
}

/**
 * A six-colour categorical palette drawn from the brand's own tokens.
 *
 * The CTA green is deliberately excluded — DESIGN.md forbids reusing it as anything but the
 * primary action, and a chart segment is not one.
 *
 * Every entry has to work as a **fill on the card in both schemes**, which is a stricter test than
 * a token passing as text. The deep green comes from `chartDeepGreen` rather than `inkDeep` for
 * exactly that reason: an ink that is legible written *on* a surface can still vanish when it *is*
 * the surface.
 */
@Composable
private fun brandChartPalette(): List<Color> {
    val brand = FinTechTheme.brandColors
    return listOf(
        brand.accentCyan,
        brand.accentOrange,
        brand.positive,
        brand.warning,
        brand.chartDeepGreen,
        brand.mute,
    )
}

@Preview(name = "SpendingDonut · light", heightDp = 620)
@Composable
private fun SpendingDonutCardPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            SpendingDonutCard(SampleData.spendByCategory)
        }
    }
}

@Preview(name = "SpendingDonut · dark", heightDp = 620)
@Composable
private fun SpendingDonutCardDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            SpendingDonutCard(SampleData.spendByCategory)
        }
    }
}
