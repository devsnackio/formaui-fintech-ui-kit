@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import dev.formaui.components.navigation.FormaNavigationBar
import dev.formaui.components.navigation.FormaNavigationBarItem
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.BrandTokens

/**
 * The bottom navigation bar.
 *
 * Selection is derived from the live back stack rather than a remembered index, so the
 * highlighted tab always matches the destination actually on screen — including after a
 * system-back this component never hears about directly.
 *
 * **Selected-state colours.** M3's default `NavigationBarItemColors` map to three colour-scheme
 * roles that are all wrong in the brand palette (verified against the material3 1.4.0
 * `NavigationBarToken`s):
 *
 *  - indicator pill  ← `secondaryContainer` (brand: sage / ink — invisible against the bar)
 *  - selected icon   ← `onSecondaryContainer`
 *  - **selected label ← `secondary`** (NOT `onSurface`; brand `secondary` is `CanvasSoft` in
 *    light and a lifted ink in dark — both near-invisible on the bar, which is why the selected
 *    tab's text could not be read)
 *
 * So we pass the three brand values explicitly: a pale-green pill (`PrimaryNeutral`, DESIGN.md's
 * "neutral active fill"), a deep-green icon on it, and the label on `onSurface` so it is
 * maximum-contrast in BOTH modes (ink on the white bar / canvas-soft on the ink bar). A selection
 * indicator is not a CTA, so the green pill does not break the "green is the CTA colour" rule.
 *
 * Unselected and disabled states are left to the M3 defaults, which already resolve to
 * brand-correct `onSurfaceVariant`.
 *
 * The `colors` parameter arrived in FormaUI `0.1.0-beta04`; before that the only lever was
 * re-providing an overridden scheme through a nested `FormaTheme`, which also forced a single
 * `darkTheme` value across both modes. Don't reintroduce that — set the colours here.
 */
@Composable
fun FinTechBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (TopLevelTab) -> Unit,
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = BrandTokens.PrimaryNeutral,
        selectedIconColor = BrandTokens.InkDeep,
        selectedTextColor = FormaTheme.colorScheme.onSurface,
    )
    FormaNavigationBar {
        TopLevelTab.entries.forEach { tab ->
            FormaNavigationBarItem(
                selected = currentDestination.isOn(tab),
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = tab.label,
                colors = itemColors,
            )
        }
    }
}

/**
 * True when this destination — or any parent in its hierarchy — is the tab's route.
 *
 * Walking the hierarchy (not just the leaf) keeps the right tab lit when a tab hosts a nested
 * graph, so a detail screen inside a tab still shows that tab as selected.
 */
internal fun NavDestination?.isOn(tab: TopLevelTab): Boolean {
    val dest = this ?: return false
    return dest.hierarchy.any { node ->
        when (tab.destination) {
            Destination.Dashboard -> node.hasRoute<Destination.Dashboard>()
            Destination.Transactions -> node.hasRoute<Destination.Transactions>()
            Destination.Card -> node.hasRoute<Destination.Card>()
            Destination.Profile -> node.hasRoute<Destination.Profile>()
            else -> false
        }
    }
}
