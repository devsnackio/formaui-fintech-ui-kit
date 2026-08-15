@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.components.paymentcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.model.CardKind
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.ui.theme.BrandTokens
import dev.formaui.fintechuikit.ui.theme.FinTechTheme

/**
 * The card face, in one of the brand's two feature-card treatments depending on [PaymentCard.kind].
 *
 * **The face owns its own colours.** This used to hardcode `CanvasSoft` text and `Mute` for the
 * frozen accent, which was fine while every card was near-black. It is exactly the pairing that
 * breaks the moment a surface changes underneath it — a fixed token against a varying background —
 * and the virtual card's pale-green face is that change. So each [CardFace] carries its own content,
 * accent and muted colours, and nothing here reaches for a token directly.
 *
 * The capability strip is not decoration: it is what makes the card-settings toggles honest. Turning
 * contactless off removes the glyph, so the switch has a visible consequence on the card itself
 * rather than only on its own row.
 */
@Composable
fun PaymentCardArt(
    card: PaymentCard,
    modifier: Modifier = Modifier,
) {
    val face = cardFace(card)
    val description = buildString {
        append("${card.kind.displayName} ${card.network.displayName} card ending ${card.last4}")
        if (card.isFrozen) append(", frozen")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // ISO/IEC 7810 ID-1 — real card proportions
            .clip(FormaTheme.shapes.xl)
            .background(face.brush)
            .semantics { contentDescription = description },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(FinTechTheme.spacing.xl),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.xs)) {
                    Text(
                        "Forma",
                        style = FormaTheme.typography.material.titleLarge,
                        color = face.accent,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        card.kind.displayName.uppercase(),
                        style = FormaTheme.typography.material.labelSmall,
                        color = face.muted,
                    )
                }
                // Frozen replaces the capability strip rather than joining it: a frozen card can do
                // none of those things, so listing them beside a snowflake would contradict itself.
                if (card.isFrozen) {
                    Icon(
                        Icons.Filled.AcUnit,
                        contentDescription = null, // already in the card's own description
                        tint = face.accent,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    CapabilityStrip(card, face)
                }
            }

            Text(
                text = card.maskedNumber,
                style = FormaTheme.typography.numeric.merge(FormaTheme.typography.material.titleMedium),
                color = face.content,
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                CardMeta("Card holder", card.holderName, face)
                CardMeta("Expires", card.expiry, face)
                Text(
                    card.network.displayName,
                    style = FormaTheme.typography.material.titleSmall,
                    color = face.accent,
                )
            }
        }
    }
}

/**
 * The glyphs for the capabilities this card both supports and currently has switched on.
 *
 * Nothing is drawn greyed-out. An off capability is simply absent, which keeps the strip a
 * statement of what the card can do right now rather than a checklist of what it might.
 */
@Composable
private fun CapabilityStrip(card: PaymentCard, face: CardFace) {
    Row(horizontalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
        if (card.supportsContactless && card.contactless) {
            Icon(
                Icons.Filled.Contactless,
                contentDescription = "Contactless enabled",
                tint = face.accent,
                modifier = Modifier.size(22.dp),
            )
        }
        if (card.onlinePayments) {
            Icon(
                Icons.Filled.Language,
                contentDescription = "Online payments enabled",
                tint = face.muted,
                modifier = Modifier.size(22.dp),
            )
        }
        if (card.supportsAtm && card.atmWithdrawals) {
            Icon(
                Icons.Filled.LocalAtm,
                contentDescription = "ATM withdrawals enabled",
                tint = face.muted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun CardMeta(label: String, value: String, face: CardFace) {
    Column {
        Text(
            label.uppercase(),
            style = FormaTheme.typography.material.labelSmall,
            color = face.muted,
        )
        Text(
            value,
            style = FormaTheme.typography.material.titleSmall,
            color = face.content,
        )
    }
}

/** The four colours a card face needs. Resolved per kind so no caller pairs mismatched tokens. */
@Immutable
private data class CardFace(
    val brush: Brush,
    val content: Color,
    val accent: Color,
    val muted: Color,
)

/**
 * The face for a card, by kind and freeze state.
 *
 * Both treatments are ones DESIGN.md names. Physical is `card-feature-dark` (DESIGN.md:165) — ink
 * with a Wise-green accent, the one place the brand sanctions green on a filled surface. Virtual is
 * `card-feature-green` (DESIGN.md:162) — the pale-green surface with ink text, which reads as
 * unmistakably a different card at a glance rather than a recolour of the same one.
 *
 * Frozen desaturates, and each face has to supply its own muted colour for it: `Mute` is legible on
 * ink but manages 3.2:1 on pale green, so the green face steps down to `body` instead.
 */
@Composable
private fun cardFace(card: PaymentCard): CardFace = when (card.kind) {
    CardKind.Physical -> CardFace(
        brush = Brush.linearGradient(listOf(BrandTokens.Ink, BrandTokens.InkLiftedHigh)),
        content = BrandTokens.CanvasSoft,
        accent = if (card.isFrozen) BrandTokens.Mute else BrandTokens.Primary,
        muted = BrandTokens.Mute,
    )
    CardKind.Virtual -> CardFace(
        brush = Brush.linearGradient(listOf(BrandTokens.PrimaryPale, BrandTokens.PrimaryNeutral)),
        content = BrandTokens.Ink,
        accent = if (card.isFrozen) BrandTokens.Body else BrandTokens.PositiveDeep,
        muted = BrandTokens.Body,
    )
}

@Composable
private fun CardArtStack(dark: Boolean) {
    FinTechTheme(darkTheme = dark) {
        Column(
            Modifier
                .background(FormaTheme.colorScheme.background)
                .padding(FinTechTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.lg),
        ) {
            SampleData.cards.forEach { card ->
                PaymentCardArt(card)
                PaymentCardArt(card.copy(isFrozen = true))
            }
        }
    }
}

/** Both kinds, each active and frozen — the four faces that exist. */
@Preview(name = "PaymentCardArt · light", heightDp = 1000)
@Composable
private fun PaymentCardArtPreview() = CardArtStack(dark = false)

@Preview(name = "PaymentCardArt · dark", heightDp = 1000)
@Composable
private fun PaymentCardArtDarkPreview() = CardArtStack(dark = true)
