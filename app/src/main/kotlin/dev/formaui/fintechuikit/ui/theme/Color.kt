package dev.formaui.fintechuikit.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Every colour token from `DESIGN.md`, one property per documented token.
 *
 * Names mirror the doc's `{colors.*}` keys so a token can be traced back to its source line.
 * Nothing outside this object should hardcode a hex value.
 */
object BrandTokens {
    // Brand & accent
    val Primary = Color(0xFF9FE870)         // {colors.primary} — the universal CTA green
    val PrimaryActive = Color(0xFFCDFFAD)   // {colors.primary-active}
    val PrimaryNeutral = Color(0xFFC5EDAB)  // {colors.primary-neutral}
    val PrimaryPale = Color(0xFFE2F6D5)     // {colors.primary-pale}

    // Surface
    val Canvas = Color(0xFFFFFFFF)          // {colors.canvas} — card interiors
    val CanvasSoft = Color(0xFFE8EBE6)      // {colors.canvas-soft} — the sage page background

    // Text
    val Ink = Color(0xFF0E0F0C)             // {colors.ink}
    val InkDeep = Color(0xFF163300)         // {colors.ink-deep}
    val Body = Color(0xFF454745)            // {colors.body}
    val Mute = Color(0xFF868685)            // {colors.mute}

    // Semantic
    val Positive = Color(0xFF2EAD4B)        // {colors.positive}
    val PositiveDeep = Color(0xFF054D28)    // {colors.positive-deep}
    val Warning = Color(0xFFFFD11A)         // {colors.warning}
    val WarningDeep = Color(0xFFB86700)     // {colors.warning-deep}
    val WarningContent = Color(0xFF4A3B1C)  // {colors.warning-content}
    val Negative = Color(0xFFD03238)        // {colors.negative}
    val NegativeDeep = Color(0xFFA72027)    // {colors.negative-deep}
    val NegativeDarkest = Color(0xFFA7000D) // {colors.negative-darkest}
    val NegativeBg = Color(0xFF320707)      // {colors.negative-bg}

    // Tertiary illustration accents
    val AccentOrange = Color(0xFFFFC091)    // {colors.accent-orange}
    val AccentCyan = Color(0xFF38C8FF)      // {colors.accent-cyan}

    // --- Dark-scheme surfaces -------------------------------------------------------------
    // DESIGN.md documents no dark palette. These fill the gaps it is silent on, derived from
    // the dark surfaces it *does* specify (`card-feature-dark`, `hero-band-dark`, `footer`).
    // TODO: replace if the brand publishes real dark values.
    val InkLifted = Color(0xFF1A1C18)            // TODO(derived): elevated surface on ink
    val InkLiftedHigh = Color(0xFF242721)        // TODO(derived): second elevation step
    val InkOutline = Color(0xFF454745)           // TODO(derived): hairline on dark
    val BodyOnDark = Color(0xFFB9BCB6)           // TODO(derived): secondary text on ink
    // TODO(derived): the third step of the text ramp on ink. `mute` is mid-grey rather than dark,
    // so unlike `body` it cannot be derived by inversion — inverting #868685 lands on another
    // mid-grey and reads *dimmer*, not lighter. Chosen instead to sit one clear step under
    // BodyOnDark while clearing AA: 6.2:1 on the ink card against BodyOnDark's 8.9:1.
    val MuteOnDark = Color(0xFF999C96)
    val PrimaryContainerDark = Color(0xFF2C4A17) // TODO(derived): green container on ink

    // The light-mode semantics are tuned for white/sage and lose contrast on ink, so the dark
    // scheme lightens them. TODO(derived): confirm against brand guidance.
    val PositiveOnDark = Color(0xFF6FD98A)
    val WarningOnDark = Color(0xFFFFDE5C)
    val NegativeOnDark = Color(0xFFFF6B70)

