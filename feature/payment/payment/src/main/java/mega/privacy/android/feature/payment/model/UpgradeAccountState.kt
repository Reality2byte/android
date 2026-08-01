package mega.privacy.android.feature.payment.model

import mega.privacy.android.domain.entity.AccountSubscriptionCycle
import mega.privacy.android.domain.entity.AccountType
import mega.privacy.android.domain.entity.SubscriptionStatus
import mega.privacy.android.domain.entity.agesignal.UserAgeComplianceStatus
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

/**
 * Upgrade Account state
 *
 * @property localisedSubscriptionsList list of all monthly localised subscriptions available on app, default empty
 * @property product list of Product subscriptions
 * @property cheapestSubscriptionAvailable cheapest subscription, which is available for user (Pro Lite or Pro I)
 * @property currentSubscriptionPlan current subscribed plan, default Free plan
 * @property subscriptionCycle current subscription cycle (monthly/yearly), default UNKNOWN
 * @property subscriptionStatus current subscription status (VALID/INVALID/NONE), null if unknown
 * @property currentPlanRenewal how the current plan renews, resolved from its subscription id, null
 * while unresolved (before account details arrive, or when the account reports no Pro plan)
 * @property subscriptionRenewTime renewal timestamp of the current subscription in seconds, null if unknown
 * @property proExpirationTime expiration timestamp of the current Pro plan in seconds, null if unknown
 * @property proPlanStartTime start timestamp of the current Pro plan in seconds (uq "ts" field), null if unknown
 * @property isCurrentPlanExpiring whether the current plan expires within the next 30 days, driving the
 * "Expiring" badge on the current plan card
 * @property offerValidUntil expiry timestamp of the active discount offer in seconds (utqa "mo.e"), null when
 * there is no time-limited discount offer. Drives the revamp offer-header countdown.
 * @constructor Create default Upgrade Account state
 */
data class UpgradeAccountState(
    val localisedSubscriptionsList: List<LocalisedSubscription> = emptyList(),
    val cheapestSubscriptionAvailable: LocalisedSubscription? = null,
    val currentSubscriptionPlan: AccountType? = null,
    val subscriptionCycle: AccountSubscriptionCycle = AccountSubscriptionCycle.UNKNOWN,
    val subscriptionStatus: SubscriptionStatus? = null,
    val currentPlanRenewal: CurrentPlanRenewal? = null,
    val subscriptionRenewTime: Long? = null,
    val proExpirationTime: Long? = null,
    val proPlanStartTime: Long? = null,
    val isCurrentPlanExpiring: Boolean = false,
    val userAgeComplianceStatus: UserAgeComplianceStatus = UserAgeComplianceStatus.AdultVerified,
    val isSubscriptionFeatureAvailable: Boolean? = null,
    val offerValidUntil: Long? = null,
) {
    /**
     * Whether the current subscription is an active recurring subscription that renews
     * (as opposed to a one-off purchase / cancelled subscription that expires).
     */
    val isCurrentSubscriptionRenewing: Boolean
        get() = currentPlanRenewal.isRenewing(subscriptionStatus)

    /**
     * Period of a one-off (non-recurring) plan, derived from its start and expiry timestamps and
     * expressed in the largest unit that fits its duration, or null when the plan is not a one-off
     * purchase or either timestamp is unavailable.
     *
     * Restricted to one-off plans because only they have a single billing period: a plan that has
     * renewed spans several, so the arithmetic below would report their sum (e.g. "36 months" for a
     * three-year-old yearly plan).
     *
     * Whole months are counted with calendar arithmetic (their length varies by month and leap year),
     * mirroring how the backend derives the expiry by adding calendar months to the start; shorter
     * periods use fixed-length days/hours/minutes.
     */
    val currentPlanPeriod: PlanPeriod?
        get() {
            if (currentPlanRenewal != CurrentPlanRenewal.OneOff) return null
            val start = proPlanStartTime ?: return null
            val end = proExpirationTime ?: return null
            val duration = (end - start).seconds
            val months = ChronoUnit.MONTHS.between(
                Instant.ofEpochSecond(start).atZone(ZoneOffset.UTC),
                Instant.ofEpochSecond(end).atZone(ZoneOffset.UTC),
            )
            return when {
                months >= 1 -> PlanPeriod(months.toInt(), ChronoUnit.MONTHS)
                duration.inWholeDays >= 1 -> PlanPeriod(
                    duration.inWholeDays.toInt(),
                    ChronoUnit.DAYS
                )

                duration.inWholeHours >= 1 -> PlanPeriod(
                    duration.inWholeHours.toInt(),
                    ChronoUnit.HOURS
                )

                else -> PlanPeriod(duration.inWholeMinutes.toInt(), ChronoUnit.MINUTES)
            }
        }

    /**
     * Is PRO_III plan
     */
    fun isHighestPlan(): Boolean {
        return currentSubscriptionPlan == AccountType.PRO_III
    }

    // checking if there is any discount available it's different from current plan
    fun hasDiscount() = localisedSubscriptionsList.any {
        when (subscriptionCycle) {
            AccountSubscriptionCycle.MONTHLY ->
                it.yearlySubscription?.discountedAmountMonthly != null && it.accountType != currentSubscriptionPlan

            AccountSubscriptionCycle.YEARLY ->
                it.monthlySubscription?.discountedAmountMonthly != null && it.accountType != currentSubscriptionPlan

            else -> (it.monthlySubscription?.discountedAmountMonthly != null ||
                    it.yearlySubscription?.discountedAmountMonthly != null)
                    && it.accountType != currentSubscriptionPlan
        }
    }
}
