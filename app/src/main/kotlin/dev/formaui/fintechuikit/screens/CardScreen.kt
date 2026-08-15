/*
 * Copyright 2026 FormaUI. Licensed under the Apache License, Version 2.0.
 */
@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.ReportGmailerrorred
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.button.FormaButton
import dev.formaui.components.button.FormaButtonVariant
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.dialog.FormaAlertDialog
import dev.formaui.components.emptystate.FormaEmptyState
import dev.formaui.components.loading.FormaLoadingIndicator
import dev.formaui.components.loading.FormaLoadingIndicatorVariant
import dev.formaui.components.slider.FormaSlider
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.chart.MonthlySpendCard
import dev.formaui.fintechuikit.components.common.SectionHeader
import dev.formaui.fintechuikit.components.common.SettingsToggleRow
import dev.formaui.fintechuikit.components.paymentcard.CardControlRow
import dev.formaui.fintechuikit.components.paymentcard.CardWallet
import dev.formaui.fintechuikit.components.status.StatusPill
import dev.formaui.fintechuikit.components.status.StatusTone
import dev.formaui.fintechuikit.components.transaction.TransactionRow
import dev.formaui.fintechuikit.data.LimitCeilingMinor
import dev.formaui.fintechuikit.data.LimitFloorMinor
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.data.model.Transaction
import dev.formaui.fintechuikit.data.model.formatWithSymbol
import dev.formaui.fintechuikit.data.onCard
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/** The step the limit slider snaps to, in minor units ($100). */
private const val LimitStepMinor = 10_000L

/** Snaps a raw slider position to the nearest whole [LimitStepMinor]. */
private fun Float.roundToStep(): Long =
    (this / LimitStepMinor).toDouble().let { Math.round(it) } * LimitStepMinor

/**
 * The card screen: the wallet, and everything that is true of the card currently showing.
 *
 * **Every section below the wallet is scoped to the selected card.** That is the whole reason the
 * pager's position is hoisted rather than kept inside it — the limit, the activity, the settings and
 * the chart all describe one card, and a swipe has to move all four together or the screen starts
 * telling two stories at once.
 *
 * The controls here are held to the same rule as the rest of the app: each one changes something the
 * user can go and look at. The settings toggles add and remove glyphs on the card face above them,
 * the limit slider moves the meter it sits under, and reporting a card lost changes its last four,
 * lands a security notice in the inbox, and takes you to a confirmation.
 */
@Composable
fun CardScreen(
    cards: List<PaymentCard>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    monthlySpendByCard: Map<String, List<Pair<String, Float>>>,
    transactions: List<Transaction>,
    onToggleFreeze: () -> Unit,
    onLimitChange: (Long) -> Unit,
    onContactlessChange: (Boolean) -> Unit,
    onOnlinePaymentsChange: (Boolean) -> Unit,
    onAtmChange: (Boolean) -> Unit,
    onReportLost: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onSeeAllActivity: () -> Unit,
    modifier: Modifier = Modifier,
    onChangePin: () -> Unit = {},
    onShowDetails: () -> Unit = {},
) {
    val card = cards.getOrNull(selectedIndex) ?: cards.first()
    var confirmingReport by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // Vertical only: the wallet's pager applies its own horizontal inset, and a gutter here
            // would narrow the pager and turn the outer 24dp into dead zones that swallow the edge
            // swipe — the same trap OnboardingScreen documents.
            .padding(vertical = FinTechTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
    ) {
        // No heading here: FinTechNavHost titles the four tabs from the app bar, so repeating it
        // would show "Card" twice and give a screen reader two labels for one screen.

        // Unstaggered, like all four tabs — see DashboardScreen for why.
        CardWallet(
            cards = cards,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = onSelectedIndexChange,
        )

        // Everything from here down carries the gutter the wallet opted out of.
        Column(
            modifier = Modifier.padding(horizontal = FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            CardControlRow(
                frozen = card.isFrozen,
                onToggleFreeze = onToggleFreeze,
                onChangePin = onChangePin,
                onShowDetails = onShowDetails,
                modifier = Modifier.fillMaxWidth(),
            )

            SpendLimitCard(
                card = card,
                onLimitChange = onLimitChange,
            )

            CardActivityCard(
                transactions = transactions,
                cardId = card.id,
                onTransactionClick = onTransactionClick,
                onSeeAll = onSeeAllActivity,
            )

            CardSettingsCard(
                card = card,
                onContactlessChange = onContactlessChange,
                onOnlinePaymentsChange = onOnlinePaymentsChange,
                onAtmChange = onAtmChange,
            )

            // Looked up here rather than passed pre-resolved, so the chart cannot lag a swipe and
            // draw one card's history under another's face.
            MonthlySpendCard(monthlySpendByCard[card.id].orEmpty())

            FormaButton(
                onClick = { confirmingReport = true },
                variant = FormaButtonVariant.Text,
                colors = BrandDefaults.destructiveTextButtonColors,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.ReportGmailerrorred, contentDescription = null)
                Text("  Report lost or stolen")
            }
        }
    }

    if (confirmingReport) {
        FormaAlertDialog(
            onDismissRequest = { confirmingReport = false },
            title = "Report this card lost?",
            text = "Your ${card.kind.displayName.lowercase()} card ending ${card.last4} will be " +
                "cancelled straight away and replaced with a new one. Anything already paid for " +
                "stays on your statement.",
            icon = {
                // The title says what this is; the icon is tone, not new information.
                Icon(Icons.Filled.CreditCardOff, contentDescription = null)
            },
            confirmButton = {
                FormaButton(
                    onClick = {
                        confirmingReport = false
                        onReportLost()
                    },
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.destructiveTextButtonColors,
                ) {
                    Text("Report lost")
                }
            },
            dismissButton = {
                FormaButton(
                    onClick = { confirmingReport = false },
                    variant = FormaButtonVariant.Text,
                    colors = BrandDefaults.textButtonColors,
                ) {
                    Text("Keep card")
                }
            },
        )
    }
}

