package mega.privacy.android.domain.usecase.billing

import mega.privacy.android.domain.repository.BillingRepository
import javax.inject.Inject

/**
 * Persist that the current user has closed the subscription offer banner on the Menu screen.
 *
 * Tracked separately from [SetSubscriptionOfferBannerClosedUseCase], so dismissing the banner on
 * the Menu screen leaves it visible on the Home carousel.
 *
 * @property billingRepository [BillingRepository]
 */
class SetSubscriptionOfferMenuBannerClosedUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
) {
    /**
     * Invoke
     */
    suspend operator fun invoke() = billingRepository.setSubscriptionOfferMenuBannerClosed()
}
