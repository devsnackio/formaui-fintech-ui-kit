package dev.formaui.fintechuikit.screens.auth

/**
 * The validation rules [SignInScreen] and [SignUpScreen] share.
 *
 * Plain Kotlin with no Compose or Android types, for the same reason `data/` is: these are the
 * rules that decide whether a CTA is live, and a rule that can only be exercised by rendering a
 * screen is a rule nobody tests.
 *
 * `android.util.Patterns.EMAIL_ADDRESS` is the usual reach here and is avoided deliberately —
 * it is an Android framework constant, so on the stubbed `android.jar` a plain JVM unit test
 * sees it as null and every call throws.
 */

/** The shortest password sign-up will accept. */
internal const val MinPasswordLength = 8

/**
 * Deliberately loose: something before an `@`, something after it, and at least one dot in the
 * domain. Tightening past this rejects addresses that are perfectly valid (`+` tags, new TLDs,
 * unicode locals) — the only authority on whether an address exists is sending mail to it, so
 * the field's job is to catch typos, not to adjudicate RFC 5322.
 */
private val EmailPattern = Regex("""^[^\s@]+@[^\s@.]+(\.[^\s@.]+)+$""")

internal fun String.isValidEmail(): Boolean = EmailPattern.matches(trim())

internal fun String.isLongEnoughPassword(): Boolean = length >= MinPasswordLength
