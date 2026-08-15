package dev.formaui.fintechuikit.screens.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import dev.formaui.fintechuikit.components.onboarding.OnboardingAccent

/**
 * One intro slide.
 *
 * Unlike the models in `data/`, this one holds Compose types ([ImageVector]) — it is screen copy
 * rather than domain data, so it stays here instead of widening `data/`, which is deliberately
 * Compose-free so it can be unit-tested.
 */
data class OnboardingPage(
    val icon: ImageVector,
    val accent: OnboardingAccent,
    val title: String,
    val body: String,
)

/**
 * The demo intro deck.
 *
 * Four slides is the practical ceiling: the page indicator stops reading as a set of positions
 * past five, and each extra slide is one more tap between the user and the product.
 *
 * Accents cycle rather than repeat consecutively so consecutive slides never look like the same
 * illustration twice; green bookends the deck because it is the brand's own colour and the
 * cyan/orange pair are the sanctioned illustration accents in between.
 */
val OnboardingPages: List<OnboardingPage> = listOf(
    OnboardingPage(
        icon = Icons.Filled.SwapHoriz,
        accent = OnboardingAccent.Primary,
        title = "Money that moves like you do",
        body = "Send to 80 countries at the real exchange rate, with the fee shown up front.",
    ),
    OnboardingPage(
        icon = Icons.Filled.Language,
        accent = OnboardingAccent.Cyan,
        title = "Hold 40+ currencies",
        body = "Keep balances in the currencies you use and convert the moment the rate suits.",
    ),
    OnboardingPage(
        icon = Icons.Filled.CreditCard,
        accent = OnboardingAccent.Orange,
        title = "Spend anywhere, freeze anytime",
        body = "One card for every balance — and a switch that stops it dead if it goes missing.",
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        accent = OnboardingAccent.Primary,
        title = "See exactly where it goes",
        body = "Every payment is categorised as it lands, so the month adds up without the guesswork.",
    ),
)
