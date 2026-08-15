// ExperimentalMaterial3Api is required by FormaTopAppBar's call site: its scrollBehavior parameter
// is typed TopAppBarScrollBehavior, still experimental in M3, so the opt-in is needed even though
// this shell passes none. Documented on the TopAppBar entry in formaui-reference.md.
@file:OptIn(ExperimentalFormaUiApi::class, ExperimentalMaterial3Api::class)

package dev.formaui.fintechuikit.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.formaui.components.topappbar.FormaTopAppBar
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.BrandMotion
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/**
 * The app shell: a [Scaffold] with the bottom bar, hosting the [NavHost].
 *
 * Tab re-selection uses the standard bottom-nav back-stack recipe — pop to the graph's start,
 * `launchSingleTop`, and `restoreState` — so tapping a tab never stacks duplicate copies and a
 * tab remembers where it was when you last left it.
 *
 * The per-destination content is injected via [builder] so this shell has no dependency on the
 * screen implementations; `MainActivity` supplies them.
 *
 * [startDestination] is a parameter rather than a constant because where the app opens is a
 * product decision, not a property of the shell — `MainActivity` makes that call.
 *
 * [topBarActions] is a slot for the same reason [builder] is: the actions need the state and the
 * navigator that `MainActivity` owns, and the shell should not know that Activity has a search field
 * or that Profile has an appearance sheet. It is handed the current [TopLevelTab] so one lambda can
 * serve all four tabs with a `when`.
 */