    /**
     * The forest-green *fill* for the categorical chart palette on ink.
     *
     * Deliberately not an override of [InkDeep]. `ink-deep` is an ink, and it stays correct in dark
     * wherever it is used as one — the green surfaces it is written on do not change between
     * schemes, so lightening it would drop it from 9.5:1 to 3.5:1 on the CTA green. It only ever
     * worked as a chart fill because the light card behind it is white; on the ink card it lands at
     * 1.23:1 and the slice reads as a gap in the ring. This is the fill that slot actually needed:
     * 3.3:1 on the card, and a clear two-and-a-half stops darker than `PositiveOnDark` so the two
     * greens still separate.
     * TODO(derived): replace if the brand publishes real dark values.
     */
    val ChartDeepGreenOnDark = Color(0xFF3E7A2A)

    /**
     * The brand green as an accent *mark* on the white card — a slider's track, a meter's fill.
     *
     * The palette has no token that works here, because its greens straddle the usable band. On
     * `{colors.canvas}` `{colors.primary}` is 1.47:1 and `{colors.positive}` is 2.92:1 — both under
     * WCAG's 3:1 for a non-text control — while `{colors.positive-deep}` and `{colors.ink-deep}`
     * jump to 10:1 and 13.9:1, which pass the maths and then render as a black bar. A green that
     * reads as black is not worth spending a green on.
     *
     * So this is `{colors.primary}` darkened in place: the same hue (96°) and saturation (72%),
     * lightness down to 32%, which lands at **4.2:1** — unmistakably the brand's green, and
     * unmistakably green. Derived from `primary` rather than `positive` on purpose: a slider's fill
     * is a brand accent, not a success signal, and a limit is not a thing to congratulate.
     * TODO(derived): replace if the brand publishes a mid green.
     */
    val PrimaryOnLight = Color(0xFF458C17)
}

/**
 * The DESIGN.md tokens that have no Material 3 slot to live in.
 *
 * M3's [ColorScheme] carries the brand's surface / CTA / error roles; everything else the doc
 * defines — the positive and warning families, the tertiary illustration accents, the
 * secondary text ramp — is exposed here and read via `FinTechTheme.brandColors`.
 */
@Immutable
data class BrandColors(
    val primaryActive: Color,
    val primaryNeutral: Color,
    val primaryPale: Color,
    val canvasSoft: Color,
    val inkDeep: Color,
    val body: Color,
    val mute: Color,
    val positive: Color,
    val positiveDeep: Color,
    val warning: Color,
    val warningDeep: Color,
    val warningContent: Color,
    val negativeDeep: Color,
    val negativeDarkest: Color,
    val negativeBg: Color,
    val accentOrange: Color,
    val accentCyan: Color,
    /**
     * The categorical palette's deep green. `{colors.ink-deep}` in light, a real fill in dark —
     * see [BrandTokens.ChartDeepGreenOnDark] for why this is not simply [inkDeep].
     */
    val chartDeepGreen: Color,
    /**
     * The brand green as a **mark on a card** — a slider's active track, a meter's fill, any small
     * accent shape drawn *on* `surface` rather than a surface things are drawn on.
     *
     * `{colors.primary}` serves this directly in dark and not at all in light. On the ink card it is
     * 11.7:1, and `card-feature-dark` already puts it straight onto ink; on the white card it is
     * 1.47:1, the same figure that sent [BrandDefaults.textButtonColors] to ink. The light scheme
     * therefore takes the derived [BrandTokens.PrimaryOnLight] — see there for why no shipped token
     * fits, and why the inks that *do* pass are the wrong answer anyway.
     *
     * Distinct from [chartDeepGreen], which solves the mirror-image problem: a chart fill has to
     * separate from the *other* slices as well as its card, so it stays muted in dark where a lone
     * control wants full strength, and stays an ink in light where a control wants a green.
     */
    val accentOnSurface: Color,
)

