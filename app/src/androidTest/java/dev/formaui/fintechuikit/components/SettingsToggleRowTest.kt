/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.formaui.fintechuikit.components.common.SettingsToggleRow
import dev.formaui.fintechuikit.setThemedContent
import org.junit.Rule
import org.junit.Test

/**
 * The row-level `toggleable` pattern, which CLAUDE.md requires of every settings row and which is
 * invisible to the eye — a row whose switch owns the interaction looks identical to one that does it
 * correctly, right up until someone taps the label and nothing happens.
 */
class SettingsToggleRowTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tappingAnywhereOnTheRowToggles() {
        var checked by mutableStateOf(false)
        compose.setThemedContent {
            SettingsToggleRow(
                icon = Icons.Filled.Fingerprint,
                title = "Biometric unlock",
                checked = checked,
                onCheckedChange = { checked = it },
            )
        }

        // Found by its label, not by the switch — that is the whole point of the pattern.
        compose.onNodeWithText("Biometric unlock").assertIsOff().performClick()
        compose.onNodeWithText("Biometric unlock").assertIsOn()
    }

    /**
     * Exactly one node in the row may be toggleable. The inner `FormaSwitch` takes a `null` callback
     * so it stays display-only; if it ever regains one, the row exposes two overlapping controls and
     * TalkBack announces the same setting twice.
     */
    @Test
    fun onlyTheRowIsToggleableNotTheSwitchInsideIt() {
        compose.setThemedContent {
            SettingsToggleRow(
                icon = Icons.Filled.Fingerprint,
                title = "Biometric unlock",
                checked = true,
                onCheckedChange = {},
            )
        }

        compose.onAllNodes(isToggleable(), useUnmergedTree = true).assertCountEquals(1)
    }
}
