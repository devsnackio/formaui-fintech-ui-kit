@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.balance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * One currency holding in a multi-currency list — flag, currency name, balance.
 *
 * Text styles are stepped down one slot from M3's `ListItem` defaults (`bodyLarge`/`bodyMedium`,
 * which are 20sp/16sp on this brand's scale) to match `TransactionRow` and `SettingsRow`, so every
 * list row in the app reads at the same size. The trailing balance keeps the full 16sp
 * `typography.numeric`: it is the reason this row exists, and it is the only element here that
 * needs tabular figures to align down a column.
 */
@Composable
fun CurrencyBalanceRow(
    account: Account,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    FormaListItem(
        headline = account.label,
        supporting = account.balance.currency.code,
        modifier = modifier,
        onClick = onClick,
        leading = {
            FormaAvatar(size = FormaAvatarSize.Medium) {
                // The flag is decorative — the currency name and code are already in the row's
                // text, so announcing the emoji would just add noise.
                Text(account.balance.currency.flag, style = FormaTheme.typography.material.titleMedium)
            }
        },
        trailing = {
            Text(
                text = account.balance.formatWithSymbol(),
                style = FormaTheme.typography.numeric,
            )
        },
        headlineTextStyle = FormaTheme.typography.material.bodyMedium,
        supportingTextStyle = FormaTheme.typography.material.bodySmall,
    )
}

@Preview
@Composable
private fun CurrencyBalanceRowPreview() {
    FinTechTheme {
        Column(Modifier.padding(vertical = FinTechTheme.spacing.sm)) {
            SampleData.accounts.forEach { CurrencyBalanceRow(it, onClick = {}) }
        }
    }
}
