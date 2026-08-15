@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.icon
import dev.formaui.fintechuikit.components.status.StatusPill
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.TransactionStatus
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * One transaction in a list.
 *
 * Two brand rules are load-bearing here:
 *
 *  - The amount is set in `FormaTheme.typography.numeric` (tabular figures) so that a column
 *    of amounts aligns on the decimal point rather than jittering per digit width.
 *  - A credit is coloured with `brandColors.positive`, **never** the CTA green. DESIGN.md is
 *    explicit that Wise green is the conversion colour and must not be repurposed as a success
 *    indicator; using it here would make every incoming payment compete with the primary action.
 */
@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val brand = FinTechTheme.brandColors
    val amountColor = when {
        transaction.status == TransactionStatus.Failed -> brand.mute
        transaction.amount.isCredit -> brand.positive
        else -> FormaTheme.colorScheme.onSurface
    }

    FormaListItem(
        headline = transaction.merchant,
        supporting = transaction.note ?: transaction.category.displayName,
        modifier = modifier,
        onClick = onClick,
        leading = {
            FormaAvatar(size = FormaAvatarSize.Medium) {
                Icon(
                    imageVector = transaction.category.icon,
                    // The category is already written in the supporting text, so repeating it
                    // here would make TalkBack read it twice.
                    contentDescription = null,
                )
            }
        },
        trailing = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
            ) {
                Text(
                    text = transaction.amount.formatWithSymbol(signed = transaction.amount.isCredit),
                    // Down from the 16sp numeric default to 14sp. copy() (not merge) keeps the
                    // tabular figures and semibold weight — only the size shrinks — so amounts
                    // still align and stay emphasised.
                    style = FormaTheme.typography.numeric.copy(fontSize = 14.sp, lineHeight = 20.sp),
                    color = amountColor,
                    textAlign = TextAlign.End,
                )
                if (transaction.status != TransactionStatus.Completed) {
                    StatusPill(transaction.status)
                }
            }
        },
        // M3's ListItem sets the headline in bodyLarge and the supporting line in bodyMedium,
        // which on the brand scale are 20sp and 16sp — sizes meant for lead paragraphs, not a
        // dense list, and both larger than the 14sp amount they sit beside. Step each down one
        // slot so the row reads as a data row: merchant 16sp, note 14sp, amount 14sp semibold
        // still carrying the emphasis through weight and colour rather than size.
        headlineTextStyle = FormaTheme.typography.material.bodyMedium,
        supportingTextStyle = FormaTheme.typography.material.bodySmall,
    )
}

@Preview(name = "TransactionRow · light")
@Composable
private fun TransactionRowPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            SampleData.transactions.take(4).forEach { TransactionRow(it, onClick = {}) }
        }
    }
}

@Preview(name = "TransactionRow · dark")
@Composable
private fun TransactionRowDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            SampleData.transactions.take(4).forEach { TransactionRow(it, onClick = {}) }
        }
    }
}
