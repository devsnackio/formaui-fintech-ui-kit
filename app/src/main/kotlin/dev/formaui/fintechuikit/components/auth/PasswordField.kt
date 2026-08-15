@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.components.textfield.FormaTextField
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * A password entry field with a show/hide toggle.
 *
 * [visible] is hoisted alongside [value] rather than kept inside. Reveal is not a private
 * rendering detail — a host that navigates away, times out, or submits the form wants the field
 * masked again on return, and it cannot ask for that if the component owns the flag.
 *
 * The toggle's `contentDescription` names the *action*, not the state ("Show password" while
 * masked), because that is what a screen-reader user is choosing to do; the M3 field already
 * announces the field itself.
 *
 * No `shape` is passed — FormaUI's text-field default resolves to `shapes.md` 12dp, which is
 * exactly the brand's `text-input` radius, so this is the one Forma component family that needs
 * no `BrandDefaults` correction.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    enabled: Boolean = true,
    isError: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    FormaTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = label,
        leadingIcon = {
            // The label already says "Password"; the padlock is decoration.
            Icon(Icons.Filled.Lock, contentDescription = null)
        },
        trailingIcon = {
            FormaIconButton(onClick = { onVisibleChange(!visible) }, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                )
            }
        },
        isError = isError,
        helperText = helperText,
        errorText = errorText,
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        // Both IME actions route to one callback: whether "next" means focus the next field or
        // submit is the form's decision, not this field's.
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() },
        ),
    )
}

@Preview(name = "PasswordField · light")
@Composable
private fun PasswordFieldPreview() {
    FinTechTheme {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            PasswordField(value = "hunter2000", onValueChange = {}, visible = false, onVisibleChange = {})
            PasswordField(value = "hunter2000", onValueChange = {}, visible = true, onVisibleChange = {})
            PasswordField(
                value = "short",
                onValueChange = {},
                visible = false,
                onVisibleChange = {},
                isError = true,
                errorText = "Use at least 8 characters",
            )
        }
    }
}

@Preview(name = "PasswordField · dark")
@Composable
private fun PasswordFieldDarkPreview() {
    FinTechTheme(darkTheme = true) {
        Column(
            Modifier.padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            PasswordField(value = "hunter2000", onValueChange = {}, visible = false, onVisibleChange = {})
            PasswordField(value = "hunter2000", onValueChange = {}, visible = true, onVisibleChange = {})
            PasswordField(
                value = "short",
                onValueChange = {},
                visible = false,
                onVisibleChange = {},
                isError = true,
                errorText = "Use at least 8 characters",
            )
        }
    }
}
