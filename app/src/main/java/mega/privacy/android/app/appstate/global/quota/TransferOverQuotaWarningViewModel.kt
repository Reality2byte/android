package mega.privacy.android.app.appstate.global.quota

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.usecase.environment.GetCurrentTimeInMillisUseCase
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

/**
 * ViewModel exposing the pending bandwidth over quota event to the activity hosting the navigation
 * back stack.
 *
 * Scoped to that activity, so the warning is shown again when the user opens another one.
 */
@HiltViewModel
class TransferOverQuotaWarningViewModel @Inject constructor(
    private val transferOverQuotaEventQueue: TransferOverQuotaEventQueue,
    private val getCurrentTimeInMillisUseCase: GetCurrentTimeInMillisUseCase,
) : ViewModel() {

    private var lastWarnedAt = 0L

    /**
     * Emits while an over quota warning is waiting to be shown.
     */
    val transferOverQuotaEvents: Flow<TransferOverQuotaSource> = transferOverQuotaEventQueue.events

    /**
     * Claim the pending warning, or null when another activity already took it or it was already
     * shown here within [WARNING_INTERVAL].
     *
     * Dismissing the warning resumes the transfer, which hits the over quota again straight away,
     * so without the interval it would be shown again on every dismissal.
     */
    fun consumeTransferOverQuotaEvent(): TransferOverQuotaSource? {
        val source = transferOverQuotaEventQueue.consume() ?: return null
        val now = getCurrentTimeInMillisUseCase()
        if (now - lastWarnedAt < WARNING_INTERVAL.inWholeMilliseconds) return null
        lastWarnedAt = now
        return source
    }

    private companion object {
        // Matches the interval the legacy transfer over quota warning uses.
        val WARNING_INTERVAL = 1.minutes
    }
}
