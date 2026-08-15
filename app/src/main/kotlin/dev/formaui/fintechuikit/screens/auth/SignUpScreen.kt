/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.button.FormaButton
import dev.formaui.components.checkbox.FormaCheckbox
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
 * Account creation: name, email, password, and a terms checkbox that gates the CTA.
 *
 * Same two-tier error model as [SignInScreen] — field errors derived from the values,
 * [errorMessage] hoisted for whatever the backend says — with one addition: the password's
 * length rule is shown as helper text *before* it is shown as an error, so the requirement
 * arrives while the user is still choosing rather than after they have been rejected.
 *
 * Paints its own background and applies [safeDrawingPadding] for the same reason [SignInScreen]
 * does: the pre-session graph hosts these screens with no `Scaffold` and no `innerPadding`.
 */
@Composable
fun SignUpScreen(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    termsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    submitting: Boolean = false,
    onSubmit: () -> Unit = {},
    onViewTerms: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    val emailLooksWrong = email.isNotBlank() && !email.isValidEmail()
    val passwordTooShort = password.isNotBlank() && !password.isLongEnoughPassword()
    val canSubmit = !submitting &&
        name.isNotBlank() &&
        email.isValidEmail() &&
        password.isLongEnoughPassword() &&
        termsAccepted

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
        // The subtitle stays here — unlike sign-in's, it does a job: it sets the expectation that
        // this is short, which is the objection at this step.
        AuthHeader(
            title = "Create your account",
            subtitle = "It takes about two minutes.",
        )

        AuthFormCard {
            if (errorMessage != null) {
                AuthErrorBanner(errorMessage, Modifier.fillMaxWidth())
            }

            FormaTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !submitting,
                label = "Full name",
                placeholder = "Alex Morgan",
                leadingIcon = { Icon(Icons.Filled.PersonOutline, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )

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
                isError = passwordTooShort,
                helperText = "At least $MinPasswordLength characters",
                errorText = "Use at least $MinPasswordLength characters",
                imeAction = ImeAction.Done,
                onImeAction = { if (canSubmit) onSubmit() },
            )

            // Row-level toggleable with the checkbox passed onCheckedChange = null, so the label
            // and the box are one ~48dp target announced once — FormaUI's documented pattern.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = termsAccepted,
                        enabled = !submitting,
                        role = Role.Checkbox,
                        onValueChange = onTermsAcceptedChange,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
            ) {
                FormaCheckbox(
                    checked = termsAccepted,
                    onCheckedChange = null,
                    enabled = !submitting,
                )
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy.",
                    style = FormaTheme.typography.material.bodySmall,
                )
            }

            // A sibling of the toggleable row, not a link inside it: a clickable nested in a
            // toggleable gives accessibility services two overlapping targets on one line, and
            // agreeing to terms you have no way to open is not a consent flow worth shipping.
            AuthTextAction(
                label = "Read the Terms of Service",
                onClick = onViewTerms,
                enabled = !submitting,
            )

            FormaButton(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
                shape = BrandDefaults.buttonShape,
                contentPadding = BrandDefaults.buttonContentPadding,
            ) {
                Text(if (submitting) "Creating account…" else "Create account")
            }
        }

        // See SignInScreen: the row owns the gap so Arrangement actually centres the pair.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                FinTechTheme.spacing.xs,
                Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Already have an account?",
                style = FormaTheme.typography.material.bodySmall,
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
            AuthTextAction(label = "Sign in", onClick = onSignIn, enabled = !submitting)
        }
    }
}

@Preview(name = "SignUp · light", heightDp = 900)
@Composable
private fun SignUpScreenPreview() {
    FinTechTheme {
        SignUpScreen(
            name = "Alex Morgan",
            onNameChange = {},
            email = "alex.morgan@example.com",
            onEmailChange = {},
            password = "hunter2000",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
            termsAccepted = true,
            onTermsAcceptedChange = {},
        )
    }
}

@Preview(name = "SignUp · dark", heightDp = 900)
@Composable
private fun SignUpScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        SignUpScreen(
            name = "Alex Morgan",
            onNameChange = {},
            email = "alex.morgan@example.com",
            onEmailChange = {},
            password = "hunter2000",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
            termsAccepted = true,
            onTermsAcceptedChange = {},
        )
    }
}

@Preview(name = "SignUp · incomplete", heightDp = 900)
@Composable
private fun SignUpScreenIncompletePreview() {
    FinTechTheme {
        SignUpScreen(
            name = "Alex Morgan",
            onNameChange = {},
            email = "alex.morgan@example",
            onEmailChange = {},
            password = "short",
            onPasswordChange = {},
            passwordVisible = false,
            onPasswordVisibleChange = {},
            termsAccepted = false,
            onTermsAcceptedChange = {},
            errorMessage = "An account with that email already exists.",
        )
    }
}
