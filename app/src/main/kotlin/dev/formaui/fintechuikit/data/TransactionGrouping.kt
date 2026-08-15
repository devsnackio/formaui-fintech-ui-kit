package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Date-grouping for transaction lists.
 *
 * `java.time` is usable here despite minSdk 24 because the app enables core library
 * desugaring (see `app/build.gradle.kts`).
 */
private val SAME_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.US)
private val OTHER_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)

/**
 * The header a transaction sits under: "Today", "Yesterday", or the date.
 *
 * [today] is a parameter rather than a call to `LocalDate.now()` so grouping is deterministic
 * and testable — a function that reads the clock internally cannot be tested around midnight
 * or for "yesterday" without faking time.
 */
fun dateGroupLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(if (date.year == today.year) SAME_YEAR else OTHER_YEAR)
}

/**
 * Groups transactions into labelled, newest-first sections.
 *
 * Returns a [List] of pairs rather than a `Map` because callers render these in order, and only
 * `LinkedHashMap` would preserve it — a detail too easy to lose in a later refactor.
 */
fun List<Transaction>.groupedByDate(today: LocalDate): List<Pair<String, List<Transaction>>> =
    sortedByDescending { it.date }
        .groupBy { it.date }
        .map { (date, items) -> dateGroupLabel(date, today) to items }
