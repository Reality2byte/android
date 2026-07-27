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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitorSubscriptionOfferMenuBannerClosedUseCaseTest {

    private lateinit var underTest: MonitorSubscriptionOfferMenuBannerClosedUseCase

    private val billingRepository = mock<BillingRepository>()

    @BeforeAll
    fun setUp() {
        underTest = MonitorSubscriptionOfferMenuBannerClosedUseCase(
            billingRepository = billingRepository,
        )
    }

    @BeforeEach
    fun resetMocks() {
        reset(billingRepository)
    }

    @Test
    fun `test that invoke returns true when the menu banner is closed`() = runTest {
        whenever(billingRepository.monitorSubscriptionOfferMenuBannerClosed())
            .thenReturn(flowOf(true))

        val actual = underTest().first()

        assertThat(actual).isTrue()
    }

    @Test
    fun `test that invoke returns false when the menu banner is not closed`() = runTest {
        whenever(billingRepository.monitorSubscriptionOfferMenuBannerClosed())
            .thenReturn(flowOf(false))

        val actual = underTest().first()

        assertThat(actual).isFalse()
    }

    @Test
    fun `test that invoke monitors the menu preference and not the home one`() = runTest {
        whenever(billingRepository.monitorSubscriptionOfferMenuBannerClosed())
            .thenReturn(flowOf(false))

        underTest()

        verify(billingRepository).monitorSubscriptionOfferMenuBannerClosed()
        verifyNoMoreInteractions(billingRepository)
    }
}
