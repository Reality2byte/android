package mega.privacy.android.domain.usecase.billing

import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.repository.BillingRepository
import javax.inject.Inject

/**
 * Monitor whether the current user has closed the subscription offer banner.
 *
 * @property billingRepository [BillingRepository]
 */
class MonitorSubscriptionOfferBannerClosedUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
) {
    /**
     * Invoke
     *
     * @return [Flow] emitting true while the banner should stay hidden for the current user
     */
    operator fun invoke(): Flow<Boolean> = billingRepository.monitorSubscriptionOfferBannerClosed()
}