val LightBrandColors = BrandColors(
    primaryActive = BrandTokens.PrimaryActive,
    primaryNeutral = BrandTokens.PrimaryNeutral,
    primaryPale = BrandTokens.PrimaryPale,
    canvasSoft = BrandTokens.CanvasSoft,
    inkDeep = BrandTokens.InkDeep,
    body = BrandTokens.Body,
    mute = BrandTokens.Mute,
    positive = BrandTokens.Positive,
    positiveDeep = BrandTokens.PositiveDeep,
    warning = BrandTokens.Warning,
    warningDeep = BrandTokens.WarningDeep,
    warningContent = BrandTokens.WarningContent,
    negativeDeep = BrandTokens.NegativeDeep,
    negativeDarkest = BrandTokens.NegativeDarkest,
    negativeBg = BrandTokens.NegativeBg,
    accentOrange = BrandTokens.AccentOrange,
    accentCyan = BrandTokens.AccentCyan,
    chartDeepGreen = BrandTokens.InkDeep,
    accentOnSurface = BrandTokens.PrimaryOnLight,
)

val DarkBrandColors = LightBrandColors.copy(
    body = BrandTokens.BodyOnDark,
    // The whole text ramp lifts together. Lifting `body` for ink but leaving `mute` at its
    // light-mode value stretched the gap between them — body rose to 8.9:1 on the card while mute
    // stayed at 4.7:1 — so the lowest step read as switched-off rather than de-emphasised.
    mute = BrandTokens.MuteOnDark,
    positive = BrandTokens.PositiveOnDark,
    warning = BrandTokens.WarningOnDark,
    chartDeepGreen = BrandTokens.ChartDeepGreenOnDark,
    // The one place the *unchanged* CTA green is the right mark: on ink it reads 11.7:1, and
    // `card-feature-dark` already puts it straight onto ink. Its light-mode value would be 1.47:1.
    accentOnSurface = BrandTokens.Primary,
)

val LocalBrandColors = staticCompositionLocalOf { LightBrandColors }

/**
 * The brand's light scheme.
 *
 * The [background] / [surface] split is the load-bearing decision here: DESIGN.md's elevation
 * model is "sage canvas behind white cards — the surface contrast IS the elevation" (Level 2),
 * so `background` is the sage page and `surface` is the white card. `surfaceTint` is
 * transparent to suppress M3's elevation tint overlay, matching "Level 0 — Flat, no shadow".
 */
val BrandLightColorScheme: ColorScheme = lightColorScheme(
    primary = BrandTokens.Primary,
    // DESIGN.md's `button-primary` references {colors.on-primary} but never defines it.
    // ink-deep is the only ink the doc pairs with green surfaces, so it stands in.
    onPrimary = BrandTokens.InkDeep,
    primaryContainer = BrandTokens.PrimaryPale,
    onPrimaryContainer = BrandTokens.PositiveDeep, // per `badge-positive`
    inversePrimary = BrandTokens.PrimaryActive,

    // `button-secondary` — sage fill, ink label
    secondary = BrandTokens.CanvasSoft,
    onSecondary = BrandTokens.Ink,
    secondaryContainer = BrandTokens.CanvasSoft,
    onSecondaryContainer = BrandTokens.Ink,

    tertiary = BrandTokens.AccentCyan,
    onTertiary = BrandTokens.Ink,
    tertiaryContainer = BrandTokens.AccentOrange,
    onTertiaryContainer = BrandTokens.Ink,

    background = BrandTokens.CanvasSoft,
    onBackground = BrandTokens.Ink,
    surface = BrandTokens.Canvas,
    onSurface = BrandTokens.Ink,
    surfaceVariant = BrandTokens.CanvasSoft,
    onSurfaceVariant = BrandTokens.Body,
    surfaceTint = Color.Transparent,

    inverseSurface = BrandTokens.Ink,        // `card-feature-dark` / `hero-band-dark`
    inverseOnSurface = BrandTokens.CanvasSoft,

    error = BrandTokens.Negative,
    onError = BrandTokens.Canvas,
    // `badge-negative` is a dark maroon fill with white text — unusual for a light scheme's
    // error container, but it is what the brand specifies.
    errorContainer = BrandTokens.NegativeBg,
    onErrorContainer = BrandTokens.Canvas,

    outline = BrandTokens.Ink,               // Level 1 — 1px solid ink hairline
    outlineVariant = BrandTokens.Mute,
    scrim = BrandTokens.Ink,

    // The whole container ramp is white on purpose. In this brand a "container" is a card,
    // and cards are white by definition (`card-content`) — the sage page behind them is what
    // creates the depth. Grading these toward sage would make cards vanish into the canvas.
    // M3's FilledCard reads surfaceContainerHighest, so this is what makes the default card
    // render as the brand's flat white card. The sage and green feature cards
    // (`card-feature-sage` / `-green` / `-dark`) are explicit variants, not surface roles.
    surfaceBright = BrandTokens.Canvas,
    surfaceDim = BrandTokens.CanvasSoft,
    surfaceContainerLowest = BrandTokens.Canvas,
    surfaceContainerLow = BrandTokens.Canvas,
    surfaceContainer = BrandTokens.Canvas,
    surfaceContainerHigh = BrandTokens.Canvas,
    surfaceContainerHighest = BrandTokens.Canvas,
)

