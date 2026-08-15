@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.bottomsheet.FormaBottomSheet
import dev.formaui.components.radiobutton.FormaRadioButton
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The languages the demo offers.
 *
 * Display only — picking one changes what the Profile row reports and nothing else. Real localisation
 * means string resources and a locale change, which is a separate piece of work; this models the
 * *setting*, not the translation.
 */
enum class Language(val displayName: String) {
    EnglishUS("English (US)"),
    EnglishUK("English (UK)"),
    French("Français"),
    German("Deutsch"),
    Spanish("Español"),
    Japanese("日本語"),
}

/**
 * Picks a [Language] from a bottom sheet.
 *
 * **A sheet, not a screen, on purpose.** Language is the same shape of choice as Appearance — pick one
 * of N and you are done — and Appearance already opens a sheet from an adjacent `SettingsRow`. Giving
 * one a full destination and the other a sheet would make two identical decisions behave differently
 * for no reason the user could infer.
 *
 * The row structure mirrors `AppearancePickerSheet` exactly, including the part that is easy to get
 * wrong: the horizontal inset goes **inside** each row, after `selectable`, so the ripple spans the
 * full sheet width while the label stays inset. Padding the enclosing Column instead would shrink the
 * ripple and leave dead gutters at both edges.
 */
@Composable
fun LanguagePickerSheet(
    selected: Language,
    onSelect: (Language) -> Unit,
    onDismiss: () -> Unit,
) {
    FormaBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(vertical = FinTechTheme.spacing.lg)) {
            Text(
                "Language",
                style = FormaTheme.typography.material.headlineSmall,
                modifier = Modifier.padding(
                    start = FinTechTheme.spacing.lg,
                    end = FinTechTheme.spacing.lg,
                    bottom = FinTechTheme.spacing.sm,
                ),
            )
            Language.entries.forEach { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = language == selected,
                            role = Role.RadioButton,
                            onClick = { onSelect(language) },
                        )
                        // Inner padding — after selectable — so the ripple fills full width.
                        .padding(
                            horizontal = FinTechTheme.spacing.lg,
                            vertical = FinTechTheme.spacing.md,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.md),
                ) {
                    // Display-only: the row's selectable owns the click and the semantics.
                    FormaRadioButton(selected = language == selected, onClick = null)
                    Text(language.displayName, style = FormaTheme.typography.material.bodyLarge)
                }
            }
        }
    }
}

@Preview(name = "Language picker · light")
@Composable
private fun LanguagePickerSheetPreview() {
    FinTechTheme {
        LanguagePickerSheet(selected = Language.EnglishUS, onSelect = {}, onDismiss = {})
    }
}
