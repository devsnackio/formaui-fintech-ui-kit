@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.components.common.SettingsRow
import dev.formaui.fintechuikit.components.status.StatusPill
import dev.formaui.fintechuikit.components.status.StatusTone
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Security settings, behind Profile's "Security" row.
 *
 * **The biometric toggle deliberately stays on Profile.** It is one hoisted `Boolean`, and rendering
 * the same switch here as well would give the user two controls for one setting — the classic way a
 * settings tree starts disagreeing with itself. A slightly thin screen beats a duplicated toggle.
 *
 * The devices list is display-only. There is no session model behind it and no way to actually revoke
 * anything, so the rows carry no `onClick` — an inert "Sign out this device" would be exactly the
 * kind of promise this batch of work exists to remove.
 */
@Composable
fun SecurityScreen(
    onChangePin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowScreen(
        title = "Security",
        onBack = onBack,
        modifier = modifier,
    ) {
        Text(
            text = "How you get into the app, and where you're already signed in.",
            style = FormaTheme.typography.material.bodyLarge,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )

        // Both groups get a label. The devices card had one and the PIN card did not, which left
        // the first card floating under the app bar with nothing naming it — Profile's
        // SettingsGroup labels every group for the same reason.
        SecurityGroup(title = "PIN") {
            SettingsRow(
                icon = Icons.Filled.Lock,
                title = "Card PIN",
                value = "Used at shops and cash machines",
                onClick = onChangePin,
            )
        }

        SecurityGroup(title = "Where you're signed in") {
            // No onClick anywhere in this group: nothing here can actually end a session, and a
            // tappable row would promise otherwise.
            SettingsRow(
                icon = Icons.Filled.PhoneAndroid,
                title = "Pixel 8 Pro",
                value = "Signed in now",
                // The one row the user is actually looking at. Without the marker both rows read
                // as equally remote, and "is that me?" is the first question this list raises.
                trailing = { StatusPill(label = "This device", tone = StatusTone.Positive) },
            )
            SettingsRow(
                icon = Icons.Filled.Laptop,
                title = "MacBook Air",
                value = "Last used 3 days ago",
            )
        }

        Text(
            text = "Lost your phone? Freeze your card from the Card tab — it stops new payments " +
                "straight away, and unfreezing is instant. Biometric unlock lives on your profile, " +
                "under Preferences.",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A labelled card of settings rows — the same shape as `ProfileScreen`'s private `SettingsGroup`.
 *
 * Deliberately a local copy rather than a shared component. The two are three lines of layout that
 * happen to agree today; hoisting them into `components/common/` would create a shared API for
 * something neither screen has asked to vary, and the first divergence would mean adding a parameter
 * to satisfy one caller. If a third settings screen appears, that is the moment to extract it.
 *
 * `contentPadding = 0` is load-bearing: these rows are full-width tap targets, and the card's normal
 * 24dp inset would stop the ripple short of the card edges.
 */
@Composable
private fun SecurityGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
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
            // One child: FormaCard's 16dp content arrangement cannot be overridden.
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Preview(name = "Security · light", heightDp = 700)
@Composable
private fun SecurityScreenPreview() {
    FinTechTheme {
        SecurityScreen(onChangePin = {}, onBack = {})
    }
}

@Preview(name = "Security · dark", heightDp = 700)
@Composable
private fun SecurityScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        SecurityScreen(onChangePin = {}, onBack = {})
    }
}
