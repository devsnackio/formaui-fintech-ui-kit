package dev.formaui.fintechuikit.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import dev.formaui.fintechuikit.data.model.SpendCategory

/**
 * Maps a [SpendCategory] to its icon.
 *
 * This lives in the UI layer rather than on the enum itself so the data models stay free of
 * Compose types — they can then be unit-tested and reused on any front end.
 */
val SpendCategory.icon: ImageVector
    get() = when (this) {
        SpendCategory.Groceries -> Icons.Filled.LocalGroceryStore
        SpendCategory.Dining -> Icons.Filled.Restaurant
        SpendCategory.Transport -> Icons.Filled.DirectionsBus
        SpendCategory.Housing -> Icons.Filled.Home
        SpendCategory.Shopping -> Icons.Filled.ShoppingBag
        SpendCategory.Utilities -> Icons.Filled.Bolt
        SpendCategory.Entertainment -> Icons.Filled.Movie
        SpendCategory.Income -> Icons.Filled.Payments
        SpendCategory.Transfer -> Icons.Filled.SwapHoriz
    }
