@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.auth.AuthErrorBanner
import dev.formaui.fintechuikit.components.auth.AuthFormCard
import dev.formaui.fintechuikit.components.auth.AuthHeader
import dev.formaui.fintechuikit.components.auth.AuthTextAction
import dev.formaui.fintechuikit.components.auth.PasswordField
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The sign-in screen: email, password, and one green CTA.
 *
 * Two kinds of error live here and they are deliberately not the same thing. Field-level errors
 * are **derived** from the values — nothing is hoisted for them — and only fire once a field is
 * non-blank, so an untouched form never shows red. [errorMessage] is the other kind: the answer
 * from whatever tried to authenticate, which this screen cannot know and so takes as a
 * parameter.
 *
 * The CTA is enabled only when the form could plausibly succeed. That is a deliberate trade —
 * a disabled button explains itself poorly to a screen reader — but the alternative on an auth
 * form is round-tripping a request that is already known to fail, and the field-level error text
 * is what carries the explanation.
 *
 * Like the onboarding screens before it, this runs in the pre-session graph with no `Scaffold`
 * and no `innerPadding`, under `enableEdgeToEdge()`, so it paints its own background and insets
 * itself with [safeDrawingPadding]. Without that the card would sit under the status bar.
 */
@Composable
fun SignInScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    submitting: Boolean = false,
    onSubmit: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
) {
    val emailLooksWrong = email.isNotBlank() && !email.isValidEmail()
    val canSubmit = !submitting && email.isValidEmail() && password.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FormaTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = FinTechTheme.spacing.lg,
                vertical = FinTechTheme.spacing.xxl,
            ),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xl),
    ) {
        // No subtitle. "Sign in to continue to your account" restated the headline above it and
        // the button below it without adding anything the form doesn't already say.
        AuthHeader(title = "Welcome back")

        AuthFormCard {
            if (errorMessage != null) {
                AuthErrorBanner(errorMessage, Modifier.fillMaxWidth())
            }

            FormaTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !submitting,
                label = "Email",
                placeholder = "you@example.com",
                leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
                isError = emailLooksWrong,
                errorText = "That doesn't look like an email address",
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                visible = passwordVisible,
                onVisibleChange = onPasswordVisibleChange,
                enabled = !submitting,
                imeAction = ImeAction.Done,
                onImeAction = { if (canSubmit) onSubmit() },
            )

            AuthTextAction(
                label = "Forgot password?",
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End),
                enabled = !submitting,
            )

            FormaButton(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
            ) {
                // The busy state is a label change, not a spinner: FormaLoadingIndicator exposes
                // no colour parameter and M3 draws the circular indicator in colorScheme.primary,
                // which on the green CTA would be green on green.
                Text(if (submitting) "Signing in…" else "Sign in")
            }
        }

        // The gap is the row's, not the button's: AuthTextAction has no horizontal padding, so
        // Arrangement.Center actually centres the pair. With padding on the button the trailing
        // inset counted as content and the pair sat visibly left of centre.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                FinTechTheme.spacing.xs,
                Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "New here?",
                style = FormaTheme.typography.material.bodySmall,
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
            AuthTextAction(
                label = "Create an account",
                onClick = onCreateAccount,
                enabled = !submitting,
            )
        }
    }
}

@Preview(name = "SignIn · light", heightDp = 720)
@Composable
private fun SignInScreenPreview() {
    FinTechTheme {
        SignInScreen(
            email = "alex.morgan@example.com",
            onEmailChange = {},
            password = "hunter2000",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
        )
    }
}

@Preview(name = "SignIn · dark", heightDp = 720)
@Composable
private fun SignInScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        SignInScreen(
            email = "alex.morgan@example.com",
            onEmailChange = {},
            password = "hunter2000",
            onPasswordChange = {},
            passwordVisible = true,
            onPasswordVisibleChange = {},
        )
    }
}

@Preview(name = "SignIn · rejected", heightDp = 720)
@Composable
private fun SignInScreenErrorPreview() {
    FinTechTheme {
        SignInScreen(
            email = "alex.morgan@example",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
            errorMessage = "That email and password don't match an account.",
        )
    }
}
