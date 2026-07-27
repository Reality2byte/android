package mega.privacy.android.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
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

    private fun subscriptionOfferBannerClosedKey(userHandle: Long) =
        booleanPreferencesKey("${userHandle}_$SUBSCRIPTION_OFFER_BANNER_CLOSED")

    private fun subscriptionOfferMenuBannerClosedKey(userHandle: Long) =
        booleanPreferencesKey("${userHandle}_$SUBSCRIPTION_OFFER_MENU_BANNER_CLOSED")

    companion object {
        private const val SUBSCRIPTION_OFFER_BANNER_CLOSED = "SUBSCRIPTION_OFFER_BANNER_CLOSED"
        private const val SUBSCRIPTION_OFFER_MENU_BANNER_CLOSED =
            "SUBSCRIPTION_OFFER_MENU_BANNER_CLOSED"
    }
}
