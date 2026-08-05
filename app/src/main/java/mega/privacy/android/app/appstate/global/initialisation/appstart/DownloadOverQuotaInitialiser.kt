package mega.privacy.android.app.appstate.global.initialisation.appstart

import kotlinx.coroutines.flow.catch
import mega.privacy.android.app.appstate.global.quota.TransferOverQuotaEventQueue
import mega.privacy.android.app.appstate.global.quota.TransferOverQuotaSource
import mega.privacy.android.domain.usecase.transfers.overquota.MonitorTransferOverQuotaEventUseCase
import mega.privacy.android.navigation.contract.initialisation.initialisers.AppStartInitialiserAction
import timber.log.Timber
import javax.inject.Inject

/**
 * Monitors bandwidth over quota hit while downloading, so the warning is queued for the activity
 * that is in front rather than only reaching the single activity shell.
 */
class DownloadOverQuotaInitialiser @Inject constructor(
    monitorTransferOverQuotaEventUseCase: MonitorTransferOverQuotaEventUseCase,
    transferOverQuotaEventQueue: TransferOverQuotaEventQueue,
) : AppStartInitialiserAction(action = {
    monitorTransferOverQuotaEventUseCase()
        .catch { Timber.e(it, "Error monitoring download over quota events") }
        .collect { status ->
            Timber.d("Emit download over quota $status")
            transferOverQuotaEventQueue.emit(TransferOverQuotaSource.Download)
        }
})
