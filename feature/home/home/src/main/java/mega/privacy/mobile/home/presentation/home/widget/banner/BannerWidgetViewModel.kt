package mega.privacy.mobile.home.presentation.home.widget.banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.domain.usecase.banner.DismissBannerUseCase
import mega.privacy.android.domain.usecase.banner.GetPromoBannersUseCase
import mega.privacy.android.domain.usecase.billing.GetRecommendedSubscriptionWithOfferUseCase
import mega.privacy.android.domain.usecase.billing.MonitorSubscriptionOfferBannerClosedUseCase
import mega.privacy.android.domain.usecase.billing.SetSubscriptionOfferBannerClosedUseCase
import mega.privacy.mobile.home.presentation.home.widget.banner.mapper.SubscriptionOfferBannerMapper
import mega.privacy.mobile.home.presentation.home.widget.banner.mapper.SubscriptionOfferBannerMapper.Companion.SUBSCRIPTION_OFFER_BANNER_ID
import mega.privacy.mobile.home.presentation.home.widget.banner.model.BannerUiState
import mega.privacy.mobile.home.presentation.home.widget.banner.model.SubscriptionOfferBannerUiModel
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for banner widget
 */
@HiltViewModel
class BannerWidgetViewModel @Inject constructor(
    private val getPromoBannersUseCase: GetPromoBannersUseCase,
    private val dismissBannerUseCase: DismissBannerUseCase,
    private val getRecommendedSubscriptionWithOfferUseCase: GetRecommendedSubscriptionWithOfferUseCase,
    private val monitorSubscriptionOfferBannerClosedUseCase: MonitorSubscriptionOfferBannerClosedUseCase,
    private val setSubscriptionOfferBannerClosedUseCase: SetSubscriptionOfferBannerClosedUseCase,
    private val subscriptionOfferBannerMapper: SubscriptionOfferBannerMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BannerUiState())
    val uiState: StateFlow<BannerUiState> = _uiState.asStateFlow()

    init {
        loadBanners()
    }

    /**
     * Load banners. The subscription offer banner is not delivered by the banners API, so it is
     * built locally from the active subscription offer and shown ahead of the promo banners.
     */
    private fun loadBanners() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            coroutineScope {
                val offerBannerDeferred = async {
                    runCatching { getSubscriptionOfferBanner() }
                        .onFailure { Timber.e(it, "Failed to load subscription offer banner") }
                        .getOrNull()
                }
                val promoBannersDeferred = async {
                    runCatching { getPromoBannersUseCase() }
                        .onFailure { Timber.e(it, "Failed to load banners") }
                        .getOrDefault(emptyList())
                }
                _uiState.update {
                    it.copy(
                        offerBanner = offerBannerDeferred.await(),
                        banners = promoBannersDeferred.await(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun getSubscriptionOfferBanner(): SubscriptionOfferBannerUiModel? {
        if (monitorSubscriptionOfferBannerClosedUseCase().first()) return null
        return getRecommendedSubscriptionWithOfferUseCase()
            ?.let { subscriptionOfferBannerMapper(it, Locale.getDefault()) }
    }

    /**
     * Dismiss a banner. The subscription offer banner only exists locally, so instead of calling the
     * banners API its dismissal is persisted per account, keeping it hidden on the next app launch.
     *
     * @param bannerId The ID of the banner to dismiss
     */
    fun dismissBanner(bannerId: Int) {
        if (bannerId == SUBSCRIPTION_OFFER_BANNER_ID) {
            viewModelScope.launch {
                runCatching {
                    setSubscriptionOfferBannerClosedUseCase()
                }.onFailure { exception ->
                    Timber.e(exception, "Failed to persist subscription offer banner dismissal")
                }
                _uiState.update { it.copy(offerBanner = null) }
            }
        } else {
            viewModelScope.launch {
                runCatching {
                    dismissBannerUseCase(bannerId)
                }.onSuccess {
                    removeBanner(bannerId)
                }.onFailure { exception ->
                    Timber.e(exception, "Failed to dismiss banner $bannerId")
                }
            }
        }
    }

    private fun removeBanner(bannerId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                banners = currentState.banners.filter { it.id != bannerId }
            )
        }
    }
}
