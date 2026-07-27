package mega.privacy.android.domain.usecase.billing

import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.repository.BillingRepository
import javax.inject.Inject

/**
 * Monitor whether the current user has closed the subscription offer banner on the Menu screen.
 *
 * Tracked separately from [MonitorSubscriptionOfferBannerClosedUseCase], so dismissing the banner
 * on the Home carousel leaves it visible on the Menu screen and vice versa.
 *
 * @property billingRepository [BillingRepository]
 */
class MonitorSubscriptionOfferMenuBannerClosedUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
) {
    /**
     * Invoke
     *
     * @return [Flow] emitting true while the Menu banner should stay hidden for the current user
     */
    operator fun invoke(): Flow<Boolean> =
        billingRepository.monitorSubscriptionOfferMenuBannerClosed()
}