/**
 * The brand's dark scheme — derived, not specified.
 *
 * The anchors come straight from DESIGN.md: `background` = ink and `onSurface` = canvas-soft
 * per the `footer` band, and the green CTA is unchanged because `card-feature-dark` puts Wise
 * green directly on ink and the Don'ts list ink as a sanctioned neutral surface for it.
 * Everything marked TODO in [BrandTokens] is invented to fill gaps the doc leaves open.
 */
val BrandDarkColorScheme: ColorScheme = darkColorScheme(
    primary = BrandTokens.Primary,
    onPrimary = BrandTokens.InkDeep,
    primaryContainer = BrandTokens.PrimaryContainerDark,
    onPrimaryContainer = BrandTokens.PrimaryPale,
    inversePrimary = BrandTokens.PrimaryActive,

    secondary = BrandTokens.InkLiftedHigh,
    onSecondary = BrandTokens.CanvasSoft,
    secondaryContainer = BrandTokens.InkLiftedHigh,
    onSecondaryContainer = BrandTokens.CanvasSoft,

    tertiary = BrandTokens.AccentCyan,
    onTertiary = BrandTokens.Ink,
    tertiaryContainer = BrandTokens.AccentOrange,
    onTertiaryContainer = BrandTokens.Ink,

    background = BrandTokens.Ink,
    onBackground = BrandTokens.CanvasSoft,
    surface = BrandTokens.InkLifted,
    onSurface = BrandTokens.CanvasSoft,
    surfaceVariant = BrandTokens.InkLiftedHigh,
    onSurfaceVariant = BrandTokens.BodyOnDark,
    surfaceTint = Color.Transparent,

    inverseSurface = BrandTokens.CanvasSoft,
    inverseOnSurface = BrandTokens.Ink,

    error = BrandTokens.NegativeOnDark,
    onError = BrandTokens.Ink,
    // `badge-negative`'s dark maroon does not survive the trip to a dark scheme. On the ink card
    // (`InkLifted`) `negative-bg` #320707 sits at 1.05:1 — the destructive fill has no visible shape
    // at all — and anything reading `onError` for its content lands near-black on it at 1.07:1. The
    // semantic the doc is after is "saturated destructive fill, white label", so the dark scheme
    // keeps that and takes the value that actually works on ink: `{colors.negative}` itself, which
    // separates from the card at 3.4:1 and carries white text at 5.0:1.
    // TODO(derived): replace if the brand publishes real dark values.
    errorContainer = BrandTokens.Negative,
    onErrorContainer = BrandTokens.Canvas,

    outline = BrandTokens.InkOutline,
    outlineVariant = BrandTokens.Body,
    scrim = BrandTokens.Ink,

    // Same reasoning as the light scheme, inverted: cards lift *off* the ink page rather than
    // sitting white on sage, so the container ramp is the lifted ink tone throughout.
    surfaceBright = BrandTokens.InkLiftedHigh,
    surfaceDim = BrandTokens.Ink,
    surfaceContainerLowest = BrandTokens.Ink,
    surfaceContainerLow = BrandTokens.InkLifted,
    surfaceContainer = BrandTokens.InkLifted,
    surfaceContainerHigh = BrandTokens.InkLifted,
    surfaceContainerHighest = BrandTokens.InkLifted,
)
