package mega.privacy.android.feature.payment.presentation.offer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.entity.Subscription
import mega.privacy.android.domain.entity.account.Skus
import mega.privacy.android.domain.entity.billing.RecommendedSubscriptionOffer
import mega.privacy.android.domain.usecase.billing.GetRecommendedSubscriptionWithOfferUseCase
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.feature.payment.model.LocalisedSubscription
import mega.privacy.android.feature.payment.model.mapper.LocalisedSubscriptionMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

@ExtendWith(CoroutineMainDispatcherExtension::class)
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscriptionOfferViewModelTest {

    private lateinit var underTest: SubscriptionOfferViewModel

    private val getRecommendedSubscriptionWithOfferUseCase =
        mock<GetRecommendedSubscriptionWithOfferUseCase>()
    private val monitorConnectivityUseCase = mock<MonitorConnectivityUseCase>()
    private val localisedSubscriptionMapper = mock<LocalisedSubscriptionMapper>()
    private val localisedSubscription = mock<LocalisedSubscription>()

    @BeforeEach
    fun setUp() {
        reset(
            getRecommendedSubscriptionWithOfferUseCase,
            monitorConnectivityUseCase,
            localisedSubscriptionMapper,
        )
        whenever(localisedSubscriptionMapper(anyOrNull(), anyOrNull()))
            .thenReturn(localisedSubscription)
        stubConnectivity(true)
    }

    private fun stubConnectivity(isConnected: Boolean) {
        whenever(monitorConnectivityUseCase()).thenReturn(flowOf(isConnected))
    }

    private fun initViewModel() {
        underTest = SubscriptionOfferViewModel(
            getRecommendedSubscriptionWithOfferUseCase = getRecommendedSubscriptionWithOfferUseCase,
            monitorConnectivityUseCase = monitorConnectivityUseCase,
            localisedSubscriptionMapper = localisedSubscriptionMapper,
        )
    }

    /**
     * Stubs the recommended offer with a subscription on [sku], returning the promoted
     * [Subscription] so the test can assert which billing period it was mapped on.
     */
    private fun stubOffer(sku: String, hasMultipleOffers: Boolean = false): Subscription {
        val subscription = mock<Subscription> {
            on { this.sku } doReturn sku
            on { offerValidUntil } doReturn OFFER_VALID_UNTIL
        }
        val offer = mock<RecommendedSubscriptionOffer> {
            on { this.subscription } doReturn subscription
            on { this.hasMultipleOffers } doReturn hasMultipleOffers
        }
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }.thenReturn(offer)
        return subscription
    }

    @Test
    fun `test that init exposes a monthly offer on the monthly billing period`() = runTest {
        val subscription = stubOffer(Skus.SKU_PRO_I_MONTH)
        initViewModel()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.isMonthly).isTrue()
            assertThat(state.offerSubscription).isEqualTo(localisedSubscription)
            assertThat(state.offerValidUntil).isEqualTo(OFFER_VALID_UNTIL)
            assertThat(state.hasMultipleOffers).isFalse()
        }
        verify(localisedSubscriptionMapper).invoke(
            monthlySubscription = subscription,
            yearlySubscription = null,
        )
    }

    @Test
    fun `test that init exposes a yearly offer on the yearly billing period`() = runTest {
        val subscription = stubOffer(Skus.SKU_PRO_I_YEAR)
        initViewModel()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.isMonthly).isFalse()
            assertThat(state.offerSubscription).isEqualTo(localisedSubscription)
        }
        verify(localisedSubscriptionMapper).invoke(
            monthlySubscription = null,
            yearlySubscription = subscription,
        )
    }

    @Test
    fun `test that init flags multiple offers when the campaign discounts several plans`() =
        runTest {
            stubOffer(Skus.SKU_PRO_I_MONTH, hasMultipleOffers = true)
            initViewModel()

            underTest.state.test {
                assertThat(awaitItem().hasMultipleOffers).isTrue()
            }
        }

    @Test
    fun `test that init exposes no offer when there is no recommended subscription`() = runTest {
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }.thenReturn(null)
        initViewModel()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.offerSubscription).isNull()
            assertThat(state.hasMultipleOffers).isFalse()
        }
    }

    @Test
    fun `test that init does not report a load error when there is no recommended subscription`() =
        runTest {
            wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }.thenReturn(null)
            initViewModel()

            underTest.state.test {
                assertThat(awaitItem().hasLoadError).isFalse()
            }
        }

    @Test
    fun `test that init exposes no offer when loading fails`() = runTest {
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }
            .thenThrow(RuntimeException("boom"))
        initViewModel()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.offerSubscription).isNull()
        }
    }

    @Test
    fun `test that init reports a load error when loading fails`() = runTest {
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }
            .thenThrow(RuntimeException("boom"))
        initViewModel()

        underTest.state.test {
            assertThat(awaitItem().hasLoadError).isTrue()
        }
    }

    @Test
    fun `test that init exposes the connectivity state when the device is offline`() = runTest {
        stubConnectivity(false)
        stubOffer(Skus.SKU_PRO_I_MONTH)
        initViewModel()

        underTest.state.test {
            assertThat(awaitItem().isConnected).isFalse()
        }
    }

    @Test
    fun `test that onRetry exposes the offer when loading succeeds`() = runTest {
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }
            .thenThrow(RuntimeException("boom"))
        initViewModel()
        advanceUntilIdle()
        assertThat(underTest.state.value.hasLoadError).isTrue()

        reset(getRecommendedSubscriptionWithOfferUseCase)
        stubOffer(Skus.SKU_PRO_I_MONTH)
        underTest.onRetry()
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.hasLoadError).isFalse()
            assertThat(state.isLoading).isFalse()
            assertThat(state.offerSubscription).isEqualTo(localisedSubscription)
        }
    }

    @Test
    fun `test that onRetry keeps the load error when loading fails again`() = runTest {
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }
            .thenThrow(RuntimeException("boom"))
        initViewModel()
        advanceUntilIdle()

        underTest.onRetry()
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.hasLoadError).isTrue()
            assertThat(state.isLoading).isFalse()
        }
    }

    private companion object {
        const val OFFER_VALID_UNTIL = 1_800_000_000L
    }
}
