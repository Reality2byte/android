package mega.privacy.android.feature.payment.presentation.offer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mega.privacy.android.core.formatter.mapper.FormattedSizeMapper
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.entity.AccountType
import mega.privacy.android.domain.entity.Currency
import mega.privacy.android.domain.entity.Subscription
import mega.privacy.android.domain.entity.account.CurrencyAmount
import mega.privacy.android.domain.entity.account.Skus
import mega.privacy.android.domain.usecase.billing.GetRecommendedSubscriptionWithOfferUseCase
import mega.privacy.android.feature.payment.model.mapper.LocalisedPriceCurrencyCodeStringMapper
import mega.privacy.android.feature.payment.model.mapper.LocalisedSubscriptionMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.wheneverBlocking

@ExtendWith(CoroutineMainDispatcherExtension::class)
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscriptionOfferViewModelTest {

    private lateinit var underTest: SubscriptionOfferViewModel

    private val getRecommendedSubscriptionWithOfferUseCase =
        mock<GetRecommendedSubscriptionWithOfferUseCase>()
    private val localisedPriceCurrencyCodeStringMapper =
        mock<LocalisedPriceCurrencyCodeStringMapper>()
    private val formattedSizeMapper = mock<FormattedSizeMapper>()
    private val localisedSubscriptionMapper =
        LocalisedSubscriptionMapper(localisedPriceCurrencyCodeStringMapper, formattedSizeMapper)

    @BeforeEach
    fun setUp() {
        reset(
            getRecommendedSubscriptionWithOfferUseCase,
            localisedPriceCurrencyCodeStringMapper,
            formattedSizeMapper,
        )
    }

    private fun initViewModel() {
        underTest = SubscriptionOfferViewModel(
            getRecommendedSubscriptionWithOfferUseCase = getRecommendedSubscriptionWithOfferUseCase,
            localisedSubscriptionMapper = localisedSubscriptionMapper,
        )
    }

    private fun subscription(sku: String) = Subscription(
        sku = sku,
        accountType = AccountType.PRO_I,
        handle = 1L,
        storage = 2048,
        transfer = 2048,
        amount = CurrencyAmount(9.99F, Currency("EUR")),
        discountedAmountMonthly = CurrencyAmount(4.99F, Currency("EUR")),
        discountedPercentage = 50,
        discountName = "Black Friday",
        offerValidUntil = 1_800_000_000L,
    )

    @Test
    fun `test that init exposes a monthly offer on the monthly billing period`() = runTest {
        val offer = subscription(Skus.SKU_PRO_I_MONTH)
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }.thenReturn(offer)
        initViewModel()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.isMonthly).isTrue()
            assertThat(state.offerSubscription?.monthlySubscription).isEqualTo(offer)
            assertThat(state.offerSubscription?.yearlySubscription).isNull()
            assertThat(state.offerValidUntil).isEqualTo(1_800_000_000L)
        }
    }

    @Test
    fun `test that init exposes a yearly offer on the yearly billing period`() = runTest {
        val offer = subscription(Skus.SKU_PRO_I_YEAR)
        wheneverBlocking { getRecommendedSubscriptionWithOfferUseCase() }.thenReturn(offer)
        initViewModel()

        underTest.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.isMonthly).isFalse()
            assertThat(state.offerSubscription?.monthlySubscription).isNull()
            assertThat(state.offerSubscription?.yearlySubscription).isEqualTo(offer)
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
}
