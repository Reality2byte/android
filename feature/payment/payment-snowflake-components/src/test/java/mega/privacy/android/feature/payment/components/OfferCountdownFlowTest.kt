package mega.privacy.android.feature.payment.components

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class OfferCountdownFlowTest {

    @Test
    fun `test that offerCountdownFlow emits the remaining time immediately`() = runTest {
        offerCountdownFlow(
            validUntil = 60L,
            currentTimeMillis = { 0L },
        ).test {
            assertThat(awaitItem()).isEqualTo(60.seconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that offerCountdownFlow ticks down and completes with zero once the offer has elapsed`() =
        runTest {
            offerCountdownFlow(
                validUntil = 60L,
                currentTimeMillis = { testScheduler.currentTime },
            ).test {
                assertThat(awaitItem()).isEqualTo(60.seconds)
                assertThat(awaitItem()).isEqualTo(30.seconds)
                assertThat(awaitItem()).isEqualTo(Duration.ZERO)
                awaitComplete()
            }
        }

    @Test
    fun `test that offerCountdownFlow emits zero and completes when the offer is already elapsed`() =
        runTest {
            offerCountdownFlow(
                validUntil = 10L,
                currentTimeMillis = { 20_000L },
            ).test {
                assertThat(awaitItem()).isEqualTo(Duration.ZERO)
                awaitComplete()
            }
        }
}
