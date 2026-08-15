package dev.formaui.fintechuikit.data.model

/**
 * The currencies this kit demonstrates.
 *
 * [decimalPlaces] is per-currency on purpose: JPY has none, so a naive "always two decimals"
 * formatter would render ¥124,000 as ¥1,240.00 — a 100× error. [Money] keeps its value in
 * minor units and relies on this to place the decimal point.
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    /** Flag emoji, used by the currency selector chips. */
    val flag: String,
    val decimalPlaces: Int,
) {
    USD("USD", "$", "US Dollar", "🇺🇸", 2),
    EUR("EUR", "€", "Euro", "🇪🇺", 2),
    GBP("GBP", "£", "British Pound", "🇬🇧", 2),
    JPY("JPY", "¥", "Japanese Yen", "🇯🇵", 0),
    AUD("AUD", "A$", "Australian Dollar", "🇦🇺", 2),
    SGD("SGD", "S$", "Singapore Dollar", "🇸🇬", 2),
}
