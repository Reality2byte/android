package mega.privacy.android.feature.payment.presentation.offer

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mega.privacy.android.domain.entity.billing.BillingEvent
import mega.privacy.android.domain.entity.payment.UpgradeSource
import mega.privacy.android.feature.payment.presentation.billing.BillingViewModel

/**
 * Route for the subscription offer landing screen. Wires the [SubscriptionOfferViewModel] state
 * and the Google Play purchase flow via [BillingViewModel], and closes the screen when the offer
 * is unavailable or the purchase completes.
 *
 * @param onBack closes the screen
 * @param onViewAllPlans opens the full list of plans, shown when several plans carry the campaign
 */
@Composable
internal fun SubscriptionOfferRoute(
    onBack: () -> Unit,
    onViewAllPlans: () -> Unit,
    viewModel: SubscriptionOfferViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    BackHandler(onBack = onBack)

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
            activity?.let {
                billingViewModel.startPurchase(
                    activity = it,
                    subscription = subscription,
                    source = UpgradeSource.Main,
                )
            }
        },
        onDismiss = onBack,
        onViewAllPlansClick = onViewAllPlans,
        onRetryClick = viewModel::onRetry,
    )
}
