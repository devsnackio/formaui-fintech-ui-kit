@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.formaui.components.card.FormaCard
import dev.formaui.components.card.FormaCardVariant
import dev.formaui.components.divider.FormaDivider
import dev.formaui.components.listitem.FormaListItem
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.core.theme.FormaTheme
import dev.formaui.fintechuikit.components.common.FlowScreen
import dev.formaui.fintechuikit.ui.theme.BrandDefaults
import dev.formaui.fintechuikit.ui.theme.BrandMotion
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.LocalReducedMotion

/** One question and its answer. Content only — the screen owns which ones are open. */
private data class Faq(val question: String, val answer: String)

/** A named run of questions. The title is what makes seven questions scannable instead of a list. */
private data class FaqSection(val title: String, val faqs: List<Faq>)

/**
 * The content, grouped the way the app is: money going out, the card, and the things people worry
 * about. Every answer describes behaviour this app actually has — the wayfinding lines name real
 * destinations (the Card tab's Change PIN, Profile → Security, Activity), so nothing here sends the
 * user looking for a screen that does not exist.
 */
private val FaqSections = listOf(
    FaqSection(
        title = "Sending money",
        faqs = listOf(
            Faq(
                "When will my transfer arrive?",
                "Most transfers land within minutes. Anything still moving shows as Pending in " +
                    "Activity, and the status updates there rather than by email.",
            ),
            Faq(
                "Why is there a fee on my transfer?",
                "The rate you see is the real one, and the fee is charged separately so you can " +
                    "see exactly what you paid for the conversion. Both are shown before you " +
                    "confirm.",
            ),
            Faq(
                "Why do I have more than one balance?",
                "You hold one balance per currency. Money sent to you in any of them lands in the " +
                    "matching balance, and sending converts between them at the rate shown before " +
                    "you confirm.",
            ),
        ),
    ),
    FaqSection(
        title = "Your card",
        faqs = listOf(
            Faq(
                "What happens when I freeze my card?",
                "New payments are declined immediately. Existing subscriptions and refunds still " +
                    "go through, and unfreezing takes effect straight away.",
            ),
            Faq(
                "How do I change my card PIN?",
                "Change PIN sits on the Card tab, and under Profile → Security if that is where " +
                    "you looked first. You enter the new PIN twice, and it applies straight away.",
            ),
        ),
    ),
    FaqSection(
        title = "Staying safe",
        faqs = listOf(
            Faq(
                "Someone asked me to pay a request. Is it safe?",
                "A request only ever moves money when you approve it. Check the name and the " +
                    "amount before you pay — a request cannot pull funds on its own.",
            ),
            Faq(
                "Can someone take money out of my balance?",
                "Not without you approving it on this device. A request only asks, card payments " +
                    "stop the moment you freeze the card, and your PIN is never shown back to you " +
                    "or to anyone who calls.",
            ),
        ),
    ),
)

