package dev.formaui.fintechuikit.data.model

import java.time.LocalDate

/**
 * A single movement of money.
 *
 * [amount] carries the direction — negative for a debit, positive for a credit — rather than a
 * separate boolean, so a running total is a plain sum and cannot disagree with the sign shown.
 */
data class Transaction(
    val id: String,
    val merchant: String,
    val category: SpendCategory,
    val amount: Money,
    val date: LocalDate,
    val status: TransactionStatus = TransactionStatus.Completed,
    val note: String? = null,
    /**
     * The [PaymentCard] this went through, or null if no card was involved.
     *
     * Null is the common case and the right default: a transfer, a top-up and a salary are all
     * movements of money with no card anywhere near them. Only a purchase carries one.
     */
    val cardId: String? = null,
) {
    /** Two-letter monogram for the row's avatar when there is no logo. */
    val initials: String
        get() = merchant.split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
}

enum class TransactionStatus { Completed, Pending, Failed }

/**
 * Spend categories. Deliberately holds no icon or colour — models stay free of Compose types so
 * they can be unit-tested and reused; the UI maps a category to its icon and swatch.
 */
enum class SpendCategory(val displayName: String) {
    Groceries("Groceries"),
    Dining("Dining"),
    Transport("Transport"),
    Housing("Housing"),
    Shopping("Shopping"),
    Utilities("Utilities"),
    Entertainment("Entertainment"),
    Income("Income"),
    Transfer("Transfer"),
}
