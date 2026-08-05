package mega.privacy.android.app.appstate.global.initialisation.appstart

import kotlinx.coroutines.flow.catch
import mega.privacy.android.app.appstate.global.quota.TransferOverQuotaEventQueue
import mega.privacy.android.app.appstate.global.quota.TransferOverQuotaSource
import mega.privacy.android.domain.usecase.transfers.overquota.BroadcastTransferOverQuotaUseCase
import mega.privacy.android.domain.usecase.transfers.overquota.MonitorStreamOverQuotaEventUseCase
import mega.privacy.android.navigation.contract.initialisation.initialisers.AppStartInitialiserAction
import timber.log.Timber
import javax.inject.Inject

/**
 * Monitors bandwidth over quota hit while streaming, so the state is updated app wide and the
 * warning is queued for the activity that is in front.
 */
class StreamOverQuotaInitialiser @Inject constructor(
    monitorStreamOverQuotaEventUseCase: MonitorStreamOverQuotaEventUseCase,
    broadcastTransferOverQuotaUseCase: BroadcastTransferOverQuotaUseCase,
    transferOverQuotaEventQueue: TransferOverQuotaEventQueue,
) : AppStartInitialiserAction(action = {
    monitorStreamOverQuotaEventUseCase()
        .catch { Timber.e(it, "Error monitoring streaming over quota events") }
        .collect { timeLeft ->
            Timber.d("Emit stream over quota $timeLeft")
            broadcastTransferOverQuotaUseCase(true)
            transferOverQuotaEventQueue.emit(TransferOverQuotaSource.Streaming)
        }
})
