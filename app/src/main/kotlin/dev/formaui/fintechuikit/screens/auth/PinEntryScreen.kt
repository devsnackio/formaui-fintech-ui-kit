@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.dialog.FormaAlertDialog
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.auth.AuthErrorBanner
import dev.formaui.fintechuikit.components.auth.AuthTextAction
import dev.formaui.fintechuikit.components.auth.PinDefaults
import dev.formaui.fintechuikit.components.auth.PinDots
import dev.formaui.fintechuikit.components.auth.PinKeypad
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * PIN unlock: dots for the entered digits, a keypad, and a shake when the PIN is wrong.
 *
 * The host owns the digits and the verdict, which makes the interaction contract worth stating
 * plainly:
 *
 *  - Every keypress arrives through [onPinChange]; this screen never accumulates digits itself,
 *    and it refuses input past [length] so the host is never handed an over-long PIN.
 *  - [onComplete] fires from a `LaunchedEffect` the moment [pin] reaches [length]. It has to be
 *    an effect, not a call during composition — verifying a PIN is a side effect, and running it
 *    while composing would re-fire on every unrelated recomposition.
 *  - On a wrong PIN the host sets [isError] **and** clears [pin]; on the next keypress it clears
 *    [isError] again. That false → true edge is what re-triggers [PinDots]' shake, so an error
 *    left latched turns the second wrong attempt into a silent one.
 *
 * The layout is not scrollable on purpose: a keypad that scrolls away from the thumb is worse
 * than one that is slightly cramped. The two weighted spacers absorb the height instead, so the
 * keypad stays pinned to the bottom on a tall screen and the header gives up its air first on a
 * short one.
 *
 * [onForgotPin] fires **after** the screen has confirmed with the user, not on the tap — the same
 * shape as `ProfileScreen`'s sign-out. The host just navigates; it does not need its own dialog.
 *
 * That bottom-pinned keypad is exactly why [safeDrawingPadding] is not optional here. The
 * pre-session graph hosts this with no `Scaffold` and no `innerPadding` under
 * `enableEdgeToEdge()`, so without the inset the bottom row of keys — and the "Forgot your PIN?"
 * action below it — would sit under the gesture bar.
 */
@Composable
fun PinEntryScreen(
    pin: String,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = PinDefaults.Length,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    onComplete: (String) -> Unit = {},
    onBiometric: (() -> Unit)? = null,
    onForgotPin: () -> Unit = {},
) {
    // Owned here, like ProfileScreen's sign-out confirmation: the only thing that opens it is the
    // link directly below, and the decision dies with the screen.
    var confirmingReset by remember { mutableStateOf(false) }

    LaunchedEffect(pin) {
        if (pin.length >= length) onComplete(pin)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FormaTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(FinTechTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl),
        ) {
            FormaAvatar(size = FormaAvatarSize.Large) {
                // The heading right below says what this is.
                Icon(Icons.Filled.Lock, contentDescription = null)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs),
            ) {
                Text(
                    text = "Enter your PIN",
                    style = FormaTheme.typography.material.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Use your $length-digit PIN to unlock.",
                    style = FormaTheme.typography.material.bodySmall,
                    color = FormaTheme.colorScheme.onSurfaceVariant,
                )
            }

            PinDots(filled = pin.length, length = length, isError = isError)

            if (errorMessage != null) {
                // No fillMaxWidth: under centred dots the banner should hug its text rather than
                // stretch a maroon bar across a screen that has no card to align with.
                AuthErrorBanner(errorMessage)
            }
        }

        Spacer(Modifier.weight(1f))

        PinKeypad(
            onDigit = { digit -> if (pin.length < length) onPinChange(pin + digit) },
            onBackspace = { if (pin.isNotEmpty()) onPinChange(pin.dropLast(1)) },
            enabled = enabled,
            onBiometric = onBiometric,
        )

        AuthTextAction(
            label = "Forgot your PIN?",
            onClick = { confirmingReset = true },
            modifier = Modifier.padding(top = FinTechTheme.spacing.sm),
        )
    }

    // Confirm before acting, because the action is destructive in the only way that matters here:
    // it abandons whatever the user was part-way through and sends them back to sign in. A link
    // that silently threw away the flow would be worse than one that did nothing.
    if (confirmingReset) {
        FormaAlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = "Forgot your PIN?",
            text = "Sign in with your password and you can set a new PIN. Your account and your " +
                "money are not affected.",
            confirmButton = {
                FormaButton(
                    onClick = {
                        confirmingReset = false
                        onForgotPin()
                    },
                    shape = BrandDefaults.buttonShape,
                    contentPadding = BrandDefaults.buttonContentPadding,
                ) { Text("Go to sign in") }
            },
            dismissButton = {
                FormaButton(
                    onClick = { confirmingReset = false },
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                ) { Text("Keep trying") }
            },
        )
    }
}

@Preview(name = "PinEntry · light", heightDp = 800)
@Composable
private fun PinEntryScreenPreview() {
    FinTechTheme {
        PinEntryScreen(pin = "123", onPinChange = {}, onBiometric = {})
    }
}

@Preview(name = "PinEntry · dark", heightDp = 800)
@Composable
private fun PinEntryScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        PinEntryScreen(pin = "123", onPinChange = {}, onBiometric = {})
    }
}

@Preview(name = "PinEntry · wrong PIN", heightDp = 800)
@Composable
private fun PinEntryScreenErrorPreview() {
    FinTechTheme {
        PinEntryScreen(
            pin = "",
            onPinChange = {},
            length = PinDefaults.MinLength,
            isError = true,
            errorMessage = "Incorrect PIN. 2 attempts left.",
        )
    }
}
