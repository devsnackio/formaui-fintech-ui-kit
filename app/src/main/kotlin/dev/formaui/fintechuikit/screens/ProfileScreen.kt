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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.bottomsheet.FormaBottomSheet
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.dialog.FormaAlertDialog
import dev.formaui.components.radiobutton.FormaRadioButton
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.SettingsRow
import dev.formaui.fintechuikit.components.common.SettingsToggleRow
import dev.formaui.fintechuikit.screens.settings.Language
import dev.formaui.fintechuikit.screens.settings.LanguagePickerSheet
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.ThemeMode

/**
 * The profile screen: an identity header, preference toggles, settings rows, and sign-out.
 *
 * Preference switches are hoisted so the host owns them; sign-out is guarded by a
 * [FormaAlertDialog] because it is a destructive, not-obviously-reversible action.
 *
 * [appearanceOpen] is hoisted for a different reason than the switches: the appearance sheet now has
 * **two** ways in — the "Appearance" row on this screen, and the settings action in the shell's app
 * bar. A screen cannot own state that something outside it also opens, so the flag moved to the host
 * and this screen reports both entry points through [onAppearanceOpenChange]. `confirmingSignOut`
 * stays internal because the only thing that opens it is the button right above it.
 */
@Composable
fun ProfileScreen(
    biometricsEnabled: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.EnglishUS,
    onLanguageChange: (Language) -> Unit = {},
    languageOpen: Boolean = false,
    onLanguageOpenChange: (Boolean) -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onStatementsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    appearanceOpen: Boolean = false,
    onAppearanceOpenChange: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
) {
    var confirmingSignOut by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FinTechTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
    ) {
        ProfileHeader()

        SettingsGroup(title = "Preferences") {
            SettingsToggleRow(
                icon = Icons.Filled.Fingerprint,
                title = "Biometric unlock",
                checked = biometricsEnabled,
                onCheckedChange = onBiometricsChange,
            )
            SettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Push notifications",
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChange,
            )
        }

        SettingsGroup(title = "Account") {
            SettingsRow(
                Icons.Filled.Security,
                "Security",
                value = "Card PIN and devices",
                onClick = onSecurityClick,
            )
            SettingsRow(
                Icons.Filled.Language,
                "Language",
                value = language.displayName,
                onClick = { onLanguageOpenChange(true) },
            )
            SettingsRow(
                Icons.Filled.DarkMode,
                "Appearance",
                value = themeMode.displayName,
                onClick = { onAppearanceOpenChange(true) },
            )
            SettingsRow(
                Icons.Filled.Description,
                "Statements",
                value = "Export a date range",
                onClick = onStatementsClick,
            )
            SettingsRow(
                Icons.AutoMirrored.Filled.HelpOutline,
                "Help & support",
                onClick = onHelpClick,
            )
        }

        FormaButton(
            onClick = { confirmingSignOut = true },
            variant = FormaButtonVariant.Outlined,
            shape = BrandDefaults.buttonShape,
            contentPadding = BrandDefaults.buttonContentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign out")
        }
    }

    if (confirmingSignOut) {
        FormaAlertDialog(
            onDismissRequest = { confirmingSignOut = false },
            title = "Sign out?",
            text = "You'll need to sign back in to access your account.",
            confirmButton = {
                FormaButton(
                    onClick = {
                        confirmingSignOut = false
                        onSignOut()
                    },
                    shape = BrandDefaults.buttonShape,
                    contentPadding = BrandDefaults.buttonContentPadding,
                ) { Text("Sign out") }
            },
            dismissButton = {
                FormaButton(
                    onClick = { confirmingSignOut = false },
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                ) { Text("Cancel") }
            },
        )
    }

    if (languageOpen) {
        LanguagePickerSheet(
            selected = language,
            onSelect = {
                onLanguageChange(it)
                onLanguageOpenChange(false)
            },
            onDismiss = { onLanguageOpenChange(false) },
        )
    }

    if (appearanceOpen) {
        AppearancePickerSheet(
            selected = themeMode,
            onSelect = {
                onThemeModeChange(it)
                onAppearanceOpenChange(false)
            },
            onDismiss = { onAppearanceOpenChange(false) },
        )
    }
}

/**
 * A bottom-sheet picker for [ThemeMode].
 *
 * `FormaAlertDialog` only takes string title/text, so a radio list needs the `ColumnScope`
 * content slot that `FormaBottomSheet` provides. Each option is one selectable row — the row
 * carries `Modifier.selectable(role = Role.RadioButton)` and the inner `FormaRadioButton` gets
 * `onClick = null`, so the whole row is a single ~48dp target announced as one radio option.
 *
 * The horizontal inset is applied **inside** each row, after `selectable`, not on the enclosing
 * Column — so the clickable area (and its ripple) spans the full sheet width edge-to-edge while
 * the label and radio stay inset. Padding on the Column would shrink the ripple to the inset
 * width, leaving a dead gap at both edges.
 */
@Composable
private fun AppearancePickerSheet(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    FormaBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(vertical = FinTechTheme.spacing.lg)) {
            Text(
                "Appearance",
                style = FormaTheme.typography.material.headlineSmall,
                modifier = Modifier.padding(
                    start = FinTechTheme.spacing.lg,
                    end = FinTechTheme.spacing.lg,
                    bottom = FinTechTheme.spacing.sm,
                ),
            )
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == selected,
                            role = Role.RadioButton,
                            onClick = { onSelect(mode) },
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
                    FormaRadioButton(selected = mode == selected, onClick = null)
                    Text(mode.displayName, style = FormaTheme.typography.material.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
    ) {
        FormaAvatar(initials = "AM", size = FormaAvatarSize.Large)
        Text("Alex Morgan", style = FormaTheme.typography.material.headlineSmall)
        Text(
            "alex.morgan@example.com",
            style = FormaTheme.typography.material.bodyMedium,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
        Text(
            title,
            style = FormaTheme.typography.material.titleSmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child on purpose — FormaCard spaces its content slot with
            // Arrangement.spacedBy(FormaTheme.spacing.md) (16dp) and offers no way to change it,
            // so handing it the rows directly put a gap between every setting. spacedBy needs
            // siblings to separate, so a single Column costs nothing and the rows sit flush.
            // Each row already carries its own vertical padding, so they still breathe.
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Preview(name = "Profile · light", heightDp = 1300)
@Composable
private fun ProfileScreenPreview() {
    FinTechTheme {
        ProfileScreen(
            biometricsEnabled = true,
            onBiometricsChange = {},
            notificationsEnabled = false,
            onNotificationsChange = {},
            themeMode = ThemeMode.System,
            onThemeModeChange = {},
        )
    }
}

@Preview(name = "Profile · dark", heightDp = 1300)
@Composable
private fun ProfileScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        ProfileScreen(
            biometricsEnabled = true,
            onBiometricsChange = {},
            notificationsEnabled = false,
            onNotificationsChange = {},
            themeMode = ThemeMode.Dark,
            onThemeModeChange = {},
        )
    }
}
