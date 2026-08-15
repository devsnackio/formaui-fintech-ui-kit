@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.avatar.FormaAvatar
import dev.formaui.components.avatar.FormaAvatarSize
import dev.formaui.components.button.FormaButton
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.auth.AuthFormCard
import dev.formaui.fintechuikit.components.auth.AuthHeader
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * Password reset, behind sign-in's "Forgot password?" — which until now did nothing.
 *
 * **The screen owns its own confirmation rather than routing to `SuccessScreen`.** That screen's
 * `onDone` goes to the Dashboard, which is exactly wrong here: this runs *before* sign-in, and
 * finishing a password reset must not drop an unauthenticated user into the app. Keeping both states
 * here means the only way onward is back to sign-in, which is the honest outcome.
 *
 * [sent] is hoisted rather than owned, so the host can reset it when the flow is re-entered — a
 * screen that reopens already showing "check your inbox" would be stale.
 *
 * The demo never checks whether the address belongs to an account, and the copy is written so that is
 * not a lie: it says a link is on its way *if* the address is registered. Confirming or denying that
 * an email has an account is an account-enumeration leak, so the vague wording is the correct
 * behaviour, not a shortcut.
 */
@Composable
fun ForgotPasswordScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    sent: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emailLooksWrong = email.isNotBlank() && !email.isValidEmail()

    FlowScreen(
        title = "Reset password",
        onBack = onBack,
        modifier = modifier,
        footer = {
            FormaButton(
                onClick = if (sent) onBack else onSubmit,
                enabled = sent || email.isValidEmail(),
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (sent) "Back to sign in" else "Send reset link")
            }
        },
    ) {
        if (sent) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
            ) {
                FormaAvatar(
                    size = FormaAvatarSize.Large,
                    containerColor = FinTechTheme.brandColors.primaryPale,
                    contentColor = FinTechTheme.brandColors.positiveDeep,
                ) {
                    Icon(Icons.Filled.MarkEmailRead, contentDescription = null)
                }
                Text(
                    "Check your inbox",
                    style = FormaTheme.typography.material.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "If $email is registered, a reset link is on its way. It expires in " +
                        "30 minutes.",
                    style = FormaTheme.typography.material.bodyLarge,
                    color = FormaTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            AuthHeader(
                title = "Forgot your password?",
                subtitle = "Give us the email on your account and we'll send a reset link.",
            )

            AuthFormCard {
                FormaTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Email",
                    placeholder = "you@example.com",
                    leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
                    isError = emailLooksWrong,
                    errorText = "That doesn't look like an email address",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Forgot password · form", heightDp = 720)
@Composable
private fun ForgotPasswordFormPreview() {
    FinTechTheme {
        ForgotPasswordScreen(
            email = "alex.morgan@example.com",
            onEmailChange = {},
            sent = false,
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(name = "Forgot password · sent", heightDp = 720)
@Composable
private fun ForgotPasswordSentPreview() {
    FinTechTheme {
        ForgotPasswordScreen(
            email = "alex.morgan@example.com",
            onEmailChange = {},
            sent = true,
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(name = "Forgot password · dark", heightDp = 720)
@Composable
private fun ForgotPasswordDarkPreview() {
    FinTechTheme(darkTheme = true) {
        ForgotPasswordScreen(
            email = "alex.morgan@example",
            onEmailChange = {},
            sent = false,
            onSubmit = {},
            onBack = {},
        )
    }
}
