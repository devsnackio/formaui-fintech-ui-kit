@file:OptIn(ExperimentalFormaUiApi::class)

package dev.formaui.fintechuikit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.formaui.components.badge.FormaBadge
import dev.formaui.components.badge.FormaBadgedBox
import dev.formaui.components.iconbutton.FormaIconButton
import dev.formaui.core.annotation.ExperimentalFormaUiApi
import dev.formaui.fintechuikit.components.auth.PinDefaults
import dev.formaui.fintechuikit.components.converter.toMoneyOrZero
import dev.formaui.fintechuikit.data.SampleData
import dev.formaui.fintechuikit.data.StatementDirection
import dev.formaui.fintechuikit.data.applyDelta
import dev.formaui.fintechuikit.data.incomingTopUp
import dev.formaui.fintechuikit.data.model.Currency
import dev.formaui.fintechuikit.data.model.Money
import dev.formaui.fintechuikit.data.model.Notification
import dev.formaui.fintechuikit.data.model.NotificationKind
import dev.formaui.fintechuikit.data.model.PaymentCard
import dev.formaui.fintechuikit.data.model.Recipient
import dev.formaui.fintechuikit.data.model.SpendCategory
import dev.formaui.fintechuikit.data.model.unreadCount
import dev.formaui.fintechuikit.data.outgoingTransfer
import dev.formaui.fintechuikit.data.pendingRequest
import dev.formaui.fintechuikit.data.replaced
import dev.formaui.fintechuikit.data.withLimit
import dev.formaui.fintechuikit.navigation.Destination
import dev.formaui.fintechuikit.navigation.FinTechNavHost
import dev.formaui.fintechuikit.navigation.TopLevelTab
import dev.formaui.fintechuikit.navigation.enterSession
import dev.formaui.fintechuikit.navigation.leaveFlow
import dev.formaui.fintechuikit.navigation.leaveSession
import dev.formaui.fintechuikit.navigation.navigateToTab
import dev.formaui.fintechuikit.navigation.screen
import dev.formaui.fintechuikit.screens.AccountsScreen
import dev.formaui.fintechuikit.screens.AddMoneyScreen
import dev.formaui.fintechuikit.screens.CardDetailsScreen
import dev.formaui.fintechuikit.screens.CardScreen
import dev.formaui.fintechuikit.screens.ChangePinScreen
import dev.formaui.fintechuikit.screens.DashboardScreen
import dev.formaui.fintechuikit.screens.NotificationsScreen
import dev.formaui.fintechuikit.screens.PinStage
import dev.formaui.fintechuikit.screens.ProfileScreen
import dev.formaui.fintechuikit.screens.ReceiveScreen
import dev.formaui.fintechuikit.screens.RequestMoneyScreen
import dev.formaui.fintechuikit.screens.StatementsScreen
import dev.formaui.fintechuikit.screens.SuccessScreen
import dev.formaui.fintechuikit.screens.TopUpSource
import dev.formaui.fintechuikit.screens.TransactionDetailScreen
import dev.formaui.fintechuikit.screens.TransactionsScreen
import dev.formaui.fintechuikit.screens.TransferRecipientScreen
import dev.formaui.fintechuikit.screens.TransferScreen
import dev.formaui.fintechuikit.screens.auth.ForgotPasswordScreen
import dev.formaui.fintechuikit.screens.auth.PinEntryScreen
import dev.formaui.fintechuikit.screens.auth.SignInScreen
import dev.formaui.fintechuikit.screens.auth.SignUpScreen
import dev.formaui.fintechuikit.screens.auth.TermsScreen
import dev.formaui.fintechuikit.screens.onboarding.OnboardingScreen
import dev.formaui.fintechuikit.screens.onboarding.WelcomeScreen
import dev.formaui.fintechuikit.screens.settings.HelpScreen
import dev.formaui.fintechuikit.screens.settings.Language
import dev.formaui.fintechuikit.screens.settings.SecurityScreen
import dev.formaui.fintechuikit.ui.theme.FinTechTheme
import dev.formaui.fintechuikit.ui.theme.ThemeMode
import dev.formaui.fintechuikit.ui.theme.resolveDark
import kotlinx.coroutines.launch

/**
 * The PIN the demo accepts. A real app would never hold this client-side — it exists so the
 * wrong-PIN error path is reachable in the showcase.
 */
