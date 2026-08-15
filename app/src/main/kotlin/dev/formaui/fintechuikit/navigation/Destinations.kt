package dev.formaui.fintechuikit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * The app's destinations, as type-safe navigation-compose routes.
 *
 * Each is a `@Serializable` object rather than a string route, so navigating to one is a
 * compile-checked reference and a typo cannot silently produce a dead link.
 */
sealed interface Destination {
    @Serializable data object Dashboard : Destination
    @Serializable data object Transactions : Destination
    @Serializable data object Card : Destination
    @Serializable data object Profile : Destination

    // Reached from within a screen, not the bottom bar, so these carry no tab metadata. Anything
    // that is not one of the four tabs above renders without the bottom bar and supplies its own
    // background and window insets — see FinTechNavHost.
    @Serializable data object Transfer : Destination
    @Serializable data object TransferRecipient : Destination
    @Serializable data object AddMoney : Destination
    @Serializable data object Receive : Destination
    @Serializable data object RequestMoney : Destination

    /** The shared completion screen; what it says is read from hoisted state, not from a route arg. */
    @Serializable data object Success : Destination

    // Reached from a control that used to do nothing: the balance card's actions slot, Card's two
    // controls, and Profile's Security / Help rows. Language is a sheet, not a destination.
    @Serializable data object Accounts : Destination
    @Serializable data object CardDetails : Destination
    @Serializable data object ChangePin : Destination
    @Serializable data object Security : Destination
    @Serializable data object Help : Destination
    @Serializable data object Statements : Destination
    @Serializable data object Notifications : Destination

    /**
     * The only route in the app that carries an argument.
     *
     * The id — not the `Transaction` — is the route, so the back stack holds a value that stays
     * meaningful after the activity list changes underneath it, and no custom `NavType` is needed.
     * The screen looks the id up and pops if it has gone.
     */
    @Serializable data class TransactionDetail(val transactionId: String) : Destination

    /**
     * The pre-session flow — welcome, onboarding, sign-in — as a nested graph.
     *
     * Grouping these under one graph is what lets the shell ask a single question
     * (`hasRoute<PreSession>()`) to decide whether to show app chrome, instead of testing every
     * screen in the flow and having to remember to extend that test each time the flow gains a
     * step. `NavDestination.hierarchy` walks parents, so the check matches any descendant.
     */
    @Serializable data object PreSession : Destination

    @Serializable data object Welcome : Destination
    @Serializable data object Onboarding : Destination
    @Serializable data object SignIn : Destination
    @Serializable data object SignUp : Destination
    @Serializable data object PinEntry : Destination
    @Serializable data object ForgotPassword : Destination
    @Serializable data object Terms : Destination
}

/**
 * The four bottom-bar tabs, in order.
 *
 * Transfer is intentionally absent: it opens from the Dashboard CTA, keeping the bar to the
 * four primary destinations rather than letting it collect every screen.
 */
enum class TopLevelTab(
    val destination: Destination,
    val label: String,
    val icon: ImageVector,
) {
    Home(Destination.Dashboard, "Home", Icons.Filled.Home),
    Activity(Destination.Transactions, "Activity", Icons.AutoMirrored.Filled.ReceiptLong),
    Card(Destination.Card, "Card", Icons.Filled.CreditCard),
    Profile(Destination.Profile, "Profile", Icons.Filled.Person),
}
