@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.balance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.formatAmount
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The hero balance card.
 *
 * The amount is the loudest thing on the dashboard, so it takes the display tier at weight 900
 * — but rendered through `typography.numeric`, which carries the tabular-figure feature the
 * display styles do not. Without that, the balance would visibly re-flow every time a digit
 * changed.
 */
@Composable
fun BalanceCard(
    account: Account,
    modifier: Modifier = Modifier,
    trendLabel: String? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
            Text(
                text = "${account.label} balance",
                style = FormaTheme.typography.material.bodyMedium,
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = account.balance.currency.symbol,
                    style = FormaTheme.typography.material.headlineMedium,
                    color = FormaTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = FinTechTheme.spacing.xxs),
                )
                Text(
                    text = account.balance.formatAmount(),
                    style = FormaTheme.typography.numeric.merge(
                        FormaTheme.typography.material.displaySmall,
                    ),
                )
            }
            if (trendLabel != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null, // trendLabel already says it in words
                        tint = FinTechTheme.brandColors.positive,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = trendLabel,
                        style = FormaTheme.typography.material.bodySmall,
                        color = FinTechTheme.brandColors.positive,
                    )
                }
            }
            if (actions != null) {
                Row(Modifier.padding(top = FinTechTheme.spacing.sm)) { actions() }
            }
        }
    }
}

@Preview(name = "BalanceCard · light")
@Composable
private fun BalanceCardPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            BalanceCard(SampleData.primaryAccount, trendLabel = "+4.2% this month")
        }
    }
}

@Preview(name = "BalanceCard · dark")
@Composable
private fun BalanceCardDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            BalanceCard(SampleData.primaryAccount, trendLabel = "+4.2% this month")
        }
    }
}
