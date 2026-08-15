/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
package dev.formaui.fintechuikit.data

import dev.formaui.fintechuikit.data.model.Account
import dev.formaui.fintechuikit.data.model.CardKind
import dev.formaui.fintechuikit.data.model.CardNetwork
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.ExchangeRate
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Notification
import dev.formaui.fintechuikit.data.model.NotificationKind
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.TransactionStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Demo fixtures.
 *
 * Every `@Preview` and every screen reads from here, so a component's preview and its place in
 * the app show the same data — a mismatch between the two is a common way previews quietly stop
 * reflecting reality.
 *
 * [today] is fixed rather than `LocalDate.now()`: previews must render identically on every run,
 * and a moving "today" would reshuffle the date groups between screenshots.
 */
object SampleData {

    val today: LocalDate = LocalDate.of(2026, 7, 24)

    private fun usd(major: Long, minor: Int = 0) = Money.of(major, minor, Currency.USD)

    val primaryAccount = Account(
        id = "acc-usd",
        label = "US Dollar",
        balance = usd(12_480, 65),
        isPrimary = true,
    )

    val accounts = listOf(
        primaryAccount,
        Account("acc-eur", "Euro", Money.of(3_215, 40, Currency.EUR)),
        Account("acc-gbp", "British Pound", Money.of(1_902, 10, Currency.GBP)),
        Account("acc-jpy", "Japanese Yen", Money.of(486_300, currency = Currency.JPY)),
    )

    /**
     * The ledger.
     *
     * `cardId` splits along the line the two cards are actually used on: the physical card pays in
     * person (groceries, coffee, transit, the utility bill), the virtual one pays online
     * (subscriptions, an online order and its refund). Salary, the transfer to Sofia and the rent
     * standing order carry **no** card — money moved without one, which is what null means.
     *
     * That split is what makes swiping the wallet change the activity list underneath it.
     */
    val transactions = listOf(
        Transaction(
            "t1", "Whole Foods", SpendCategory.Groceries, usd(-86, 42), today,
            cardId = "card-1",
        ),
        Transaction(
            "t2", "Blue Bottle Coffee", SpendCategory.Dining, usd(-6, 75), today,
            cardId = "card-1",
        ),
        Transaction(
            "t3", "Acme Corp", SpendCategory.Income, usd(4_200, 0), today,
            note = "July salary",
        ),
        Transaction(
            "t4", "Lyft", SpendCategory.Transport, usd(-23, 10), today.minusDays(1),
            status = TransactionStatus.Pending, cardId = "card-1",
        ),
        Transaction(
            "t5", "Netflix", SpendCategory.Entertainment, usd(-15, 49), today.minusDays(1),
            cardId = "card-2",
        ),
        Transaction("t6", "Sofia Almeida", SpendCategory.Transfer, usd(-250, 0), today.minusDays(1)),
        Transaction(
            "t7", "Trader Joes", SpendCategory.Groceries, usd(-54, 88), today.minusDays(2),
            cardId = "card-1",
        ),
        Transaction(
            "t8", "Pacific Gas", SpendCategory.Utilities, usd(-112, 30), today.minusDays(2),
            status = TransactionStatus.Failed, note = "Card declined", cardId = "card-1",
        ),
        Transaction(
            "t9", "Uniqlo", SpendCategory.Shopping, usd(-78, 0), today.minusDays(3),
            cardId = "card-2",
        ),
        // Rent goes out by standing order, not on a card — the case a category-based guess got wrong.
        Transaction("t10", "Landlord LLC", SpendCategory.Housing, usd(-2_150, 0), today.minusDays(4)),
        Transaction(
            "t11", "Chipotle", SpendCategory.Dining, usd(-14, 25), today.minusDays(4),
            cardId = "card-1",
        ),
        // A refund lands back on the card the purchase was made with, so it matches t9.
        Transaction(
            "t12", "Refund — Uniqlo", SpendCategory.Shopping, usd(39, 0), today.minusDays(5),
            cardId = "card-2",
        ),
        Transaction(
            "t13", "BART", SpendCategory.Transport, usd(-8, 60), today.minusDays(6),
            cardId = "card-1",
        ),
        Transaction(
            "t14", "Spotify", SpendCategory.Entertainment, usd(-11, 99), today.minusDays(7),
            cardId = "card-2",
        ),
    )

