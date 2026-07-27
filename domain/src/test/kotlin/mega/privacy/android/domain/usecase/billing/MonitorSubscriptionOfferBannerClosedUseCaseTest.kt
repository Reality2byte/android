package mega.privacy.android.domain.usecase.billing

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.repository.BillingRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitorSubscriptionOfferBannerClosedUseCaseTest {

    private lateinit var underTest: MonitorSubscriptionOfferBannerClosedUseCase

    private val billingRepository = mock<BillingRepository>()

    @BeforeAll
    fun setUp() {
        underTest = MonitorSubscriptionOfferBannerClosedUseCase(
            billingRepository = billingRepository,
        )
    }

    @BeforeEach
    fun resetMocks() {
        reset(billingRepository)
    }

    @Test
    fun `test that invoke returns true when the banner is closed`() = runTest {
        whenever(billingRepository.monitorSubscriptionOfferBannerClosed())
            .thenReturn(flowOf(true))

        val actual = underTest().first()

        assertThat(actual).isTrue()
    }

    @Test
    fun `test that invoke returns false when the banner is not closed`() = runTest {
        whenever(billingRepository.monitorSubscriptionOfferBannerClosed())
            .thenReturn(flowOf(false))

        val actual = underTest().first()

        assertThat(actual).isFalse()
    }

    @Test
    fun `test that invoke monitors the repository`() = runTest {
        whenever(billingRepository.monitorSubscriptionOfferBannerClosed())
            .thenReturn(flowOf(false))

        underTest()

        verify(billingRepository).monitorSubscriptionOfferBannerClosed()
    }
}
