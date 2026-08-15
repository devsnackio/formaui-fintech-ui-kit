package dev.formaui.fintechuikit.data.model

/** A currency balance the user holds. */
data class Account(
    val id: String,
    val label: String,
    val balance: Money,
    val isPrimary: Boolean = false,
)

/** A physical or virtual payment card. */
data class PaymentCard(
    val id: String,
    val holderName: String,
    /** Only the last four digits are ever modelled — a UI kit has no business holding a PAN. */
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val network: CardNetwork,
    val isFrozen: Boolean = false,
    val monthlyLimit: Money,
    val spentThisMonth: Money,
    val kind: CardKind = CardKind.Physical,
    val contactless: Boolean = true,
    val onlinePayments: Boolean = true,
    val atmWithdrawals: Boolean = true,
) {
    val maskedNumber: String get() = "•••• •••• •••• $last4"
    val expiry: String get() = "%02d/%02d".format(expiryMonth, expiryYear % 100)

    /** 0f..1f, clamped — drives the limit progress indicator. */
    val limitProgress: Float
        get() = if (monthlyLimit.minorUnits <= 0) 0f
        else (spentThisMonth.minorUnits.toFloat() / monthlyLimit.minorUnits).coerceIn(0f, 1f)

    /** True once spending has passed the limit — [limitProgress] alone clamps and cannot say so. */
    val isOverLimit: Boolean
        get() = monthlyLimit.minorUnits > 0 &&
            spentThisMonth.minorUnits > monthlyLimit.minorUnits

    // Which capabilities the card can have at all is a property of what it *is*, so it is derived
    // rather than stored: a virtual card has no chip to tap and no plastic to put in a cash
    // machine. The settings list reads these to decide which rows exist — a switch that can only
    // ever be off is the disabled-control form of a control that does nothing.
    val supportsContactless: Boolean get() = kind == CardKind.Physical
    val supportsAtm: Boolean get() = kind == CardKind.Physical
}

enum class CardNetwork(val displayName: String) { Visa("Visa"), Mastercard("Mastercard") }

/**
 * Whether a card exists as plastic or only as numbers.
 *
 * Load-bearing beyond the label: it picks the card's face in `PaymentCardArt` and decides which
 * capability toggles the card screen offers.
 */
enum class CardKind(val displayName: String) { Physical("Physical"), Virtual("Virtual") }

/** Someone the user can send money to. */
data class Recipient(
    val id: String,
    val name: String,
    val handle: String,
    val currency: Currency,
) {
    val initials: String
        get() = name.split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
}