/**
 * The monthly limit: what has been spent against it, and the control that sets it.
 *
 * The meter is `Linear`, explicitly. `FormaLoadingIndicator` defaults to `Circular`, so omitting the
 * variant silently produces a circular spinner stretched to the card's width — which is what this
 * card did before, under a comment calling it a meter.
 *
 * Over-limit is carried by a [StatusPill] rather than by tinting the bar, because
 * `FormaLoadingIndicator` exposes no colour parameter. The pill also survives the case the bar
 * cannot express at all: `limitProgress` clamps at 1f, so a full bar means "at the limit or past it"
 * and only the pill can say which.
 */
@Composable
private fun SpendLimitCard(
    card: PaymentCard,
    onLimitChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormaCard(
        variant = FormaCardVariant.Filled,
        shape = BrandDefaults.cardShape,
        contentPadding = BrandDefaults.cardContentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        // One child: FormaCard's 16dp content arrangement cannot be overridden.
        Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Monthly limit", style = FormaTheme.typography.material.titleMedium)
                if (card.isOverLimit) {
                    StatusPill(label = "Over limit", tone = StatusTone.Negative)
                } else if (card.limitProgress >= NearLimitFraction) {
                    StatusPill(label = "Nearly used", tone = StatusTone.Warning)
                }
            }

            Text(
                "${card.spentThisMonth.formatWithSymbol()} of ${card.monthlyLimit.formatWithSymbol()}",
                style = FormaTheme.typography.numeric.merge(
                    FormaTheme.typography.material.bodyMedium,
                ),
            )

            FormaLoadingIndicator(
                progress = card.limitProgress,
                // Linear, explicitly — the default is Circular, and a stretched circle is not a meter.
                variant = FormaLoadingIndicatorVariant.Linear,
                modifier = Modifier.fillMaxWidth(),
                contentDescription =
                    "${(card.limitProgress * 100).toInt()} percent of monthly limit used",
            )

            Text(
                "Set your limit",
                style = FormaTheme.typography.material.titleSmall,
                color = FormaTheme.colorScheme.onSurfaceVariant,
            )
            // Continuous, and rounded on the way out — NOT `steps`.
            //
            // M3 draws a tick mark per step, and $500..$10,000 in $100s is 94 of them: a tick every
            // few dp, which reads as a speckled bar rather than a track. The rounding has to happen
            // regardless, since the value still has to land on a round hundred, so doing it here
            // costs nothing and buys back a clean track. `withLimit` clamps it again at the data
            // layer, which is where the bound actually has to hold.
            FormaSlider(
                value = card.monthlyLimit.minorUnits.toFloat(),
                onValueChange = { onLimitChange(it.roundToStep()) },
                valueRange = LimitFloorMinor.toFloat()..LimitCeilingMinor.toFloat(),
                colors = BrandDefaults.sliderColors,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LimitBound(LimitFloorMinor, card)
                LimitBound(LimitCeilingMinor, card)
            }
        }
    }
}

@Composable
private fun LimitBound(minorUnits: Long, card: PaymentCard) {
    Text(
        text = card.monthlyLimit.copy(minorUnits = minorUnits).formatWithSymbol(),
        style = FormaTheme.typography.material.labelSmall,
        color = FormaTheme.colorScheme.onSurfaceVariant,
    )
}

