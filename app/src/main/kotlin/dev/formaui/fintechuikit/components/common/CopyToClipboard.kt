/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.components.common

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import kotlinx.coroutines.launch

/**
 * The one copy action, for the four screens that offer one.
 *
 * Writing to the clipboard is a suspend call on `LocalClipboard` — the platform may have to hand the
 * clip across a process boundary — so it needs a scope, and a scope is not something a screen whose
 * only job is layout should be holding. The launch lives here instead, and every call site keeps a
 * plain synchronous `onClick`.
 *
 * [label] is the clip's description, not the payload: Android 13+ shows it in the paste confirmation
 * it raises by itself, so it should name the thing being copied ("Statement"), never the app. That
 * system confirmation is also why no screen here adds a snackbar of its own — two acknowledgements
 * of one action read as a stutter.
 */
@Composable
fun rememberCopyToClipboard(label: String): (String) -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    return remember(clipboard, scope, label) {
        { text: String ->
            scope.launch {
                clipboard.setClipEntry(ClipData.newPlainText(label, text).toClipEntry())
            }
        }
    }
}
