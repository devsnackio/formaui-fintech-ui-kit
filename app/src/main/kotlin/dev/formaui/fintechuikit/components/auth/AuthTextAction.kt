@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A low-emphasis text action — "Forgot password?", "Create an account", "Read the Terms".
 *
 * Two overrides carry this component; both are places the Material 3 default is wrong *for this
 * brand specifically*:
 *
 *  - **Colour.** [BrandDefaults.textButtonColors], because M3 draws a text button's label in
 *    `colorScheme.primary` — Wise green, which fails contrast on any of this brand's surfaces.
 *    That correction is shared with every other text button in the app; see its KDoc for the
 *    measurement. The label additionally takes `{typography.body-sm-strong}`, the doc's own
 *    `nav-link` spec.
 *  - **Padding.** Zero horizontally. The brand's 24dp button padding is *container* padding, and
 *    a text button has no container to inset — any horizontal padding here just pushes the label
 *    inboard of the field it belongs to, so "Forgot password?" no longer lines up with the right
 *    edge of the password field above it and the label reads as slightly adrift. Vertical padding
 *    stays for comfort; `FormaButton`'s 48dp minimum keeps the touch target legal either way.
 *    Callers that want a gap should space the row, not pad the button.
 */
@Composable
fun AuthTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FormaButton(
        onClick = onClick,
        modifier = modifier,
        variant = FormaButtonVariant.Text,
        enabled = enabled,
        shape = BrandDefaults.buttonShape,
        colors = BrandDefaults.textButtonColors,
        contentPadding = PaddingValues(
            horizontal = FinTechTheme.spacing.sm,
            vertical = FinTechTheme.spacing.sm,
        ),
    ) {
        Text(label, style = FormaTheme.typography.material.titleSmall)
    }
}

@Preview(name = "AuthTextAction · light")
@Composable
private fun AuthTextActionPreview() {
    FinTechTheme {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
        ) {
            AuthTextAction("Forgot password?", onClick = {})
            AuthTextAction("Create an account", onClick = {})
            AuthTextAction("Read the Terms of Service", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "AuthTextAction · dark")
@Composable
private fun AuthTextActionDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
        ) {
            AuthTextAction("Forgot password?", onClick = {})
            AuthTextAction("Create an account", onClick = {})
            AuthTextAction("Read the Terms of Service", onClick = {}, enabled = false)
        }
    }
}
