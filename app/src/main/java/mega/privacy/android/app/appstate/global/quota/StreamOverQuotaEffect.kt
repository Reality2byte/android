package mega.privacy.android.app.appstate.global.quota

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.navigation.contract.NavigationHandler
import mega.privacy.android.navigation.contract.featureflag.FeatureFlagGate
import mega.privacy.android.navigation.contract.navOptions
import mega.privacy.android.navigation.destination.QuotaWarningUpgradeNavKey
import mega.privacy.android.navigation.payment.QuotaWarningTrigger
import mega.privacy.android.navigation.payment.QuotaWarningType

/**
 * Shows the transfer quota-warning upsell screen when streaming hits bandwidth over quota. With the
 * upsell screen disabled the legacy over quota dialog shows the warning instead.
 *
 * Streaming can be started from any activity hosting its own back stack, so each host reacts only
 * while resumed and the foreground one handles the warning in its own back stack.
 */
@Composable
fun StreamOverQuotaEffect(navigationHandler: NavigationHandler) {
    FeatureFlagGate(feature = ApiFeatures.QuotaWarningUpsellScreen) {
        val viewModel = hiltViewModel<StreamOverQuotaViewModel>()
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(navigationHandler, lifecycle) {
            viewModel.streamOverQuotaEvents
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collect {
                    viewModel.consumeStreamOverQuotaEvent()?.let {
                        navigationHandler.navigate(
                            QuotaWarningUpgradeNavKey(
                                type = QuotaWarningType.Transfer,
                                trigger = QuotaWarningTrigger.Streaming,
                            ),
                            navOptions { dropIfAlreadyShown = true },
                        )
                    }
                }
        }
    }
}
