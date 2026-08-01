package mega.privacy.android.feature.payment.model

import mega.privacy.android.domain.entity.SubscriptionStatus

/**
 * How the user's current Pro plan renews, resolved by matching the plan's subscription id ("subid")
 * against the subscriptions the account still holds.
 */
enum class CurrentPlanRenewal {
    /**
     * The plan is held as an active recurring subscription and renews automatically.
     */
    Renewing,

    /**
     * The plan came from a recurring subscription that has since been cancelled. It stays valid
     * until it expires, but its billing cycle is no longer reported by the API.
     */
    Cancelled,

    /**
     * The plan is a one-off (non-recurring) purchase that expires without renewing.
     */
    OneOff,
}

/**
 * Whether the plan renews, falling back to the account-wide [subscriptionStatus] when the plan's own
 * renewal is unresolved (null) — either account details have not arrived yet, or the account reports
 * no Pro plan to match against its subscriptions. That status cannot tell a cancelled subscription
 * from a one-off purchase, but it does still say whether anything is renewing.
 *
 * @param subscriptionStatus the account-wide subscription status, used only for that fallback
 */
fun CurrentPlanRenewal?.isRenewing(subscriptionStatus: SubscriptionStatus?): Boolean = when (this) {
    CurrentPlanRenewal.Renewing -> true
    CurrentPlanRenewal.Cancelled, CurrentPlanRenewal.OneOff -> false
    null -> subscriptionStatus == SubscriptionStatus.VALID
}