/** Spend ratio at which the limit is worth flagging before it is actually breached. */
private const val NearLimitFraction = 0.9f

/**
 * This card's own recent spend — filtered by `cardId`, so it changes when the wallet is swiped.
 *
 * "Card activity" and the Activity tab are not the same list: the tab holds transfers, salary and a
 * standing order too, none of which touched a card. So "See all" goes to the full tab rather than
 * pretending this is a truncated view of it.
 */
@Composable
private fun CardActivityCard(
    transactions: List<Transaction>,
    cardId: String,
    onTransactionClick: (Transaction) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recent = transactions.onCard(cardId).sortedByDescending { it.date }.take(5)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
    ) {
        SectionHeader(
            title = "Card activity",
            actionLabel = if (recent.isEmpty()) null else "See all",
            onActionClick = if (recent.isEmpty()) null else onSeeAll,
        )

        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            // Zero: the rows are full-width tap targets, and the card's inset would stop the ripple
            // short of its edges.
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child: FormaCard's 16dp content arrangement cannot be overridden, and a gap
            // between list rows would break them into separate blocks.
            Column(Modifier.fillMaxWidth()) {
                if (recent.isEmpty()) {
                    FormaEmptyState(
                        title = "No card spending yet",
                        description = "Purchases made with this card will show up here.",
                        icon = { Icon(Icons.Filled.CreditCardOff, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FinTechTheme.spacing.lg),
                    )
                } else {
                    recent.forEach { txn ->
                        TransactionRow(txn, onClick = { onTransactionClick(txn) })
                    }
                }
            }
        }
    }
}

/**
 * What the card is allowed to do.
 *
 * Rows exist only for capabilities the card actually has — a virtual card shows neither contactless
 * nor ATM, because it has no chip and no plastic. A permanently-off switch would be a control that
 * cannot do anything, which is the same defect as one that does nothing.
 */
@Composable
private fun CardSettingsCard(
    card: PaymentCard,
    onContactlessChange: (Boolean) -> Unit,
    onOnlinePaymentsChange: (Boolean) -> Unit,
    onAtmChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm),
    ) {
        SectionHeader(title = "Card settings")

        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            // Zero for the same reason as the activity card: each row is its own tap target.
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.fillMaxWidth()) {
                if (card.supportsContactless) {
                    SettingsToggleRow(
                        icon = Icons.Filled.Contactless,
                        title = "Contactless",
                        value = "Tap to pay in shops",
                        checked = card.contactless,
                        onCheckedChange = onContactlessChange,
                    )
                }
                SettingsToggleRow(
                    icon = Icons.Filled.Language,
                    title = "Online payments",
                    value = "Use this card on the web",
                    checked = card.onlinePayments,
                    onCheckedChange = onOnlinePaymentsChange,
                )
                if (card.supportsAtm) {
                    SettingsToggleRow(
                        icon = Icons.Filled.LocalAtm,
                        title = "ATM withdrawals",
                        value = "Take cash out",
                        checked = card.atmWithdrawals,
                        onCheckedChange = onAtmChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardScreenPreview(
    dark: Boolean = false,
    selectedIndex: Int = 0,
    cards: List<PaymentCard> = SampleData.cards,
) {
    FinTechTheme(darkTheme = dark) {
        CardScreen(
            cards = cards,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = {},
            monthlySpendByCard = SampleData.monthlySpendByCard,
            transactions = SampleData.transactions,
            onToggleFreeze = {},
            onLimitChange = {},
            onContactlessChange = {},
            onOnlinePaymentsChange = {},
            onAtmChange = {},
            onReportLost = {},
            onTransactionClick = {},
            onSeeAllActivity = {},
        )
    }
}

@Preview(name = "Card · physical light", heightDp = 1700)
@Composable
private fun CardScreenLightPreview() = CardScreenPreview()

/** The virtual card — a different face, and two fewer settings rows. */
@Preview(name = "Card · virtual light", heightDp = 1700)
@Composable
private fun CardScreenVirtualPreview() = CardScreenPreview(selectedIndex = 1)

@Preview(name = "Card · virtual dark", heightDp = 1700)
@Composable
private fun CardScreenVirtualDarkPreview() = CardScreenPreview(dark = true, selectedIndex = 1)

/** Frozen and over its limit — the two states the card face and the meter exist to signal. */
@Preview(name = "Card · frozen over-limit dark", heightDp = 1700)
@Composable
private fun CardScreenFrozenDarkPreview() = CardScreenPreview(
    dark = true,
    cards = SampleData.cards.map {
        it.copy(isFrozen = true, spentThisMonth = it.monthlyLimit + it.monthlyLimit)
    },
)