    val recentTransactions = transactions.take(5)

    /**
     * The inbox, deliberately built from transactions that already exist above.
     *
     * Every entry points at a row the user can go and find — the pending Lyft charge (`t4`), the
     * declined Pacific Gas payment (`t8`), the transfer to Sofia (`t6`), and July's salary (`t3`).
     * Inventing separate events would give the app two accounts of the same money.
     */
    val notifications = listOf(
        Notification(
            id = "n1",
            kind = NotificationKind.Security,
            title = "Card payment declined",
            body = "Pacific Gas for $112.30 didn't go through. Your card was declined — no money " +
                "left your account.",
            date = today.minusDays(2),
        ),
        Notification(
            id = "n2",
            kind = NotificationKind.Payment,
            title = "Payment pending",
            body = "Your $23.10 Lyft charge is still being confirmed. It'll settle on its own.",
            date = today.minusDays(1),
        ),
        Notification(
            id = "n3",
            kind = NotificationKind.Request,
            title = "You sent $250.00 to Sofia Almeida",
            body = "Sent yesterday. She'll see it in her euro balance.",
            date = today.minusDays(1),
            read = true,
        ),
        Notification(
            id = "n4",
            kind = NotificationKind.Payment,
            title = "Salary received",
            body = "Acme Corp paid you $4,200.00.",
            date = today,
            read = true,
        ),
    )

    /**
     * The wallet, in the order it is swiped.
     *
     * Two cards rather than one so the Card tab has something to page through, and one of each
     * [CardKind] so both faces and both capability sets are reachable in the demo. The virtual card
     * carries a smaller limit and lighter spend, which is the usual shape of a secondary card and
     * also puts the limit meter at a visibly different fill from the physical card's — swiping
     * between them should obviously change the screen below.
     */
    val cards = listOf(
        PaymentCard(
            id = "card-1",
            holderName = "ALEX MORGAN",
            last4 = "4821",
            expiryMonth = 11,
            expiryYear = 2029,
            network = CardNetwork.Visa,
            isFrozen = false,
            monthlyLimit = usd(3_000),
            spentThisMonth = usd(1_842, 20),
            kind = CardKind.Physical,
        ),
        PaymentCard(
            id = "card-2",
            holderName = "ALEX MORGAN",
            last4 = "9037",
            expiryMonth = 4,
            expiryYear = 2028,
            network = CardNetwork.Mastercard,
            isFrozen = false,
            monthlyLimit = usd(1_000),
            spentThisMonth = usd(212, 40),
            kind = CardKind.Virtual,
            // A virtual card has no chip and no plastic, so these two are not merely off — they do
            // not apply, and `supportsContactless` / `supportsAtm` keep their rows off the screen.
            contactless = false,
            atmWithdrawals = false,
        ),
    )

    /** The primary card. Kept as a name of its own because previews reference it directly. */
    val card: PaymentCard get() = cards.first()

    val recipients = listOf(
        Recipient("r1", "Sofia Almeida", "sofia@wise.example", Currency.EUR),
        Recipient("r2", "Kenji Watanabe", "kenji@wise.example", Currency.JPY),
        Recipient("r3", "Priya Nair", "priya@wise.example", Currency.GBP),
        Recipient("r4", "Tom Becker", "tom@wise.example", Currency.EUR),
    )

    val usdToEur = ExchangeRate(
        from = Currency.USD,
        to = Currency.EUR,
        rate = BigDecimal("0.9210"),
        feeRate = BigDecimal("0.0045"),
    )

