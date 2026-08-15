@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The white card an auth form sits in — DESIGN.md's `ex-auth-form-card`.
 *
 * The doc gives sign-in its own surface ("re-uses feature-card chrome with text-input
 * primitives inside"), which is also why the auth screens set no background of their own: a
 * white card on the sage canvas *is* the brand's elevation model, so the contrast does the work
 * a shadow would elsewhere.
 *
 * The card is handed exactly **one** child. `FormaCard` lays its content slot out with
 * `Arrangement.spacedBy(FormaTheme.spacing.md)` — FormaUI's 16dp, not the brand's 12dp — and
 * exposes no arrangement parameter, so that gap is unreachable from here. `spacedBy` only
 * inserts space *between* siblings, so a single child costs nothing and moves the rhythm to the
 * inner Column, where a form's fields, its terms row, and its CTA can be spaced deliberately.
 */
/**
 * [title] is optional. When the screen already carries its own headline — as the auth screens do,
 * so that the brand lockup, the headline and the form read as one descending stack — the card is
 * a pure container and must NOT repeat the heading: two headings on one screen give a screen
 * reader two things to jump to for the same place. DESIGN.md's `ex-auth-form-card` specifies only
 * background, radius and padding, so a titleless card is still spec-correct.
 */
@Composable
fun AuthFormCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            if (title != null) {
                Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
                    Text(
                        text = title,
                        style = FormaTheme.typography.material.headlineSmall,
                        // The card carries no heading semantics of its own, so TalkBack has
                        // nothing to jump to without this — the same gap FormaBottomSheet
                        // documents.
                        modifier = Modifier.semantics { heading() },
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = FormaTheme.typography.material.bodySmall,
                            color = FormaTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Preview(name = "AuthFormCard · light")
@Composable
private fun AuthFormCardPreview() {
    FinTechTheme {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            AuthFormCard(title = "Welcome back", subtitle = "Sign in to continue.") {
                Text("Form fields go here.", style = FormaTheme.typography.material.bodyMedium)
            }
        }
    }
}

@Preview(name = "AuthFormCard · dark")
@Composable
private fun AuthFormCardDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(Modifier.padding(FinTechTheme.spacing.lg)) {
            AuthFormCard(title = "Welcome back", subtitle = "Sign in to continue.") {
                Text("Form fields go here.", style = FormaTheme.typography.material.bodyMedium)
            }
        }
    }
}
