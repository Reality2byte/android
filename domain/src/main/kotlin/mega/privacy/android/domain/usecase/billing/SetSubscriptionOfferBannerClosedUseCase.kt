package mega.privacy.android.domain.usecase.billing

import mega.privacy.android.domain.repository.BillingRepository
import javax.inject.Inject

/**
 * Persist that the current user has closed the subscription offer banner.
 *
 * @property billingRepository [BillingRepository]
 */
class SetSubscriptionOfferBannerClosedUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
) {
    /**
     * Invoke
     */
    suspend operator fun invoke() = billingRepository.setSubscriptionOfferBannerClosed()
}
