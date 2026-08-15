@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/** One numbered clause. Content only — the screen decides how it is laid out. */
private data class Clause(val heading: String, val body: String)

private val Clauses = listOf(
    Clause(
        "Who we are",
        "Forma is a demonstration application. It holds no real money, moves no real funds, and " +
            "is not a licensed payment institution. Everything in it is sample data.",
    ),
    Clause(
        "Your account",
        "You are responsible for keeping your sign-in details and card PIN to yourself. If you " +
            "think someone else has them, freeze your card from the Card tab and change your PIN.",
    ),
    Clause(
        "Fees and exchange rates",
        "Where a fee applies it is shown before you confirm, separately from the exchange rate. " +
            "We do not mark up the rate to hide a fee inside it — the rate you see is the rate we " +
            "use for the conversion.",
    ),
    Clause(
        "Payments and requests",
        "A transfer is final once confirmed. A payment request is an invitation only: it never " +
            "moves money on its own, and nothing leaves your balance until you approve it.",
    ),
    Clause(
        "Your data",
        "This demo keeps one flag on your device — whether you have completed setup — and nothing " +
            "else. No balances, transactions, or credentials are stored, and none of it leaves " +
            "the device.",
    ),
    Clause(
        "Changes to these terms",
        "If these terms change in a way that affects you, we will tell you before the change takes " +
            "effect and give you the chance to close your account without penalty.",
    ),
)

/**
 * The Terms of Service, behind sign-up's "Read the Terms of Service" — previously a no-op.
 *
 * A real screen rather than a bottom sheet, unlike the app's other one-choice surfaces: this is a
 * document to read and scroll, not a decision to make, and it is reached mid-form so it needs a back
 * affordance that returns the user to their half-filled sign-up rather than dismissing over it.
 *
 * There is no "Accept" button here. Acceptance is the checkbox on the sign-up form, and a second
 * accept control would leave two sources of truth for one piece of consent — the user could accept
 * here and still find the sign-up CTA disabled, with nothing on screen explaining why.
 *
 * The clauses are written to match what the app actually does. The data clause describes
 * `SessionStore`'s single flag because that genuinely is everything the app persists.
 */
@Composable
fun TermsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowScreen(
        title = "Terms of Service",
        onBack = onBack,
        modifier = modifier,
    ) {
        Text(
            text = "Last updated 24 July 2026",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )

        Clauses.forEachIndexed { index, clause ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
            ) {
                Text(
                    // Numbered because the content genuinely is an ordered set of clauses people
                    // refer to by number — not as decoration.
                    text = "${index + 1}. ${clause.heading}",
                    style = FormaTheme.typography.material.titleMedium,
                )
                Text(
                    text = clause.body,
                    style = FormaTheme.typography.material.bodyMedium,
                    color = FormaTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = "Questions about any of this are answered under Help & support in your profile.",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Terms · light", heightDp = 1000)
@Composable
private fun TermsScreenPreview() {
    FinTechTheme {
        TermsScreen(onBack = {})
    }
}

@Preview(name = "Terms · dark", heightDp = 1000)
@Composable
private fun TermsScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        TermsScreen(onBack = {})
    }
}
