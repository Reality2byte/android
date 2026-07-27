package mega.privacy.android.app.appstate.global.initialisation.appcreate

import mega.privacy.android.analytics.Analytics
import mega.privacy.android.analytics.tracker.AnalyticsTracker
import mega.privacy.android.navigation.contract.initialisation.SynchronousAppCreateInitialiser
import javax.inject.Inject

/**
 * Registers the analytics tracker with the [Analytics] singleton.
 *
 * Synchronous: [Analytics.tracker] throws when accessed before a tracker is registered, so the
 * tracker must be in place before any code that reports analytics events can run.
 */
internal class AnalyticsInitialiser @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
) : SynchronousAppCreateInitialiser {
    override val name = "AnalyticsInitialiser"

    override operator fun invoke() {
        Analytics.initialise(analyticsTracker)
    }
}