    /**
     * The demo's rate table, all USD-based.
     *
     * [usdToEur] stays a named member because previews reference it directly; the table just adds
     * the pairs the recipient list actually needs. Before this existed the transfer flow converted
     * every recipient at the USD→EUR rate, so sending to a JPY or GBP payee showed euros.
     */
    val rates: List<ExchangeRate> = listOf(
        usdToEur,
        ExchangeRate(Currency.USD, Currency.JPY, BigDecimal("157.20"), BigDecimal("0.0045")),
        ExchangeRate(Currency.USD, Currency.GBP, BigDecimal("0.7860"), BigDecimal("0.0055")),
    )

    /**
     * The rate for a pair, or `null` when the demo cannot reach it.
     *
     * Resolved in three steps so that a currency picker or a swap can ask for any pair, not just
     * the three stored ones:
     *
     *  1. **Same currency** — an identity rate with no fee, so a USD→USD transfer converts 1:1
     *     rather than reporting "no rate".
     *  2. **Direct, or inverted** — the table is USD-based, so `EUR→USD` comes from
     *     [ExchangeRate.inverted].
     *  3. **Crossed through USD** — `EUR→JPY` is `(USD→JPY) / (USD→EUR)`. Every entry shares the
     *     pivot, so any pair among the held currencies resolves in at most two hops.
     */
    fun rateFor(from: Currency, to: Currency): ExchangeRate? {
        if (from == to) return ExchangeRate(from, to, BigDecimal.ONE, BigDecimal.ZERO)
        direct(from, to)?.let { return it }
        direct(to, from)?.let { return it.inverted() }

        val pivotToSource = direct(Currency.USD, from) ?: return null
        val pivotToTarget = direct(Currency.USD, to) ?: return null
        return ExchangeRate(
            from = from,
            to = to,
            rate = pivotToTarget.rate.divide(pivotToSource.rate, 10, RoundingMode.HALF_UP),
            // The crossed pair carries the target leg's fee rather than compounding both; the brand
            // shows one fee on the label, not a fee per hop.
            feeRate = pivotToTarget.feeRate,
        )
    }

    private fun direct(from: Currency, to: Currency): ExchangeRate? =
        rates.firstOrNull { it.from == from && it.to == to }

    /** Spend by category for the donut — derived from [transactions] so the two never disagree. */
    val spendByCategory: List<Pair<SpendCategory, Money>> =
        transactions
            .filter { it.amount.isDebit }
            .groupBy { it.category }
            .map { (category, items) ->
                category to Money(items.sumOf { it.amount.absolute.minorUnits }, Currency.USD)
            }
            .sortedByDescending { it.second.minorUnits }

    /** Twelve weeks of balance history for the trend line. */
    val balanceTrend: List<Float> = listOf(
        9_420f, 9_880f, 9_640f, 10_310f, 10_050f, 10_920f,
        11_140f, 10_870f, 11_560f, 11_980f, 12_240f, 12_480f,
    )

    val balanceTrendLabels = listOf("May", "Jun", "Jul")

    /**
     * Six months of outgoings per card, for the bar chart on the card screen.
     *
     * Keyed by card so the chart moves with the wallet like everything else on that screen. Each
     * series ends on the card's own `spentThisMonth` — 1,842 for the physical card, 212 for the
     * virtual one — so the July bar and the limit meter above it cannot disagree.
     */
    val monthlySpendByCard: Map<String, List<Pair<String, Float>>> = mapOf(
        "card-1" to listOf(
            "Feb" to 2_840f,
            "Mar" to 3_120f,
            "Apr" to 2_690f,
            "May" to 3_410f,
            "Jun" to 2_980f,
            "Jul" to 1_842f,
        ),
        "card-2" to listOf(
            "Feb" to 178f,
            "Mar" to 241f,
            "Apr" to 196f,
            "May" to 305f,
            "Jun" to 264f,
            "Jul" to 212f,
        ),
    )

    /** The primary card's series. Kept as a name of its own because previews reference it. */
    val monthlySpend: List<Pair<String, Float>>
        get() = monthlySpendByCard.getValue("card-1")
}
