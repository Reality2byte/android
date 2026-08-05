package mega.privacy.android.feature.payment.presentation.offer

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mega.android.core.ui.extensions.LaunchedOnceEffect
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.domain.entity.billing.BillingEvent
import mega.privacy.android.domain.entity.payment.UpgradeSource
import mega.privacy.android.feature.payment.presentation.billing.BillingViewModel
import mega.privacy.android.navigation.payment.SubscriptionOfferSource

/**
 * Route for the subscription offer landing screen. Wires the [SubscriptionOfferViewModel] state
 * and the Google Play purchase flow via [BillingViewModel], and closes the screen when the offer
 * is unavailable or the purchase completes.
 *
 * @param source how the screen was opened; selects which analytics events are reported
 * @param onBack closes the screen
 * @param onViewAllPlans opens the full list of plans, shown when several plans carry the campaign
 */
@Composable
internal fun SubscriptionOfferRoute(
    source: SubscriptionOfferSource,
    onBack: () -> Unit,
    onViewAllPlans: () -> Unit,
    viewModel: SubscriptionOfferViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val events = remember(source) { subscriptionOfferEvents(source) }
    val isOfferShown = !uiState.isLoading && uiState.offerSubscription != null

    BackHandler(onBack = onBack)

    LaunchedOnceEffect(isOfferShown) {
        if (isOfferShown) {
            Analytics.tracker.trackEvent(events.screenView)
        }
    }

    LaunchedEffect(Unit) {
        billingViewModel.billingUpdateEvent.collect {
            if (it is BillingEvent.OnPurchaseUpdate) {
                billingViewModel.markHandleBillingEvent()
                onBack()
            }
        }
    }

    LaunchedEffect(
        uiState.isLoading,
        uiState.offerSubscription,
        uiState.isConnected,
        uiState.hasLoadError,
    ) {
        val canShowError = !uiState.isConnected || uiState.hasLoadError
        if (!uiState.isLoading && uiState.offerSubscription == null && !canShowError) {
            onBack()
        }
    }

    SubscriptionOfferScreen(
        uiState = uiState,
        onBuyClick = { subscription ->
            Analytics.tracker.trackEvent(events.ctaPressed)
            activity?.let {
                billingViewModel.startPurchase(
                    activity = it,
                    subscription = subscription,
                    source = UpgradeSource.Main,
                )
            }
        },
        onDismiss = {
            Analytics.tracker.trackEvent(events.dismissPressed)
            onBack()
        },
        onViewAllPlansClick = {
            Analytics.tracker.trackEvent(events.viewAllPlansPressed)
            onViewAllPlans()
        },
        onRetryClick = viewModel::onRetry,
    )
}
