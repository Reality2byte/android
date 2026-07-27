package mega.privacy.android.data.gateway.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Gateway for payment related preferences persistence.
 *
 * Preferences are scoped per account, so every entry point takes the handle of the logged in user.
 */
interface PaymentPreferencesGateway {

    /**
     * Monitor whether the given user has closed the subscription offer banner.
     *
     * @param userHandle the handle of the logged in user
     */
    fun monitorSubscriptionOfferBannerClosed(userHandle: Long): Flow<Boolean>

    /**
     * Set whether the given user has closed the subscription offer banner.
     *
     * @param userHandle the handle of the logged in user
     * @param closed true when the banner should stay hidden for this user
     */
    suspend fun setSubscriptionOfferBannerClosed(userHandle: Long, closed: Boolean)

    /**
     * Monitor whether the given user has closed the subscription offer banner on the Menu screen.
     *
     * Tracked separately from [monitorSubscriptionOfferBannerClosed] so dismissing the banner on
     * one surface leaves it visible on the other.
     *
     * @param userHandle the handle of the logged in user
     */
    fun monitorSubscriptionOfferMenuBannerClosed(userHandle: Long): Flow<Boolean>

    /**
     * Set whether the given user has closed the subscription offer banner on the Menu screen.
     *
     * @param userHandle the handle of the logged in user
     * @param closed true when the Menu banner should stay hidden for this user
     */
    suspend fun setSubscriptionOfferMenuBannerClosed(userHandle: Long, closed: Boolean)

    /**
     * Get when the subscription offer screen was last shown to the given user.
     *
     * @param userHandle the handle of the logged in user
     * @return the time in milliseconds, or null when the screen has never been shown
     */
    suspend fun getSubscriptionOfferLastShownTime(userHandle: Long): Long?

    /**
     * Set when the subscription offer screen was last shown to the given user.
     *
     * @param userHandle the handle of the logged in user
     * @param timeInMillis the time the screen was shown
     */
    suspend fun setSubscriptionOfferLastShownTime(userHandle: Long, timeInMillis: Long)
}
