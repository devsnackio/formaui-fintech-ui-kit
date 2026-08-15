@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.BrandLockup
import dev.formaui.fintechuikit.components.onboarding.OnboardingAccent
import dev.formaui.fintechuikit.components.onboarding.OnboardingIllustration
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The landing screen: brand lockup, a display-tier headline, and the two ways in.
 *
 * Three deliberate departures from the in-app screens:
 *
 *  - **It paints its own background and insets.** Every other screen renders inside
 *    `FinTechNavHost`'s `Scaffold`, which supplies both. Onboarding runs *before* the tab shell
 *    (a bottom bar here would offer destinations the user has no account for), so this screen
 *    has to stand on its own — hence the explicit `background` and [safeDrawingPadding]. Host
 *    it outside the `Scaffold`, or the insets apply twice.
 *  - **Hero padding, not screen padding.** `{spacing.3xl}` 48dp vertical over `{spacing.xl}`
 *    24dp horizontal is DESIGN.md's `hero-band` inset, and this is the one screen in the app
 *    that genuinely is a hero band.
 *  - **Start-aligned, not centred.** The brand's hero is a big left-aligned weight-900 headline;
 *    centring it would read as a generic app splash and throw away the typographic voice.
 *
 * The green appears exactly once, on the primary pill — the secondary way in is a text button,
 * because two filled CTAs would leave the conversion path ambiguous.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FormaTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(
                horizontal = FinTechTheme.spacing.xl,
                vertical = FinTechTheme.spacing.section,
            ),
    ) {
        BrandLockup()

        // The hero takes the slack so the actions stay pinned to the bottom edge. It scrolls on
        // its own in case a large font scale or a short screen overruns it — the actions must
        // never be the thing that gets pushed off.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            OnboardingIllustration(
                icon = Icons.Filled.Payments,
                accent = OnboardingAccent.Primary,
                size = 200.dp,
            )
            Spacer(Modifier.height(FinTechTheme.spacing.xxl))
            Text(
                "Money without borders.",
                style = FormaTheme.typography.material.displaySmall,
                color = FormaTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(FinTechTheme.spacing.md))
            Text(
                "Hold, send, and spend in 40+ currencies — at the real rate, " +
                    "with the fee on the label.",
                style = FormaTheme.typography.material.bodyLarge,
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
            FormaButton(
                onClick = onGetStarted,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Get started")
            }
            FormaButton(
                onClick = onSignIn,
                variant = FormaButtonVariant.Text,
                colors = BrandDefaults.textButtonColors,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("I already have an account")
            }
        }
    }
}

@Preview(name = "Welcome · light", heightDp = 800)
@Composable
private fun WelcomeScreenPreview() {
    FinTechTheme {
        WelcomeScreen(onGetStarted = {}, onSignIn = {})
    }
}

@Preview(name = "Welcome · dark", heightDp = 800)
@Composable
private fun WelcomeScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        WelcomeScreen(onGetStarted = {}, onSignIn = {})
    }
}
