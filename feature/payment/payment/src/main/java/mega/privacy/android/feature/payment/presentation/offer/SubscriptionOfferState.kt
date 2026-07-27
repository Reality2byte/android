package mega.privacy.android.feature.payment.presentation.offer

import mega.privacy.android.feature.payment.model.LocalisedSubscription

/**
 * UI state for the subscription offer landing screen.
 *
 * @property isLoading true while the recommended offer is being loaded
 * @property offerSubscription the discounted plan to promote, null while loading or when no
 * higher-tier plan carries an offer
 * @property isMonthly whether the promoted offer is on the monthly billing period
 * @property offerValidUntil the offer expiry as epoch seconds, null to hide the countdown
 */
data class SubscriptionOfferState(
    val isLoading: Boolean = true,
    val offerSubscription: LocalisedSubscription? = null,
    val isMonthly: Boolean = true,
    val offerValidUntil: Long? = null,
)