private const val DemoPin = "123456"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val session = SessionStore(this)
        // Read once, here, and not as observable state: the graph's start destination has to be
        // decided before the NavHost is composed, and it must not change underneath a running
        // graph. Later transitions are navigations (enterSession / leaveSession), not a new start
        // destination.
        val startDestination = if (session.hasCompletedSetup) {
            Destination.Dashboard
        } else {
            Destination.PreSession
        }

        setContent {
            // Theme mode is hoisted ABOVE FinTechTheme so a change re-themes the whole app.
            // rememberSaveable carries it across configuration changes (rotation); unlike the
            // setup flag it is not persisted, so it resets on a cold start.
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.System) }
            FinTechTheme(darkTheme = themeMode.resolveDark()) {
                FinTechApp(
                    modifier = Modifier.fillMaxSize(),
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    startDestination = startDestination,
                    onSetupComplete = { session.hasCompletedSetup = true },
                    onSetupReset = { session.hasCompletedSetup = false },
                )
            }
        }
    }
}

/**
 * The app: demo state plus the navigation graph.
 *
 * All mutable state is hoisted here so every screen underneath stays stateless and previewable
 * — this is the one place that owns the demo's session. A real app would replace this with
 * ViewModels, but for a UI kit a single holder keeps the wiring legible.
 *
 * [onSetupComplete] and [onSetupReset] are passed as callbacks rather than by handing this
 * composable a [SessionStore], so the whole navigation graph stays free of Android framework types
 * and this function remains callable from a preview or a test with two no-ops.
 */
