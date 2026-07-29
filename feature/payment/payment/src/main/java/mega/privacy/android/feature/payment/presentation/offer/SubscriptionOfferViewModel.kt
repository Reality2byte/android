package mega.privacy.android.feature.payment.presentation.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.domain.usecase.billing.GetRecommendedSubscriptionWithOfferUseCase
import mega.privacy.android.feature.payment.model.mapper.LocalisedSubscriptionMapper
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the subscription offer landing screen. Loads the cheapest higher-tier plan that
 * carries an active mobile offer and exposes it as a [mega.privacy.android.feature.payment.model.LocalisedSubscription]
 * on the billing period the offer applies to, flagging whether the campaign discounts other plans too.
 */
@HiltViewModel
class SubscriptionOfferViewModel @Inject constructor(
    private val getRecommendedSubscriptionWithOfferUseCase: GetRecommendedSubscriptionWithOfferUseCase,
    private val localisedSubscriptionMapper: LocalisedSubscriptionMapper,
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionOfferState())

    /**
     * The current UI state.
     */
    val state = _state.asStateFlow()

    init {
        loadOffer()
    }

    private fun loadOffer() {
        viewModelScope.launch {
            val offer = runCatching { getRecommendedSubscriptionWithOfferUseCase() }
                .onFailure { Timber.e(it, "Failed to load the recommended offer") }
                .getOrNull()
            if (offer == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val subscription = offer.subscription
            val isMonthly = subscription.sku.endsWith(MONTHLY_SKU_SUFFIX)
            _state.update {
                it.copy(
                    isLoading = false,
                    offerSubscription = localisedSubscriptionMapper(
                        monthlySubscription = subscription.takeIf { isMonthly },
                        yearlySubscription = subscription.takeUnless { isMonthly },
                    ),
                    isMonthly = isMonthly,
                    offerValidUntil = subscription.offerValidUntil,
                    hasMultipleOffers = offer.hasMultipleOffers,
                )
            }
        }
    }

    private companion object {
        private const val MONTHLY_SKU_SUFFIX = ".onemonth"
    }
}
