@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.textEntryKey
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/** The three digit rows; the fourth is assembled by hand around the "0". */
private val DigitRows = listOf("123", "456", "789")

private val KeySize = 72.dp

/**
 * The on-screen keypad for PIN entry.
 *
 * A PIN deserves its own keypad rather than the system numeric IME: the keys stay put between
 * screens, the layout can't be swapped by a third-party keyboard, and nothing the user taps ever
 * reaches an input-method process.
 *
 * The keys are sized and spaced by hand instead of laid out in a grid because the set is fixed
 * at twelve — a `LazyVerticalGrid` would add scroll machinery and lose the guarantee that every
 * key is exactly [KeySize] regardless of the digit inside it.
 *
 * `Text`-variant buttons need an explicit content colour: M3 draws them in `colorScheme.primary`,
 * which here is the Wise green CTA, and a keypad rendered entirely in the conversion colour is
 * precisely what DESIGN.md's "reserve the green for the primary action" rule forbids.
 *
 * When [onBiometric] is null its slot renders as empty space rather than collapsing, so the "0"
 * stays under the "8" where the thumb expects it.
 */
@Composable
fun PinKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onBiometric: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DigitRows.forEach { row ->
            KeypadRow {
                row.forEach { digit ->
                    DigitKey(digit = digit, enabled = enabled, onClick = { onDigit(digit) })
                }
            }
        }
        KeypadRow {
            if (onBiometric != null) {
                FormaIconButton(
                    onClick = onBiometric,
                    modifier = Modifier.size(KeySize),
                    enabled = enabled,
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = "Unlock with biometrics")
                }
            } else {
                Spacer(Modifier.size(KeySize))
            }

            DigitKey(digit = '0', enabled = enabled, onClick = { onDigit('0') })

            FormaIconButton(
                onClick = onBackspace,
                modifier = Modifier.size(KeySize),
                enabled = enabled,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete last digit",
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun DigitKey(digit: Char, enabled: Boolean, onClick: () -> Unit) {
    FormaButton(
        onClick = onClick,
        modifier = Modifier
            .size(KeySize)
            // Tells accessibility services this is a key in a text-entry surface, not a button
            // that navigates — so a key press is announced as typing rather than as an action.
            .semantics { textEntryKey() },
        variant = FormaButtonVariant.Text,
        enabled = enabled,
        shape = FormaTheme.shapes.full,
        colors = BrandDefaults.textButtonColors,
        // The key *is* the target; any inset would just shrink the glyph inside a fixed circle.
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(digit.toString(), style = FormaTheme.typography.material.headlineSmall)
    }
}

@Preview(name = "PinKeypad · light", heightDp = 420)
@Composable
private fun PinKeypadPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            PinKeypad(onDigit = {}, onBackspace = {}, onBiometric = {})
        }
    }
}

@Preview(name = "PinKeypad · dark", heightDp = 420)
@Composable
private fun PinKeypadDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            PinKeypad(onDigit = {}, onBackspace = {})
        }
    }
}