@Composable
private fun FinTechApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    startDestination: Any,
    onSetupComplete: () -> Unit,
    onSetupReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // The money. These start as SampleData's fixtures and are mutable from here on: a confirmed
    // transfer debits `accounts` and prepends to `activity`, so the Dashboard balance and the
    // Activity list are the same state the flows write to, not static fixtures that ignore them.
    var accounts by remember { mutableStateOf(SampleData.accounts) }
    var activity by remember { mutableStateOf(SampleData.transactions) }
    val primaryAccount = accounts.first { it.isPrimary }

    // Ids for rows created during the session. Counting up rather than using the list size keeps
    // ids unique even though nothing is ever removed.
    // mutableIntStateOf, not mutableStateOf: an Int in a generic State box autoboxes on every write.
    var createdCount by remember { mutableIntStateOf(0) }

    // What the shared success screen shows. Held here rather than passed as route arguments so
    // `Money` needs no custom NavType — but saveable, because the nav back stack outlives a plain
    // remember and the success screen has to be able to redraw itself after a rotation.
    var lastAction by rememberSaveable(stateSaver = CompletedActionSaver) {
        mutableStateOf(null)
    }

    // Transfer state
    var sendAmount by remember { mutableStateOf("1000.00") }
    var selectedRecipient by remember { mutableStateOf<Recipient?>(SampleData.recipients.first()) }
    var sourceCurrency by remember { mutableStateOf(Currency.USD) }
    var targetCurrency by remember { mutableStateOf(SampleData.recipients.first().currency) }

    // Add money
    var topUpAmount by remember { mutableStateOf("") }
    var topUpSource by remember { mutableStateOf(TopUpSource.Card) }

    // Request money
    var requestAmount by remember { mutableStateOf("") }
    var requestNote by remember { mutableStateOf("") }
    var requestFrom by remember { mutableStateOf<Recipient?>(null) }

    // Transactions filters. searchVisible is here rather than in the screen because the app-bar
    // action that toggles it lives in the shell.
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SpendCategory?>(null) }
    var searchVisible by remember { mutableStateOf(false) }

    // Inbox. `lastDismissed` holds the swiped item only until the undo snackbar resolves — a
    // destructive gesture with nothing held is a gesture with no way back.
    // `inbox`, not `notifications` — that name is already the Profile push-notifications toggle,
    // a Boolean. Two different things called the same thing in one scope is how this collided.
    var inbox by remember { mutableStateOf(SampleData.notifications) }
    var lastDismissed by remember { mutableStateOf<Notification?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Statements filters
    var statementStart by remember { mutableStateOf(SampleData.today.minusDays(7)) }
    var statementEnd by remember { mutableStateOf(SampleData.today) }
    var statementDirection by remember { mutableStateOf(StatementDirection.All) }
    var statementAmount by remember { mutableStateOf(0f..500_000f) }

    // Settings
    var language by remember { mutableStateOf(Language.EnglishUS) }
    var languageOpen by remember { mutableStateOf(false) }

    // Change PIN. One `pin` value across both stages — the user answers one question twice, and a
    // second field would let "confirm" hold something the "choose" stage never saw.
    var pin2 by remember { mutableStateOf("") }
    var pinStage by remember { mutableStateOf(PinStage.Choose) }
    var chosenPin by remember { mutableStateOf("") }
    var pinMismatch by remember { mutableStateOf(false) }

    // The appearance sheet has two entry points — the Profile row and the app-bar settings action —
    // so neither can own it.
    var appearanceOpen by remember { mutableStateOf(false) }

    // The wallet. The whole card is state now, not just its freeze flag: the limit, the capability
    // toggles and the last four all change from the Card tab. `selectedCardIndex` is saveable
    // because it survives rotation as the answer to "which card am I looking at" — the sections
    // below the pager are all scoped to it, so losing it would silently re-point the screen at
    // card one.
    var cards by remember { mutableStateOf(SampleData.cards) }
    var selectedCardIndex by rememberSaveable { mutableIntStateOf(0) }
    var replacedCount by rememberSaveable { mutableIntStateOf(0) }

    // Profile toggles
    var biometrics by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(false) }

    val selectedCard = cards.getOrElse(selectedCardIndex) { cards.first() }

    /** Applies [change] to the card currently showing, leaving the rest of the wallet alone. */
    fun updateSelectedCard(change: (PaymentCard) -> PaymentCard) {
        cards = cards.mapIndexed { index, card ->
            if (index == selectedCardIndex) change(card) else card
        }
    }

    // Sign-in form
    var signInEmail by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }
    var signInPasswordVisible by remember { mutableStateOf(false) }

    // Sign-up form — kept separate from the sign-in fields so switching between the two screens
    // does not carry a half-typed credential across.
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // Password reset. `resetSent` is hoisted so re-entering the flow starts on the form rather
    // than on a stale "check your inbox".
    var resetEmail by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }

    // PIN entry
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    FinTechNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        topBarActions = { tab ->
            when (tab) {
                TopLevelTab.Activity -> {
                    FormaIconButton(
                        onClick = {
                            // Clear the query on the way out. A hidden field with a live filter
                            // would leave the list quietly filtered with nothing on screen to
                            // explain why.
                            if (searchVisible) query = ""
                            searchVisible = !searchVisible
                        },
                    ) {
                        Icon(
                            imageVector = if (searchVisible) {
                                Icons.Filled.SearchOff
                            } else {
                                Icons.Filled.Search
                            },
                            // The label states what the tap will do, and it has to change with the
                            // state — a toggle announced only as "Search" is a toggle a screen
                            // reader user cannot tell the position of.
                            contentDescription = if (searchVisible) {
                                "Hide search"
                            } else {
                                "Search transactions"
                            },
                        )
                    }
                }

                TopLevelTab.Profile -> {
                    FormaIconButton(onClick = { appearanceOpen = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Appearance settings")
                    }
                }

                TopLevelTab.Home -> {
                    // navController, not `nav`: topBarActions is its own lambda and the
                    // builder's navigator is not in scope here.
                    FormaIconButton(onClick = { navController.navigate(Destination.Notifications) }) {
                        val unread = inbox.unreadCount
                        FormaBadgedBox(
                            badge = { if (unread > 0) FormaBadge(count = unread) },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsNone,
                                // The count is decoration for a screen reader; the label carries it.
                                contentDescription = if (unread > 0) {
                                    "Notifications, $unread unread"
                                } else {
                                    "Notifications"
                                },
                            )
                        }
                    }
                }

                // Card has nothing that belongs in an app bar — its actions are its own control
                // row. An icon here for the sake of symmetry would be an affordance with no job.
                TopLevelTab.Card -> Unit
            }
        },
    ) { nav ->
        // Reaching the app proper is what "completed setup" means, so both terminal auth paths —
        // sign-in, and sign-up's PIN step — record it before navigating. Keeping the two in one
        // lambda stops them drifting apart.
        val enterApp: () -> Unit = {
            onSetupComplete()
            nav.enterSession()
        }
        // Everything before sign-in lives in one nested graph. That grouping is load-bearing: the
        // shell decides whether to show the bottom bar with a single hasRoute<PreSession>() check,
        // and enterSession() drops the whole flow off the back stack in one popUpTo.
        navigation<Destination.PreSession>(startDestination = Destination.Welcome) {
            screen<Destination.Welcome> {
                WelcomeScreen(
                    onGetStarted = { nav.navigate(Destination.Onboarding) },
                    // "I already have an account" jumps the intro deck.
                    onSignIn = { nav.navigate(Destination.SignIn) },
                )
            }
            screen<Destination.Onboarding> {
                // Skip and finish land in the same place — the deck is informational, so leaving
                // it early and reading it through both end at the same gate.
                OnboardingScreen(
                    onFinish = { nav.navigate(Destination.SignIn) },
                    onSkip = { nav.navigate(Destination.SignIn) },
                )
            }
            screen<Destination.SignIn> {
                SignInScreen(
                    email = signInEmail,
                    onEmailChange = { signInEmail = it },
                    password = signInPassword,
                    onPasswordChange = { signInPassword = it },
                    passwordVisible = signInPasswordVisible,
                    onPasswordVisibleChange = { signInPasswordVisible = it },
                    // The demo accepts any credentials the screen's own validation lets through.
                    onSubmit = enterApp,
                    onCreateAccount = { nav.navigate(Destination.SignUp) },
                    onForgotPassword = {
                        // Start clean: pre-fill from the sign-in field, drop any previous send.
                        resetEmail = signInEmail
                        resetSent = false
                        nav.navigate(Destination.ForgotPassword)
                    },
                )
            }
            screen<Destination.SignUp> {
                SignUpScreen(
                    name = signUpName,
                    onNameChange = { signUpName = it },
                    email = signUpEmail,
                    onEmailChange = { signUpEmail = it },
                    password = signUpPassword,
                    onPasswordChange = { signUpPassword = it },
                    passwordVisible = signUpPasswordVisible,
                    onPasswordVisibleChange = { signUpPasswordVisible = it },
                    termsAccepted = termsAccepted,
                    onTermsAcceptedChange = { termsAccepted = it },
                    // A new account finishes by choosing a PIN, so sign-up hands off there rather
                    // than straight into the app.
                    onSubmit = { nav.navigate(Destination.PinEntry) },
                    onSignIn = { nav.popBackStack() },
                    onViewTerms = { nav.navigate(Destination.Terms) },
                )
            }
            screen<Destination.ForgotPassword> {
                ForgotPasswordScreen(
                    email = resetEmail,
                    onEmailChange = { resetEmail = it },
                    sent = resetSent,
                    // The demo never checks whether the address has an account — see the screen's
                    // note on why the copy stays vague about it.
                    onSubmit = { resetSent = true },
                    onBack = { nav.popBackStack() },
                )
            }

            screen<Destination.Terms> {
                TermsScreen(onBack = { nav.popBackStack() })
            }

            screen<Destination.PinEntry> {
                PinEntryScreen(
                    pin = pin,
                    // Clearing the error on every keystroke is required by the component, not
                    // incidental: the shake keys on the false -> true edge, so a latched error
                    // would make the second wrong attempt silent.
                    onPinChange = { entered ->
                        pin = entered
                        pinError = false
                    },
                    isError = pinError,
                    errorMessage = if (pinError) "Incorrect PIN. Try again." else null,
                    onComplete = { entered ->
                        // Reset the field directly rather than through onPinChange, which would
                        // wipe the error flag we are about to set.
                        pin = ""
                        if (entered == DemoPin) {
                            pinError = false
                            enterApp()
                        } else {
                            pinError = true
                        }
                    },
                    // The screen confirms first, so this only runs once the user has said yes.
                    // popUpTo rather than navigate: SignIn is already below this on the stack
                    // (SignIn -> SignUp -> PinEntry), and pushing a second copy would leave a
                    // dead PIN gate behind it that back could walk into again.
                    onForgotPin = {
                        pin = ""
                        pinError = false
                        nav.navigate(Destination.SignIn) {
                            popUpTo(Destination.SignIn) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        screen<Destination.Dashboard> {
            DashboardScreen(
                account = primaryAccount,
                recentTransactions = activity.take(5),
                spendByCategory = SampleData.spendByCategory,
                balanceTrend = SampleData.balanceTrend,
                onSendClick = { nav.navigate(Destination.Transfer) },
                onViewBalances = { nav.navigate(Destination.Accounts) },
                onAddMoneyClick = { nav.navigate(Destination.AddMoney) },
                onRequestClick = { nav.navigate(Destination.RequestMoney) },
                // Switch to the Activity tab via the tab-switch recipe, NOT a plain
                // navigate() — a plain navigate to a tab destination breaks the bottom bar.
                onSeeAllTransactions = { nav.navigateToTab(TopLevelTab.Activity) },
                onTransactionClick = { txn ->
                    nav.navigate(Destination.TransactionDetail(txn.id))
                },
            )
        }
        screen<Destination.Transactions> {
            TransactionsScreen(
                transactions = activity,
                query = query,
                onQueryChange = { query = it },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                today = SampleData.today,
                searchVisible = searchVisible,
                onTransactionClick = { txn ->
                    nav.navigate(Destination.TransactionDetail(txn.id))
                },
            )
        }
        screen<Destination.Card> {
            CardScreen(
                cards = cards,
                selectedIndex = selectedCardIndex,
                onSelectedIndexChange = { selectedCardIndex = it },
                monthlySpendByCard = SampleData.monthlySpendByCard,
                transactions = activity,
                onToggleFreeze = { updateSelectedCard { it.copy(isFrozen = !it.isFrozen) } },
                onLimitChange = { minorUnits ->
                    updateSelectedCard { it.withLimit(minorUnits) }
                },
                onContactlessChange = { on -> updateSelectedCard { it.copy(contactless = on) } },
                onOnlinePaymentsChange = { on ->
                    updateSelectedCard { it.copy(onlinePayments = on) }
                },
                onAtmChange = { on -> updateSelectedCard { it.copy(atmWithdrawals = on) } },
                onReportLost = {
                    val lost = selectedCard
                    replacedCount += 1
                    // A caller-supplied last four keeps `replaced` pure — no clock, no RNG — the
                    // same reason the MoneyMovement builders take an id.
                    val newLast4 = (5000 + replacedCount).toString()
                    updateSelectedCard {
                        it.replaced(last4 = newLast4, expiryYear = it.expiryYear + 3)
                    }
                    // The inbox is where the app already tells the user things happened, so a
                    // security event belongs there rather than only on the card face.
                    inbox = listOf(
                        Notification(
                            id = "card-replaced-$replacedCount",
                            kind = NotificationKind.Security,
                            title = "Card ending ${lost.last4} cancelled",
                            body = "A replacement ending $newLast4 is on its way.",
                            date = SampleData.today,
                            read = false,
                        ),
                    ) + inbox
                    lastAction = CompletedAction(
                        headline = "Card cancelled.",
                        amount = null,
                        subtitle = "A replacement ending $newLast4 is on its way",
                    )
                    nav.toSuccess()
                },
                onTransactionClick = { txn ->
                    nav.navigate(Destination.TransactionDetail(txn.id))
                },
                onSeeAllActivity = { nav.navigateToTab(TopLevelTab.Activity) },
                onChangePin = {
                    // Reset the flow on entry so it never opens mid-way through a previous attempt.
                    pin2 = ""
                    chosenPin = ""
                    pinStage = PinStage.Choose
                    pinMismatch = false
                    nav.navigate(Destination.ChangePin)
                },
                onShowDetails = { nav.navigate(Destination.CardDetails) },
            )
        }
        screen<Destination.Profile> {
            ProfileScreen(
                biometricsEnabled = biometrics,
                onBiometricsChange = { biometrics = it },
                notificationsEnabled = notifications,
                onNotificationsChange = { notifications = it },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                language = language,
                onLanguageChange = { language = it },
                languageOpen = languageOpen,
                onLanguageOpenChange = { languageOpen = it },
                onSecurityClick = { nav.navigate(Destination.Security) },
                onStatementsClick = { nav.navigate(Destination.Statements) },
                onHelpClick = { nav.navigate(Destination.Help) },
                appearanceOpen = appearanceOpen,
                onAppearanceOpenChange = { appearanceOpen = it },
                // Sign-out is the only way back to the pre-session flow once setup is recorded.
                // Without it, the one-time gate would make Welcome, Onboarding, SignIn, SignUp and
                // PinEntry unreachable for the rest of the install.
                onSignOut = {
                    onSetupReset()
                    // Blank the credentials on the way out. Leaving them would hand the next
                    // person at the device a pre-filled sign-in form.
                    signInEmail = ""
                    signInPassword = ""
                    signInPasswordVisible = false
                    pin = ""
                    pinError = false
                    nav.leaveSession()
                },
            )
        }
        screen<Destination.Accounts> {
            AccountsScreen(accounts = accounts, onBack = { nav.popBackStack() })
        }

        screen<Destination.CardDetails> {
            // The card showing in the wallet, not a fixed one — Details is reached from the wallet
            // and would otherwise describe a card the user had swiped away from.
            CardDetailsScreen(
                card = selectedCard,
                onBack = { nav.popBackStack() },
            )
        }

        screen<Destination.ChangePin> {
            ChangePinScreen(
                pin = pin2,
                stage = pinStage,
                isError = pinMismatch,
                errorMessage = if (pinMismatch) "That didn't match. Choose a PIN again." else null,
                onBack = { nav.popBackStack() },
                onPinChange = { entered ->
                    // Clearing the error on every keystroke is the PinDots contract: the shake keys
                    // on the false -> true edge, so a latched error makes the next mismatch silent.
                    pinMismatch = false
                    pin2 = entered
                    if (entered.length < PinDefaults.Length) return@ChangePinScreen

                    when (pinStage) {
                        PinStage.Choose -> {
                            chosenPin = entered
                            pin2 = ""
                            pinStage = PinStage.Confirm
                        }
                        PinStage.Confirm -> {
                            pin2 = ""
                            if (entered == chosenPin) {
                                lastAction = CompletedAction(
                                    headline = "PIN changed.",
                                    amount = null,
                                    subtitle = "Use it next time you pay in a shop",
                                )
                                nav.toSuccess()
                            } else {
                                // Back to the start: confirming against a PIN they may have
                                // mistyped first is worse than asking again.
                                chosenPin = ""
                                pinStage = PinStage.Choose
                                pinMismatch = true
                            }
                        }
                    }
                },
            )
        }

        screen<Destination.Security> {
            SecurityScreen(
                onChangePin = {
                    pin2 = ""
                    chosenPin = ""
                    pinStage = PinStage.Choose
                    pinMismatch = false
                    nav.navigate(Destination.ChangePin)
                },
                onBack = { nav.popBackStack() },
            )
        }

        screen<Destination.Statements> {
            StatementsScreen(
                transactions = activity,
                startDate = statementStart,
                endDate = statementEnd,
                onRangeChange = { start, end ->
                    statementStart = start
                    statementEnd = end
                },
                direction = statementDirection,
                onDirectionChange = { statementDirection = it },
                amountRange = statementAmount,
                onAmountRangeChange = { statementAmount = it },
                onTransactionClick = { nav.navigate(Destination.TransactionDetail(it.id)) },
                onBack = { nav.popBackStack() },
            )
        }

        screen<Destination.Notifications> {
            NotificationsScreen(
                notifications = inbox,
                snackbarHostState = snackbarHostState,
                onMarkRead = { target ->
                    inbox = inbox.map { if (it.id == target.id) it.copy(read = true) else it }
                },
                onDismiss = { target ->
                    // Remove, remember, and offer it back. Restoring at the original index rather
                    // than appending keeps the list in the order the user was looking at.
                    val index = inbox.indexOfFirst { it.id == target.id }
                    inbox = inbox.filterNot { it.id == target.id }
                    lastDismissed = target
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Notification dismissed",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            lastDismissed?.let { restored ->
                                inbox = inbox.toMutableList().apply {
                                    add(index.coerceIn(0, size), restored)
                                }
                            }
                        }
                        lastDismissed = null
                    }
                },
                onBack = { nav.popBackStack() },
            )
        }

        screen<Destination.Help> {
            HelpScreen(onBack = { nav.popBackStack() })
        }

        // --- Money movement ------------------------------------------------------------------
        //
        // The rate is resolved from the chosen currency pair, not hardcoded. It used to be
        // SampleData.usdToEur for every recipient, so sending to a JPY or GBP payee reviewed in
        // euros. rateFor falls back to an identity rate for a same-currency pair and returns null
        // only for a pair the demo cannot price, which the picker already filters out.
        val transferRate = SampleData.rateFor(sourceCurrency, targetCurrency) ?: SampleData.usdToEur
        val fundingAccount = accounts.firstOrNull { it.balance.currency == sourceCurrency }

        screen<Destination.Transfer> {
            TransferScreen(
                sendAmount = sendAmount,
                onSendAmountChange = { sendAmount = it },
                rate = transferRate,
                fundingAccount = fundingAccount,
                onSourceCurrencyChange = { sourceCurrency = it },
                onTargetCurrencyChange = { targetCurrency = it },
                onSwapCurrencies = {
                    val previousSource = sourceCurrency
                    sourceCurrency = targetCurrency
                    targetCurrency = previousSource
                },
                onBack = { nav.popBackStack() },
                onContinue = { nav.navigate(Destination.TransferRecipient) },
            )
        }

        screen<Destination.TransferRecipient> {
            TransferRecipientScreen(
                sendAmount = sendAmount,
                rate = transferRate,
                // Only payees this pair can actually reach; the rest would review in the wrong
                // currency, which is the bug this flow just stopped having.
                recipients = SampleData.recipients.filter { it.currency == targetCurrency },
                selectedRecipient = selectedRecipient,
                onRecipientSelected = { selectedRecipient = it },
                onBack = { nav.popBackStack() },
                onConfirm = {
                    val recipient = selectedRecipient ?: return@TransferRecipientScreen
                    val send = sendAmount.toMoneyOrZero(transferRate.from)
                    val total = send + transferRate.feeOn(send)

                    createdCount += 1
                    // Named once and used twice: the row's id is also what the success screen's
                    // receipt link opens, and two copies of the same expression is one edit away
                    // from a receipt that points at nothing.
                    val rowId = "new-$createdCount"
                    activity = listOf(
                        outgoingTransfer(
                            id = rowId,
                            to = recipient,
                            amount = send,
                            on = SampleData.today,
                        ),
                    ) + activity
                    accounts = accounts.applyDelta(-total)

                    lastAction = CompletedAction(
                        headline = "Sent.",
                        amount = transferRate.convert(send),
                        subtitle = "to ${recipient.name}",
                        transactionId = rowId,
                    )
                    // Clear the form so re-entering Send starts fresh. It used to keep the last
                    // transfer pre-filled, which is one mistaken tap away from sending twice.
                    sendAmount = ""
                    nav.toSuccess()
                },
            )
        }

        screen<Destination.AddMoney> {
            AddMoneyScreen(
                amount = topUpAmount,
                onAmountChange = { topUpAmount = it },
                source = topUpSource,
                onSourceChange = { topUpSource = it },
                destination = primaryAccount,
                onBack = { nav.popBackStack() },
                onReceive = { nav.navigate(Destination.Receive) },
                onConfirm = {
                    val added = topUpAmount.toMoneyOrZero(primaryAccount.balance.currency)
                    createdCount += 1
                    val rowId = "new-$createdCount"
                    activity = listOf(
                        incomingTopUp(
                            id = rowId,
                            source = topUpSource.label,
                            amount = added,
                            on = SampleData.today,
                        ),
                    ) + activity
                    accounts = accounts.applyDelta(added)

                    lastAction = CompletedAction(
                        headline = "Added.",
                        amount = added,
                        subtitle = "from ${topUpSource.label}",
                        transactionId = rowId,
                    )
                    topUpAmount = ""
                    nav.toSuccess()
                },
            )
        }

        screen<Destination.Receive> {
            ReceiveScreen(
                account = primaryAccount,
                handle = "alex.morgan@forma.example",
                onBack = { nav.popBackStack() },
            )
        }

        screen<Destination.RequestMoney> {
            RequestMoneyScreen(
                amount = requestAmount,
                onAmountChange = { requestAmount = it },
                note = requestNote,
                onNoteChange = { requestNote = it },
                recipients = SampleData.recipients,
                selectedRecipient = requestFrom,
                onRecipientSelected = { requestFrom = it },
                account = primaryAccount,
                onBack = { nav.popBackStack() },
                onCreate = {
                    val from = requestFrom ?: return@RequestMoneyScreen
                    val asked = requestAmount.toMoneyOrZero(primaryAccount.balance.currency)

                    createdCount += 1
                    val rowId = "new-$createdCount"
                    // Appended but NOT applied to the balance: a request is money expected, not
                    // money held. It sits in the list as Pending until the other side pays.
                    activity = listOf(
                        pendingRequest(
                            id = rowId,
                            from = from,
                            amount = asked,
                            on = SampleData.today,
                            note = requestNote.ifBlank { null },
                        ),
                    ) + activity

                    lastAction = CompletedAction(
                        headline = "Requested.",
                        amount = asked,
                        subtitle = "from ${from.name}",
                        transactionId = rowId,
                    )
                    requestAmount = ""
                    requestNote = ""
                    requestFrom = null
                    nav.toSuccess()
                },
            )
        }

        screen<Destination.Success> {
            // Nothing to show means the screen was reached out of order — leave rather than render
            // an empty hero.
            val action = lastAction
            if (action == null) {
                LaunchedEffect(Unit) { nav.leaveFlow() }
            } else {
                // Held in a local so the smart cast survives into the lambda below.
                val receiptId = action.transactionId
                SuccessScreen(
                    headline = action.headline,
                    amount = action.amount,
                    subtitle = action.subtitle,
                    onDone = { nav.leaveFlow() },
                    // The row the flow just filed, opened as the receipt. Only the flows that
                    // filed one pass an id; cancelling a card moved no money, so it passes null
                    // and the screen omits the button rather than offering a dead one.
                    onViewReceipt = if (receiptId != null) {
                        {
                            // Replaces the success screen rather than stacking on it. Backing out
                            // of a receipt should reach the Dashboard, not a completion the user
                            // has already acknowledged and pressed past.
                            nav.navigate(Destination.TransactionDetail(receiptId)) {
                                popUpTo(Destination.Success) { inclusive = true }
                            }
                        }
                    } else {
                        null
                    },
                )
            }
        }

        composable<Destination.TransactionDetail> { entry ->
            val route = entry.toRoute<Destination.TransactionDetail>()
            val transaction = activity.firstOrNull { it.id == route.transactionId }
            if (transaction == null) {
                // The row was removed while the detail sat on the back stack. Popping beats
                // rendering a blank screen.
                LaunchedEffect(route.transactionId) { nav.popBackStack() }
            } else {
                TransactionDetailScreen(
                    transaction = transaction,
                    today = SampleData.today,
                    onBack = { nav.popBackStack() },
                    // Only a transfer can be repeated; a card purchase is not something to re-run.
                    onRepeat = if (transaction.category == SpendCategory.Transfer) {
                        {
                            sendAmount = transaction.amount.absolute.decimal.toPlainString()
                            nav.navigate(Destination.Transfer)
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

/**
 * What the shared [SuccessScreen] renders after a flow completes.
 *
 * Kept as hoisted state rather than route arguments: the alternative is serialising [Money] into the
 * route, which needs a custom `NavType` for a value that is only ever read one screen later.
 */
private data class CompletedAction(
    val headline: String,
    /** `null` when the completion moved no money — a PIN change, for instance. */
    val amount: Money?,
    val subtitle: String,
    /**
     * The activity row this completion filed, if it filed one — what "View receipt" opens.
     *
     * The id rather than the `Transaction`, for the same reason [Destination.TransactionDetail]
     * carries one: the list is rewritten underneath this value, and an id stays meaningful where a
     * copy of the row would go stale. `null` for a completion with no row behind it, which is what
     * makes the success screen omit the button instead of showing one that leads nowhere.
     */
    val transactionId: String? = null,
)

/**
 * Saves [CompletedAction] across Activity recreation.
 *
 * This is not optional polish. `navigation-compose` restores the back stack after a configuration
 * change, so `Success` comes back as the current destination — but a plain `remember` does not
 * survive, so the action came back `null` while its screen was still on screen. The guard then
 * rendered nothing and the result was a blank dark frame: rotate on the success screen and the app
 * showed an empty rectangle. Saving the four primitives keeps the screen able to redraw itself.
 *
 * A list of primitives rather than `@Parcelize`, which would mean adding the parcelize plugin for one
 * three-field class. `Money` is a `Long` plus an enum, so it flattens exactly.
 */
private val CompletedActionSaver = listSaver<CompletedAction?, Any>(
    save = { action ->
        when {
            action == null -> emptyList()
            // Three entries means "no amount"; five carries one. Encoding the absence in the length
            // avoids a placeholder value that a future reader could mistake for a real zero. The
            // receipt id carries no such hazard — a row id is never blank — so it always occupies
            // its slot and empty stands in for "no receipt".
            action.amount == null -> listOf(
                action.headline,
                action.subtitle,
                action.transactionId.orEmpty(),
            )
            else -> listOf(
                action.headline,
                action.subtitle,
                action.transactionId.orEmpty(),
                action.amount.minorUnits,
                action.amount.currency.name,
            )
        }
    },
    restore = { saved ->
        // An empty list means there was nothing to save; returning null lands on the same initial
        // value rememberSaveable would have used anyway.
        when (saved.size) {
            3 -> CompletedAction(
                headline = saved[0] as String,
                subtitle = saved[1] as String,
                amount = null,
                transactionId = (saved[2] as String).ifEmpty { null },
            )
            5 -> CompletedAction(
                headline = saved[0] as String,
                subtitle = saved[1] as String,
                amount = Money(saved[3] as Long, Currency.valueOf(saved[4] as String)),
                transactionId = (saved[2] as String).ifEmpty { null },
            )
            else -> null
        }
    },
)

/**
 * Goes to the success screen, dropping the flow that led there.
 *
 * `popUpTo(Dashboard)` leaves the completed flow off the back stack, so pressing back from the
 * success screen lands on the Dashboard rather than stepping back into a transfer that has already
 * been sent.
 */
private fun NavHostController.toSuccess() {
    navigate(Destination.Success) {
        popUpTo(Destination.Dashboard)
        launchSingleTop = true
    }
}
