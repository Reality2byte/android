package mega.privacy.android.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mega.privacy.android.data.gateway.preferences.PaymentPreferencesGateway
import mega.privacy.android.data.qualifier.PaymentPreference
import javax.inject.Inject

internal const val paymentPreferenceFileName = "PAYMENT_PREFERENCES"

/**
 * Datastore holding payment related preferences.
 */
internal class PaymentDatastore @Inject constructor(
    @PaymentPreference private val paymentPreferenceDataStore: DataStore<Preferences>,
) : PaymentPreferencesGateway {

    override fun monitorSubscriptionOfferBannerClosed(userHandle: Long): Flow<Boolean> =
        paymentPreferenceDataStore.data.map {
            it[subscriptionOfferBannerClosedKey(userHandle)] == true
        }

    override suspend fun setSubscriptionOfferBannerClosed(userHandle: Long, closed: Boolean) {
        paymentPreferenceDataStore.edit {
            it[subscriptionOfferBannerClosedKey(userHandle)] = closed
        }
    }

    override fun monitorSubscriptionOfferMenuBannerClosed(userHandle: Long): Flow<Boolean> =
        paymentPreferenceDataStore.data.map {
            it[subscriptionOfferMenuBannerClosedKey(userHandle)] == true
        }

    override suspend fun setSubscriptionOfferMenuBannerClosed(userHandle: Long, closed: Boolean) {
        paymentPreferenceDataStore.edit {
            it[subscriptionOfferMenuBannerClosedKey(userHandle)] = closed
        }
    }

    override suspend fun getSubscriptionOfferLastShownTime(userHandle: Long): Long? =
        paymentPreferenceDataStore.data.map {
            it[subscriptionOfferLastShownTimeKey(userHandle)]
        }.first()

    override suspend fun setSubscriptionOfferLastShownTime(userHandle: Long, timeInMillis: Long) {
        paymentPreferenceDataStore.edit {
            it[subscriptionOfferLastShownTimeKey(userHandle)] = timeInMillis
        }
    }

    private fun subscriptionOfferBannerClosedKey(userHandle: Long) =
        booleanPreferencesKey("${userHandle}_$SUBSCRIPTION_OFFER_BANNER_CLOSED")

    private fun subscriptionOfferMenuBannerClosedKey(userHandle: Long) =
        booleanPreferencesKey("${userHandle}_$SUBSCRIPTION_OFFER_MENU_BANNER_CLOSED")

    private fun subscriptionOfferLastShownTimeKey(userHandle: Long) =
        longPreferencesKey("${userHandle}_$SUBSCRIPTION_OFFER_LAST_SHOWN_TIME")

    companion object {
        private const val SUBSCRIPTION_OFFER_BANNER_CLOSED = "SUBSCRIPTION_OFFER_BANNER_CLOSED"
        private const val SUBSCRIPTION_OFFER_MENU_BANNER_CLOSED =
            "SUBSCRIPTION_OFFER_MENU_BANNER_CLOSED"
        private const val SUBSCRIPTION_OFFER_LAST_SHOWN_TIME = "SUBSCRIPTION_OFFER_LAST_SHOWN_TIME"
    }
}