/**
 * Help and support, behind Profile's "Help & support" row.
 *
 * **There is no "Contact us" action**: this demo has no inbox, no chat, and no phone number to dial,
 * so the button would open nothing. That is precisely the kind of control this batch of work exists
 * to remove, and adding one here to round out the screen would be re-introducing the problem. The
 * closing note says as much in the app's own voice rather than leaving the absence unexplained.
 *
 * **Still an accordion, and deliberately.** The alternatives were weighed: printing all seven answers
 * costs roughly a page and a half of prose and destroys the one thing this screen is good at — being
 * scannable — while a screen per question needs a route and a destination for two sentences of text.
 * What the accordion was missing was not the pattern but the craft: a chevron so a row announces that
 * it opens, an animated reveal so the answer arrives rather than blinks, an answer set at reading
 * size instead of subtitle size, and section titles so seven questions read as three topics.
 *
 * **Rows open independently.** A single-open accordion has to collapse whatever was open, which
 * yanks the page upward under the finger when the tapped row sits below the open one. Independent
 * rows only ever move what is beneath them, which is what a tap on that row implies.
 *
 * Expansion state is owned here — which questions are open matters to nothing outside this screen,
 * and it dies with it, like `TransferScreen`'s review-sheet flag.
 */
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openQuestions by remember { mutableStateOf(emptySet<String>()) }

    FlowScreen(
        title = "Help & support",
        onBack = onBack,
        modifier = modifier,
    ) {
        Text(
            text = "The questions people ask most. Tap one to read the answer.",
            style = FormaTheme.typography.material.bodyLarge,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )

        FaqSections.forEach { section ->
            FaqSectionCard(title = section.title) {
                section.faqs.forEachIndexed { index, faq ->
                    if (index > 0) FormaDivider()
                    FaqRow(
                        faq = faq,
                        open = faq.question in openQuestions,
                        onToggle = {
                            openQuestions = if (faq.question in openQuestions) {
                                openQuestions - faq.question
                            } else {
                                openQuestions + faq.question
                            }
                        },
                    )
                }
            }
        }

        Text(
            text = "There is no queue to join here. Freezing a card, changing your PIN and " +
                "checking whether a transfer has landed are all things you do yourself — on the " +
                "Card tab, under Profile → Security, and in Activity.",
            style = FormaTheme.typography.material.bodySmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * One question: a tappable header row and the answer it reveals.
 *
 * The header is a [FormaListItem] so the question row matches every other row in the app — same
 * height, same 16dp text inset, same full-width ripple — while the answer sits **outside** the list
 * item. That is the point of the rewrite: as `supporting` text the answer inherited the row's
 * subtitle slot, and a paragraph someone is meant to read is not a subtitle. Below the row it gets
 * `bodyMedium` (16sp) and the full width of the card, indented to line up with the question.
 *
 * Only the header carries the click. Making the whole block toggleable would mean a tap anywhere in
 * the answer collapses the thing being read.
 *
 * The chevron is the signpost the old row lacked: pointing down it says the row opens, and it turns
 * to point up while the answer expands, so the affordance and the state are the same mark.
 */
@Composable
private fun FaqRow(
    faq: Faq,
    open: Boolean,
    onToggle: () -> Unit,
) {
    // Both animations read this. Under reduced motion the chevron snaps and the answer appears with
    // no transition at all — the "render the plain thing, don't animate to a no-op" rule documented
    // on `LocalReducedMotion`.
    val reducedMotion = LocalReducedMotion.current

    val chevronRotation by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = if (reducedMotion) {
            snap<Float>()
        } else {
            // Opening and closing use the brand's two durations rather than one shared number, so
            // the chevron stays in step with the reveal it belongs to. Tweens, not springs: the
            // success badge is the app's only spring and should stay that way.
            tween(
                durationMillis = if (open) BrandMotion.EnterMillis else BrandMotion.ExitMillis,
                easing = if (open) BrandMotion.EnterEasing else BrandMotion.ExitEasing,
            )
        },
        label = "faqChevron",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        FormaListItem(
            headline = faq.question,
            onClick = onToggle,
            trailing = {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    // The row is the button and the state is announced below; the chevron is decor.
                    contentDescription = null,
                    modifier = Modifier.rotate(chevronRotation),
                )
            },
            // titleMedium, not the ListItem default: M3 sets a headline in bodyLarge, which is 20sp
            // on this brand scale. 16sp semibold reads as a question and leaves the answer, at the
            // same size in regular weight, clearly subordinate without being smaller.
            headlineTextStyle = FormaTheme.typography.material.titleMedium,
            // What a screen reader has that a sighted user gets from the chevron. Announced with
            // the row's own Role.Button semantics, which FormaListItem adds for onClick.
            modifier = Modifier.semantics {
                stateDescription = if (open) "Expanded" else "Collapsed"
            },
        )

        AnimatedVisibility(
            visible = open,
            enter = if (reducedMotion) {
                EnterTransition.None
            } else {
                fadeIn(tween(BrandMotion.EnterMillis, easing = BrandMotion.EnterEasing)) +
                    expandVertically(
                        tween(BrandMotion.EnterMillis, easing = BrandMotion.EnterEasing),
                    )
            },
            exit = if (reducedMotion) {
                ExitTransition.None
            } else {
                fadeOut(tween(BrandMotion.ExitMillis, easing = BrandMotion.ExitEasing)) +
                    shrinkVertically(
                        tween(BrandMotion.ExitMillis, easing = BrandMotion.ExitEasing),
                    )
            },
        ) {
            Text(
                text = faq.answer,
                style = FormaTheme.typography.material.bodyMedium,
                color = FormaTheme.colorScheme.onSurfaceVariant,
                // 16dp start/end matches M3's ListItem text inset, so the answer hangs off the
                // question rather than floating at its own margin. No top padding: the list item's
                // own vertical padding already separates the two.
                modifier = Modifier.padding(
                    start = FinTechTheme.spacing.lg,
                    end = FinTechTheme.spacing.lg,
                    bottom = FinTechTheme.spacing.lg,
                ),
            )
        }
    }
}

/**
 * A titled card of questions — the same shape as `SecurityScreen`'s `SecurityGroup` and
 * `ProfileScreen`'s `SettingsGroup`, kept local for the same reason: three lines of layout that
 * happen to agree today are not yet a shared component.
 *
 * `contentPadding = 0` is load-bearing: the question rows are full-width tap targets, and the card's
 * normal 24dp inset would stop the ripple short of the card edges.
 */
@Composable
private fun FaqSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FinTechTheme.spacing.sm)) {
        Text(
            title,
            style = FormaTheme.typography.material.titleSmall,
            color = FormaTheme.colorScheme.onSurfaceVariant,
        )
        FormaCard(
            variant = FormaCardVariant.Filled,
            shape = BrandDefaults.cardShape,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // One child: FormaCard's 16dp content arrangement cannot be overridden.
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Preview(name = "Help · light", heightDp = 900)
@Composable
private fun HelpScreenPreview() {
    FinTechTheme {
        HelpScreen(onBack = {})
    }
}

@Preview(name = "Help · dark", heightDp = 900)
@Composable
private fun HelpScreenDarkPreview() {
    FinTechTheme(darkTheme = true) {
        HelpScreen(onBack = {})
    }
}
