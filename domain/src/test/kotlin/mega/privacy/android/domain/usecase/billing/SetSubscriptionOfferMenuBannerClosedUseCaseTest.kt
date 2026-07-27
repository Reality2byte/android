package mega.privacy.android.domain.usecase.billing

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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetSubscriptionOfferMenuBannerClosedUseCaseTest {

    private lateinit var underTest: SetSubscriptionOfferMenuBannerClosedUseCase

    private val billingRepository = mock<BillingRepository>()

    @BeforeAll
    fun setUp() {
        underTest = SetSubscriptionOfferMenuBannerClosedUseCase(
            billingRepository = billingRepository,
        )
    }

    @BeforeEach
    fun resetMocks() {
        reset(billingRepository)
    }

    @Test
    fun `test that invoke saves the closed state in the repository`() = runTest {
        underTest()

        verify(billingRepository).setSubscriptionOfferMenuBannerClosed()
    }

    @Test
    fun `test that invoke does not close the home banner`() = runTest {
        underTest()

        verify(billingRepository).setSubscriptionOfferMenuBannerClosed()
        verifyNoMoreInteractions(billingRepository)
    }
}
