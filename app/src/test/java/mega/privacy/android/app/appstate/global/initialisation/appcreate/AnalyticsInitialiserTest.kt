package mega.privacy.android.app.appstate.global.initialisation.appcreate

import com.google.common.truth.Truth.assertThat
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.analytics.tracker.AnalyticsTracker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class AnalyticsInitialiserTest {

    private val analyticsTracker = mock<AnalyticsTracker>()
    private val underTest = AnalyticsInitialiser(
        analyticsTracker = analyticsTracker,
    )

    @AfterEach
    fun tearDown() {
        Analytics.initialise(null)
    }

    @Test
    fun `test that invoke registers the tracker with the analytics singleton`() {
        underTest()

        assertThat(Analytics.tracker).isSameInstanceAs(analyticsTracker)
    }
}