@Composable
fun FinTechNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Destination.Dashboard,
    topBarActions: @Composable RowScope.(TopLevelTab) -> Unit = {},
    builder: NavGraphBuilder.(NavHostController) -> Unit,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentEntry?.destination

    // The tab we are on, or null everywhere else. One lookup drives all three decisions below —
    // the title, the bottom bar, and the insets — so they cannot disagree about where we are.
    //
    // The test is deliberately "which tab is this?" rather than "is this one of the chromeless
    // ones?", so a newly added destination gets no app chrome by default. That is the safe
    // direction to fail: forgetting to list a screen gives you a focused screen, not a bottom bar
    // offering four tabs from halfway through a transfer. It also fixes the older behaviour where
    // Transfer kept the bar on screen with no tab highlighted, because it matched none of them.
    //
    // Reuses [isOn] so tab-route matching lives in exactly one place.
    val currentTab = TopLevelTab.entries.firstOrNull { tab -> currentDestination.isOn(tab) }

    val reduced = LocalReducedMotion.current

    Scaffold(
        modifier = modifier,
        topBar = {
            // Titled from the tab's own label, so the bar always names the tab the bottom bar has
            // highlighted. Hosting it here rather than in each screen keeps the four consistent by
            // construction and keeps the bar stable across a tab switch, exactly like the bottom
            // bar — a per-screen bar would be torn down and rebuilt on every tap.
            if (currentTab != null) {
                FormaTopAppBar(
                    title = currentTab.label,
                    actions = { topBarActions(currentTab) },
                    // Transparent, and it stays that way — no scrollBehavior, so the bar looks
                    // identical whether or not the content below it has moved. Safe only because
                    // content stops at the bar rather than passing behind it (see the NavHost below).
                    // Shared with FlowScreen; see BrandDefaults for the full reasoning.
                    colors = BrandDefaults.topAppBarColors,
                )
            }
        },
        bottomBar = {
            if (currentTab != null) {
                FinTechBottomBar(
                    currentDestination = currentDestination,
                    onTabSelected = { tab -> navController.navigateToTab(tab) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // Motion is decided from the *pair* of destinations, not per route — see [screenMotion].
            // `reduced` is captured rather than read inside: these lambdas are not @Composable and
            // cannot touch a CompositionLocal.
            enterTransition = { screenMotion(initialState, targetState, reduced).enter },
            exitTransition = { screenMotion(initialState, targetState, reduced).exit },
            // Pop mirrors the forward motion of the leg being undone, which is why both pop lambdas
            // classify with the states swapped: on a pop, `targetState` is where we came from.
            popEnterTransition = { screenMotion(targetState, initialState, reduced).popEnter },
            popExitTransition = { screenMotion(targetState, initialState, reduced).popExit },
            // Tabs are inset by the bars, so their content stops at the app bar rather than running
            // behind it — which is what keeps the transparent bar readable without needing a
            // scroll-driven colour change. Every other destination owns the full window and applies
            // its own safeDrawingPadding (see FlowScreen); handing it innerPadding would inset it
            // twice.
            modifier = if (currentTab != null) Modifier.padding(innerPadding) else Modifier,
        ) {
            builder(navController)
        }
    }
}

/** The four transitions one navigation leg needs, forward and reversed. */
private data class ScreenMotion(
    val enter: EnterTransition,
    val exit: ExitTransition,
    val popEnter: EnterTransition,
    val popExit: ExitTransition,
)

/**
 * Picks the motion for a navigation leg from what the two destinations *are*.
 *
 * Deciding here rather than per destination is what keeps 18 registrations consistent: a new screen
 * inherits the right motion from its kind, and there is no per-route argument to forget. It is also
 * the only option available — [screen] discards the `AnimatedContentScope` that `composable<T>`
 * provides, so no screen body can supply its own transition.
 *
 * The rules encode the *relationship* between the screens:
 *
 *  - **Tab → tab** fades and scales slightly, with no left/right component. The four tabs are
 *    siblings; sliding between them would imply an order they do not have.
 *  - **→ transaction detail** rises from the bottom. It is an inspection of something on the screen
 *    you were just looking at, not the next step of anything.
 *  - **→ success** fades and scales up gently. It is an arrival, and sliding it in would make it feel
 *    like one more step in the flow rather than the end of it.
 *  - **Everything else** slides along X — the flows, and the pre-session graph, where each screen
 *    genuinely is the next one.
 *
 * **This is the only screen-level motion in the app, and screens must not add their own.** Every leg
 * above carries a `fadeIn`, and a screen that also fades its own content in does not reinforce that —
 * nested `graphicsLayer` alphas *multiply*, so two 260ms ramps put the content at 0.25 alpha halfway
 * through instead of 0.5. The screen hangs faint and then snaps in, which reads as lag at a perfect
 * frame rate. Staggering made it worse again: a delayed element skips drawing until its delay elapses
 * and then runs its own full duration, landing *after* the host transition has already settled.
 *
 * Every screen used to do this, and the transaction detail route showed it worst because it is the
 * one leg that also travels. The fix was to delete the per-screen entrances outright, which is why
 * `BrandMotion` no longer offers one.
 *
 * A screen may still animate something of its own when it is not a fade over the whole body —
 * `SuccessScreen`'s badge spring is a scale, so it composes with the host's scale rather than
 * fighting its alpha, and `HelpScreen`'s FAQ reveal fires long after any transition has finished.
 */
private fun screenMotion(
    from: NavBackStackEntry,
    to: NavBackStackEntry,
    reduced: Boolean,
): ScreenMotion {
    if (reduced) {
        return ScreenMotion(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            popEnter = EnterTransition.None,
            popExit = ExitTransition.None,
        )
    }

    val enterSpec = tween<Float>(BrandMotion.EnterMillis, easing = BrandMotion.EnterEasing)
    val exitSpec = tween<Float>(BrandMotion.ExitMillis, easing = BrandMotion.ExitEasing)
    val enterOffsetSpec = tween<IntOffset>(BrandMotion.EnterMillis, easing = BrandMotion.EnterEasing)
    val exitOffsetSpec = tween<IntOffset>(BrandMotion.ExitMillis, easing = BrandMotion.ExitEasing)

    val betweenTabs = from.destination.isAnyTab && to.destination.isAnyTab
    val toDetail = to.destination.hasRoute<Destination.TransactionDetail>()
    val toSuccess = to.destination.hasRoute<Destination.Success>()

    return when {
        betweenTabs -> ScreenMotion(
            // Fade-through: the outgoing screen leaves before the incoming one arrives, and the
            // slight scale gives the swap somewhere to go without a direction.
            enter = fadeIn(enterSpec) + scaleIn(enterSpec, initialScale = 0.92f),
            exit = fadeOut(exitSpec) + scaleOut(exitSpec, targetScale = 1.04f),
            popEnter = fadeIn(enterSpec) + scaleIn(enterSpec, initialScale = 0.92f),
            popExit = fadeOut(exitSpec) + scaleOut(exitSpec, targetScale = 1.04f),
        )

        toDetail -> ScreenMotion(
            enter = slideInVertically(enterOffsetSpec) { height -> height / 6 } + fadeIn(enterSpec),
            // The screen behind only fades; sliding it too would read as two things moving apart.
            exit = fadeOut(exitSpec),
            popEnter = fadeIn(enterSpec),
            popExit = slideOutVertically(exitOffsetSpec) { height -> height / 6 } + fadeOut(exitSpec),
        )

        toSuccess -> ScreenMotion(
            enter = fadeIn(enterSpec) + scaleIn(enterSpec, initialScale = 0.94f),
            exit = fadeOut(exitSpec),
            popEnter = fadeIn(enterSpec),
            popExit = fadeOut(exitSpec) + scaleOut(exitSpec, targetScale = 0.94f),
        )

        // Forward along a flow. Fractional offsets rather than full-width: the incoming screen
        // covers the last stretch by fading, which is cheaper to read than a full slide.
        else -> ScreenMotion(
            enter = slideInHorizontally(enterOffsetSpec) { width -> width / 4 } + fadeIn(enterSpec),
            exit = slideOutHorizontally(exitOffsetSpec) { width -> -width / 6 } + fadeOut(exitSpec),
            popEnter = slideInHorizontally(enterOffsetSpec) { width -> -width / 6 } + fadeIn(enterSpec),
            popExit = slideOutHorizontally(exitOffsetSpec) { width -> width / 4 } + fadeOut(exitSpec),
        )
    }
}

/** True for any of the four bottom-bar destinations. Reuses [isOn] so route matching lives once. */
private val NavDestination.isAnyTab: Boolean
    get() = TopLevelTab.entries.any { tab -> isOn(tab) }

/**
 * The idempotent tab-switch: single copy on top, tab home popped to, state preserved.
 *
 * This is the ONLY correct way to reach a top-level tab destination. Reaching one with a plain
 * `navigate()` (e.g. a "See all" link) mixes an un-saved back-stack entry into the
 * `saveState`/`restoreState` bookkeeping the tabs rely on, after which the bottom bar's own
 * navigations silently no-op — so any in-app link to a tab must go through here too.
 *
 * The pop target is [Destination.Dashboard] by name, NOT `graph.startDestinationId`. The usual
 * recipe uses the graph's start destination because it is normally also the tab home, but this
 * graph starts at [Destination.PreSession]. Popping to the graph start would either strand
 * Welcome under every tab, or — once [enterSession] has dropped the pre-session graph off the
 * stack — find no matching entry and pop nothing, letting the back stack grow a new entry on
 * every tab tap.
 *
 * **It is for tab-to-tab travel only.** Called from a screen stacked *above* a tab it can undo
 * itself — see [leaveFlow], which is what such a screen should call instead.
 */
internal fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.destination) {
        popUpTo(Destination.Dashboard) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Dismisses a finished flow, landing on the Dashboard underneath it.
 *
 * A pop, not a [navigateToTab] call, and the difference is load-bearing rather than stylistic.
 * `navigateToTab` pairs `popUpTo(Dashboard) { saveState = true }` with `restoreState = true`, which
 * is exactly right between tabs and self-cancelling from a screen sitting above one: the pop files
 * the screen you are leaving under the Dashboard's key in the controller's saved-state map, and
 * `restoreState` — evaluated immediately afterwards, in the same `navigate` call — finds that key
 * populated and puts the screen straight back. The success screen's `Done` did that and read as a
 * button wired to nothing, because the destination it popped and the destination it navigated to
 * were the same one.
 *
 * Popping also earns the leg its reverse motion — the success screen scales and fades back out the
 * way it arrived — and makes `Done` and system-back agree, which is the least surprising pair of
 * behaviours a terminal screen can have.
 *
 * Safe because every flow reaches its completion through a `popUpTo(Dashboard)` navigation, so the
 * Dashboard is the entry immediately below.
 */
internal fun NavHostController.leaveFlow() {
    popBackStack(Destination.Dashboard, inclusive = false)
}

/**
 * Leaves the pre-session flow for the app proper.
 *
 * `inclusive = true` drops the entire welcome / onboarding / sign-in graph off the back stack, so
 * system-back from the Dashboard exits the app rather than falling back into sign-in — a signed-in
 * user should not be able to reverse into the sign-in screen. It also leaves the Dashboard as the
 * bottom entry, which is what [navigateToTab] pops to.
 */
internal fun NavHostController.enterSession() {
    navigate(Destination.Dashboard) {
        popUpTo(Destination.PreSession) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Returns to the pre-session flow — the sign-out path, and the mirror of [enterSession].
 *
 * Navigating to [Destination.PreSession] resolves to that graph's own start destination, so the
 * user lands on Welcome rather than on whichever auth screen they last saw.
 *
 * The pop target is the Dashboard, `inclusive`, because the Dashboard is the session's bottom
 * back-stack entry — after [enterSession] by construction, and on a returning launch because it is
 * then the graph's start destination. Clearing it too means back from Welcome exits the app instead
 * of returning to the Dashboard of an account that was just signed out of. `saveState` is
 * deliberately absent: restoring a signed-out user's tab state is exactly what should not happen.
 */
internal fun NavHostController.leaveSession() {
    navigate(Destination.PreSession) {
        popUpTo(Destination.Dashboard) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Registers a top-level tab destination.
 *
 * A thin wrapper over [composable] so `MainActivity` reads as a list of `tab { … }` /
 * `screen { … }` entries rather than repeating the reified-route boilerplate.
 */
inline fun <reified T : Any> NavGraphBuilder.screen(
    noinline content: @Composable () -> Unit,
) = composable<T> { content() }
