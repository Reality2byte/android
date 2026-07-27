package mega.privacy.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class PaymentDatastoreTest {

    private lateinit var underTest: PaymentDatastore

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val paymentPreferenceDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            produceFile = { context.preferencesDataStoreFile(paymentPreferenceFileName) }
        )

    @Before
    fun setup() {
        underTest = PaymentDatastore(paymentPreferenceDataStore = paymentPreferenceDataStore)
    }

    @Test
    fun `test that the subscription offer banner is not closed by default`() = runTest {
        underTest.monitorSubscriptionOfferBannerClosed(userHandle).test {
            assertThat(expectMostRecentItem()).isFalse()
        }
    }

    @Test
    fun `test that the subscription offer banner is closed after it is set`() = runTest {
        underTest.setSubscriptionOfferBannerClosed(userHandle = userHandle, closed = true)

        underTest.monitorSubscriptionOfferBannerClosed(userHandle).test {
            assertThat(expectMostRecentItem()).isTrue()
        }
    }

    @Test
    fun `test that the subscription offer banner is not closed after it is reset`() = runTest {
        underTest.setSubscriptionOfferBannerClosed(userHandle = userHandle, closed = true)
        underTest.setSubscriptionOfferBannerClosed(userHandle = userHandle, closed = false)

        underTest.monitorSubscriptionOfferBannerClosed(userHandle).test {
            assertThat(expectMostRecentItem()).isFalse()
        }
    }

    @Test
    fun `test that closing the subscription offer banner does not close it for another user`() =
        runTest {
            underTest.setSubscriptionOfferBannerClosed(userHandle = userHandle, closed = true)

            underTest.monitorSubscriptionOfferBannerClosed(otherUserHandle).test {
                assertThat(expectMostRecentItem()).isFalse()
            }
            underTest.monitorSubscriptionOfferBannerClosed(userHandle).test {
                assertThat(expectMostRecentItem()).isTrue()
            }
        }

    @Test
    fun `test that the subscription offer menu banner is not closed by default`() = runTest {
        underTest.monitorSubscriptionOfferMenuBannerClosed(userHandle).test {
            assertThat(expectMostRecentItem()).isFalse()
        }
    }

    @Test
    fun `test that the subscription offer menu banner is closed after it is set`() = runTest {
        underTest.setSubscriptionOfferMenuBannerClosed(userHandle = userHandle, closed = true)

        underTest.monitorSubscriptionOfferMenuBannerClosed(userHandle).test {
            assertThat(expectMostRecentItem()).isTrue()
        }
    }

    @Test
    fun `test that closing the subscription offer menu banner does not close the home banner`() =
        runTest {
            underTest.setSubscriptionOfferMenuBannerClosed(userHandle = userHandle, closed = true)

            underTest.monitorSubscriptionOfferBannerClosed(userHandle).test {
                assertThat(expectMostRecentItem()).isFalse()
            }
            underTest.monitorSubscriptionOfferMenuBannerClosed(userHandle).test {
                assertThat(expectMostRecentItem()).isTrue()
            }
        }

    @Test
    fun `test that closing the subscription offer home banner does not close the menu banner`() =
        runTest {
            underTest.setSubscriptionOfferBannerClosed(userHandle = userHandle, closed = true)

            underTest.monitorSubscriptionOfferMenuBannerClosed(userHandle).test {
                assertThat(expectMostRecentItem()).isFalse()
            }
            underTest.monitorSubscriptionOfferBannerClosed(userHandle).test {
                assertThat(expectMostRecentItem()).isTrue()
            }
        }

    @Test
    fun `test that closing the subscription offer menu banner does not close it for another user`() =
        runTest {
            underTest.setSubscriptionOfferMenuBannerClosed(userHandle = userHandle, closed = true)

            underTest.monitorSubscriptionOfferMenuBannerClosed(otherUserHandle).test {
                assertThat(expectMostRecentItem()).isFalse()
            }
            underTest.monitorSubscriptionOfferMenuBannerClosed(userHandle).test {
                assertThat(expectMostRecentItem()).isTrue()
            }
        }

    private companion object {
        const val userHandle = 123L
        const val otherUserHandle = 456L
    }
}
