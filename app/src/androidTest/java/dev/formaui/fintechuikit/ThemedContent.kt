package dev.formaui.fintechuikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Sets test content inside [FinTechTheme], exactly as the app root and every `@Preview` do.
 *
 * Not a convenience: several of the things under test only exist inside the theme. `FinTechTheme`
 * provides `LocalBrandColors`, `LocalBrandSpacing` and `LocalReducedMotion`, and a component that
 * reads `FinTechTheme.brandColors` outside it would take the static default rather than the scheme's
 * — so a test that skipped the wrapper could pass while the real screen rendered off-brand.
 *
 * [darkTheme] is a parameter for the same reason the previews come in pairs: this project has found
 * three separate colours that were legible in one scheme and invisible in the other.
 */
fun ComposeContentTestRule.setThemedContent(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    setContent {
        FinTechTheme(darkTheme = darkTheme) { content() }
    }
}
