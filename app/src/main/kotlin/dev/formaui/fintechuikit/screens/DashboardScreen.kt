@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.fintechuikit.components.balance.BalanceCard
import dev.formaui.fintechuikit.components.chart.BalanceTrendCard
import dev.formaui.fintechuikit.components.chart.SpendingDonutCard
import dev.formaui.fintechuikit.components.common.QuickActionButton
import dev.formaui.fintechuikit.components.common.SectionHeader
import dev.formaui.fintechuikit.components.transaction.TransactionRow
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The home screen: balance, quick actions, trend, spending, and recent activity.
 *
 * Stateless — the callbacks are hoisted so `MainActivity` decides what each action navigates
 * to. It renders whatever data it is handed, which is what lets the preview and the running
 * app share [SampleData].
 */
@Composable
fun DashboardScreen(
    account: Account,
    recentTransactions: List<Transaction>,
    spendByCategory: List<Pair<SpendCategory, Money>>,
    balanceTrend: List<Float>,
    modifier: Modifier = Modifier,
    onSendClick: () -> Unit = {},
    onAddMoneyClick: () -> Unit = {},
    onRequestClick: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onViewBalances: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FinTechTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
    ) {
        // Unstaggered, like all four tabs. `FinTechNavHost` cross-fades tab → tab, and a tab switch
        // is the most-repeated navigation in the app — content that resolves top-down on every tap
        // stops reading as polish by the third tap. It also avoids compounding with the host's own
        // fade; `screenMotion` in FinTechNavHost has the reasoning.
        BalanceCard(
            account = account,
            trendLabel = "+4.2% this month",
            // BalanceCard has carried this slot since it was written and nothing ever passed it.
            // Using it here means reaching the other three balances costs no new button competing
            // with the quick-action row below.
            actions = {
                FormaButton(
                    onClick = onViewBalances,
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("View all balances")
                }
            },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl)) {
            QuickActionButton(Icons.Filled.SwapHoriz, "Send", onClick = onSendClick, primary = true)
            QuickActionButton(Icons.Filled.Add, "Add money", onClick = onAddMoneyClick)
            QuickActionButton(Icons.Filled.QrCode, "Request", onClick = onRequestClick)
        }

        BalanceTrendCard(
            values = balanceTrend,
            xLabels = SampleData.balanceTrendLabels,
            currency = account.balance.currency,
        )

        SpendingDonutCard(spend = spendByCategory)

        SectionHeader(
            title = "Recent activity",
            actionLabel = "See all",
            onActionClick = onSeeAllTransactions,
        )
        // The rows sit in a card so the list reads as one rounded block. contentPadding = 0 lets
        // each FormaListItem span the full width, and the card's xl radius clips the top and
        // bottom corners.
        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // FormaCard lays its content out with Arrangement.spacedBy(FormaTheme.spacing.md) —
            // FormaUI's 16dp, not the brand's 12dp — and exposes no arrangement parameter, so that
            // gap is otherwise unreachable (LocalFormaSpacing is internal). spacedBy only inserts
            // space *between* siblings, so handing the card exactly one child costs nothing and
            // moves the row rhythm here. Rows sit flush: each ListItem already carries 8dp of
            // internal vertical padding, giving 16dp between text blocks without an added gap.
            Column(modifier = Modifier.fillMaxWidth()) {
                recentTransactions.forEach { txn ->
                    TransactionRow(txn, onClick = { onTransactionClick(txn) })
                }
            }
        }
    }
}

@Preview(name = "Dashboard · light", heightDp = 1600)
@Composable
private fun DashboardScreenPreview() {
    FinTechTheme {
        DashboardScreen(
            account = SampleData.primaryAccount,
            recentTransactions = SampleData.recentTransactions,
            spendByCategory = SampleData.spendByCategory,
            balanceTrend = SampleData.balanceTrend,
        )
    }
}

@Preview(name = "Dashboard · dark", heightDp = 1600)
@Composable
private fun DashboardScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        DashboardScreen(
            account = SampleData.primaryAccount,
            recentTransactions = SampleData.recentTransactions,
            spendByCategory = SampleData.spendByCategory,
            balanceTrend = SampleData.balanceTrend,
        )
    }
}
