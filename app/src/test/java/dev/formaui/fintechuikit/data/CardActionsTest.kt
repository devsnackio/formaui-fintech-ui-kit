package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.CardKind
import dev.formaui.fintechuikit.data.model.CardNetwork
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CardActionsTest {

    private val day = LocalDate.of(2026, 7, 15)
    private fun usd(major: Long, minor: Int = 0) = Money.of(major, minor, Currency.USD)

    private fun txn(
        id: String,
        category: SpendCategory,
        cardId: String? = null,
        status: TransactionStatus = TransactionStatus.Completed,
    ) = Transaction(
        id = id,
        merchant = "M-$id",
        category = category,
        amount = usd(-10),
        date = day,
        status = status,
        cardId = cardId,
    )

    private val card = PaymentCard(
        id = "c1",
        holderName = "ALEX MORGAN",
        last4 = "4821",
        expiryMonth = 11,
        expiryYear = 2029,
        network = CardNetwork.Visa,
        monthlyLimit = usd(3_000),
        spentThisMonth = usd(1_800),
    )

    // --- what counts as card spend ---------------------------------------------------------

    private val ledger = listOf(
        txn("a", SpendCategory.Groceries, cardId = "card-1"),
        txn("b", SpendCategory.Transfer),                      // no card: a transfer
        txn("c", SpendCategory.Entertainment, cardId = "card-2"),
        txn("d", SpendCategory.Income),                        // no card: salary
        txn("e", SpendCategory.Utilities, cardId = "card-1"),
        txn("f", SpendCategory.Housing),                       // no card: standing order
    )

    /**
     * The whole point of the field. Two cards in one wallet cannot be told apart by category, so a
     * category-based guess would hand both cards the same list and swiping would change nothing.
     */
    @Test
    fun `onCard returns only that card's transactions`() {
        assertEquals(listOf("a", "e"), ledger.onCard("card-1").map { it.id })
        assertEquals(listOf("c"), ledger.onCard("card-2").map { it.id })
    }

    /** Rent, salary and transfers moved money without a card, and must not be attributed to one. */
    @Test
    fun `onCard never returns a transaction with no card`() {
        val everyCard = ledger.onCard("card-1") + ledger.onCard("card-2")
        assertTrue(everyCard.none { it.cardId == null })
        assertTrue(ledger.onCard("card-3").isEmpty())
    }

    /**
     * A declined card payment is card activity — arguably the row you most want to find. Filtering
     * on status here would hide exactly the transaction the user opened the screen to look for.
     */
    @Test
    fun `onCard keeps pending and failed purchases`() {
        val cardLedger = listOf(
            txn("pending", SpendCategory.Shopping, "card-1", TransactionStatus.Pending),
            txn("failed", SpendCategory.Utilities, "card-1", TransactionStatus.Failed),
        )
        assertEquals(2, cardLedger.onCard("card-1").size)
    }

    @Test
    fun `onCard of an empty ledger is empty`() {
        assertTrue(emptyList<Transaction>().onCard("card-1").isEmpty())
    }

    // --- the limit -------------------------------------------------------------------------

    @Test
    fun `withLimit clamps at both ends`() {
        assertEquals(LimitFloorMinor, card.withLimit(1).monthlyLimit.minorUnits)
        assertEquals(LimitCeilingMinor, card.withLimit(99_999_999).monthlyLimit.minorUnits)
    }

    @Test
    fun `withLimit keeps a value inside the range untouched`() {
        assertEquals(250_000L, card.withLimit(250_000).monthlyLimit.minorUnits)
    }

    /** A limit in a currency the card does not spend in would make the meter compare nothing. */
    @Test
    fun `withLimit preserves the currency`() {
        assertEquals(Currency.USD, card.withLimit(200_000).monthlyLimit.currency)
    }

    /**
     * The case the slider makes reachable: drag the limit under what has already been spent.
     * `limitProgress` clamps, so it alone cannot say "over" — which is why `isOverLimit` exists.
     */
    @Test
    fun `dragging the limit below what is spent pins the meter and flags over-limit`() {
        val tightened = card.withLimit(100_000) // $1,000 against $1,800 already spent
        assertEquals(1f, tightened.limitProgress, 0.0001f)
        assertTrue(tightened.isOverLimit)
    }

    @Test
    fun `a limit above the spend is neither full nor over`() {
        val roomy = card.withLimit(300_000) // $3,000 against $1,800
        assertEquals(0.6f, roomy.limitProgress, 0.0001f)
        assertFalse(roomy.isOverLimit)
    }

    // --- replacement -----------------------------------------------------------------------

    @Test
    fun `a replacement arrives active with a new last4 and expiry`() {
        val replacement = card.copy(isFrozen = true).replaced(last4 = "5566", expiryYear = 2031)
        assertEquals("5566", replacement.last4)
        assertEquals(2031, replacement.expiryYear)
        assertFalse("a replacement should not arrive frozen", replacement.isFrozen)
        assertNotEquals(card.last4, replacement.last4)
    }

    /** The settings belonged to a card that no longer exists. */
    @Test
    fun `a replacement resets the capability toggles`() {
        val locked = card.copy(contactless = false, onlinePayments = false, atmWithdrawals = false)
        val replacement = locked.replaced(last4 = "5566", expiryYear = 2031)
        assertTrue(replacement.contactless)
        assertTrue(replacement.onlinePayments)
        assertTrue(replacement.atmWithdrawals)
    }

    /** The spending happened; the statement still counts it. Losing a card is not a fresh limit. */
    @Test
    fun `a replacement carries the month's spend over`() {
        val replacement = card.replaced(last4 = "5566", expiryYear = 2031)
        assertEquals(card.spentThisMonth, replacement.spentThisMonth)
        assertEquals(card.monthlyLimit, replacement.monthlyLimit)
    }

    // --- what a card can do at all ----------------------------------------------------------

    @Test
    fun `a virtual card supports neither contactless nor ATM`() {
        val virtual = card.copy(kind = CardKind.Virtual)
        assertFalse(virtual.supportsContactless)
        assertFalse(virtual.supportsAtm)
    }

    @Test
    fun `a physical card supports both`() {
        assertTrue(card.supportsContactless)
        assertTrue(card.supportsAtm)
    }
}
