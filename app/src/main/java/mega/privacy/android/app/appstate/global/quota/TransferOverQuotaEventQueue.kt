package mega.privacy.android.app.appstate.global.quota

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the pending bandwidth over quota event until an activity is resumed to show the warning.
 *
 * The SDK event is broadcast app wide, but the warning belongs to whichever activity is in front,
 * so it stays pending until one takes it: nothing is shown while the app is backgrounded, and
 * nothing is lost either. Downloads and streaming share the queue so that hitting both only ever
 * raises one warning.
 */
@Singleton
class TransferOverQuotaEventQueue @Inject constructor() {

    private val pending = MutableStateFlow<TransferOverQuotaSource?>(null)

    /**
     * Emits while a warning is waiting to be shown. Collectors must call [consume] to claim it.
     */
    val events: Flow<TransferOverQuotaSource> = pending.filterNotNull()

    /**
     * Queue an over quota event.
     *
     * @param source the kind of transfer that hit the quota.
     */
    fun emit(source: TransferOverQuotaSource) {
        pending.update { source }
    }

    /**
     * Claim the pending event, or null when another activity already took it. The event stays
     * pending until claimed, so an activity being paused mid-handover does not drop it.
     */
    fun consume(): TransferOverQuotaSource? = pending.getAndUpdate { null }
}
