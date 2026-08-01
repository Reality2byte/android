package mega.privacy.android.app.appstate.global.quota

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

/**
 * Holds the pending streaming bandwidth over quota event until an activity is resumed to show the
 * warning.
 *
 * The SDK event is broadcast app wide, but the warning belongs to whichever activity is in front,
 * so it stays pending until one takes it: nothing is shown while the app is backgrounded, and
 * nothing is lost either. Only the latest event is kept, as it carries the time left of the
 * current over quota state.
 */
@Singleton
class StreamOverQuotaEventQueue @Inject constructor() {

    private val pending = MutableStateFlow<Duration?>(null)

    /**
     * Emits while a warning is waiting to be shown. Collectors must call [consume] to claim it.
     */
    val events: Flow<Duration> = pending.filterNotNull()

    /**
     * Queue a streaming over quota event.
     *
     * @param timeLeft time remaining until the over quota state ends.
     */
    fun emit(timeLeft: Duration) {
        pending.update { timeLeft }
    }

    /**
     * Claim the pending event, or null when another activity already took it. The event stays
     * pending until claimed, so an activity being paused mid-handover does not drop it.
     */
    fun consume(): Duration? = pending.getAndUpdate { null }
}
