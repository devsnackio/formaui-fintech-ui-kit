@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The "Today" / "Yesterday" / "22 July" divider between transaction groups.
 *
 * Labels come from [dev.formaui.fintechuikit.data.dateGroupLabel]; this only renders them.
 */
@Composable
fun TransactionDateHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = FormaTheme.typography.material.titleSmall,
        color = FormaTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = FinTechTheme.spacing.lg,
                end = FinTechTheme.spacing.lg,
                top = FinTechTheme.spacing.lg,
                bottom = FinTechTheme.spacing.sm,
            ),
    )
}

@Preview
@Composable
private fun TransactionDateHeaderPreview() {
    FinTechTheme {
        Column {
            TransactionDateHeader("Today")
            TransactionDateHeader("Yesterday")
            TransactionDateHeader("22 July")
        }
    }
}
