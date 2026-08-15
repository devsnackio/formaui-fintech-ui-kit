/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit

import android.content.Context
import androidx.core.content.edit

/**
 * Remembers that the user has already been through the pre-session flow.
 *
 * This is the app's only persisted state. Everything else the demo holds lives in `remember` and
 * is meant to die with the process — but "show onboarding and sign-in once per install" is a claim
 * about installs, so it has to outlive both the composition and the process.
 *
 * **Why `SharedPreferences` and not DataStore.** DataStore is the current recommendation and would
 * be the right call if this app already had coroutine-backed persistence — it does not; this is the
 * first byte it stores. The deciding factor is *when* the value is needed: the navigation graph's
 * start destination must be known at the moment the `NavHost` is composed. DataStore hands back a
 * `Flow`, so the first frame would have no answer yet, and the app would either compose the
 * pre-session graph and then jump to the Dashboard a frame later, or need a third "still loading"
 * state in the shell. A single-boolean `SharedPreferences` read is synchronous and answers in time.
 * That read touches disk on the main thread; for one boolean during `onCreate` that is the standard
 * trade, and it is the reason this class exposes a plain property rather than a suspend function.
 *
 * "Per install" comes for free: uninstalling clears the app's data directory. Note that Android's
 * auto-backup can restore preferences onto a reinstall on the same account, so a returning user may
 * skip the flow after reinstalling. Excluding this file from backup would close that gap; it is not
 * worth the manifest surface for a demo.
 */
class SessionStore(context: Context) {

    // applicationContext, not the Activity: this outlives any single Activity instance and holding
    // an Activity here would leak it.
    private val prefs = context.applicationContext
        .getSharedPreferences(FileName, Context.MODE_PRIVATE)

    /**
     * True once the user has reached the app proper at least once.
     *
     * Set on a successful sign-in or PIN entry; cleared on sign-out, which is what keeps the
     * onboarding and auth screens reachable after the first run — without that, a one-time gate
     * would make five screens of this UI kit unreachable until reinstall.
     */
    var hasCompletedSetup: Boolean
        get() = prefs.getBoolean(KeyCompletedSetup, false)
        set(value) = prefs.edit { putBoolean(KeyCompletedSetup, value) }

    private companion object {
        const val FileName = "forma.session"
        const val KeyCompletedSetup = "has_completed_setup"
    }
}
