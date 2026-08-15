@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.components.iconbutton.FormaIconButtonVariant
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A dashboard quick action — a labelled circular icon button (Send, Add money, Convert).
 *
 * The primary action of the set takes the Filled variant so the CTA green appears exactly
 * once; the rest are Tonal.
 *
 * **The Column merges its semantics, and that is not cosmetic.** The tappable element is the inner
 * icon button, whose glyph is deliberately `contentDescription = null` because the visible label
 * below already names the action. Without the merge those are two unrelated nodes: a button with no
 * accessible name at all, and a caption that cannot be tapped — so TalkBack announces "button" and
 * stops. Merging makes the pair one control named by its own label, which is what it looks like.
 */
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
    ) {
        FormaIconButton(
            onClick = onClick,
            variant = if (primary) FormaIconButtonVariant.Filled else FormaIconButtonVariant.Tonal,
        ) {
            // The label beneath carries the meaning; the glyph is decorative.
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Text(label, style = FormaTheme.typography.material.labelMedium)
    }
}

@Preview
@Composable
private fun QuickActionButtonPreview() {
    FinTechTheme {
        Row(
            Modifier.padding(FinTechTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl),
        ) {
            QuickActionButton(Icons.Filled.SwapHoriz, "Send", onClick = {}, primary = true)
            QuickActionButton(Icons.Filled.Add, "Add money", onClick = {})
        }
    }
}
