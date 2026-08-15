@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.BrandLockup
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The lockup-and-headline stack above an auth form.
 *
 * This exists to carry the pre-session flow's voice into the forms. Welcome and Onboarding open
 * with the brand lockup and a big start-aligned headline; before this, sign-in opened cold on a
 * card whose title sat *inside* the chrome at `headlineSmall`. The flow lost its identity at the
 * exact step where the user is being asked to commit, and the page had no clear first line — the
 * card's own border was the first thing the eye landed on.
 *
 * Two deliberate choices:
 *
 *  - **`headlineMedium` (28sp), one step under Welcome's `displaySmall` (36sp).** A form is not a
 *    hero; matching the hero would flatten the flow's own hierarchy, and going smaller would let
 *    the card outweigh the sentence that explains it.
 *  - **Start-aligned, like the welcome hero.** The brand's headline voice is left-aligned; centring
 *    a form heading is the generic app-login look and throws that away.
 *
 * The heading semantics live here rather than on [AuthFormCard], so each screen exposes exactly
 * one heading to a screen reader. [subtitle] is for a line that does a job — setting an
 * expectation, naming a requirement — not for restating the headline in longer words.
 */
@Composable
fun AuthHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
    ) {
        // Small: the lockup identifies the flow, it does not lead it. At Medium the 32sp Black
        // wordmark would outweigh the 28sp SemiBold title directly beneath it.
        BrandLockup(size = FormaAvatarSize.Small)

        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
            Text(
                text = title,
                style = FormaTheme.typography.material.headlineMedium,
                color = FormaTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = FormaTheme.typography.material.bodyLarge,
                    color = FormaTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "AuthHeader · light")
@Composable
private fun AuthHeaderPreview() {
    FinTechTheme {
        HeaderStack()
    }
}

@Preview(name = "AuthHeader · dark")
@Composable
private fun AuthHeaderDarkPreview() {
    FinTechTheme(darkTheme = true) {
        HeaderStack()
    }
}

@Composable
private fun HeaderStack() {
    Column(
        Modifier
            .background(FormaTheme.colorScheme.background)
            .padding(FinTechTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xxl),
    ) {
        AuthHeader(title = "Welcome back")
        AuthHeader(
            title = "Create your account",
            subtitle = "It takes about two minutes.",
        )
    }
}
