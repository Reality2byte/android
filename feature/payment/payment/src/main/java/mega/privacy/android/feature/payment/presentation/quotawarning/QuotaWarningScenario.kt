package mega.privacy.android.feature.payment.presentation.quotawarning

import mega.privacy.android.domain.entity.StorageState
import mega.privacy.android.navigation.payment.QuotaWarningType

/**
 * The quota-warning scenario shown on the upsell screen: which metric is running out, and whether
 * it is merely close to the plan limit or already exhausted.
 */
internal enum class QuotaWarningScenario {
    /**
     * Storage is close to the plan limit.
     */
    StorageAlmostFull,

    /**
     * Storage is at or over the plan limit.
     */
    StorageFull,

    /**
     * Transfer quota is close to the plan limit.
     */
    TransferAlmostUsed,

    /**
     * Transfer quota is exhausted.
     */
    TransferAllUsed,
}

/**
 * Resolves the scenario from the metric the screen was opened for and the backend quota state.
 *
 * Severity is never computed from usage percentages: storage almost-full vs full comes from
 * [storageState], and transfer running-low vs exceeded from [isTransferOverQuota].
 */
internal fun quotaWarningScenario(
    type: QuotaWarningType,
    storageState: StorageState,
    isTransferOverQuota: Boolean,
): QuotaWarningScenario = when (type) {
    QuotaWarningType.Storage ->
        if (storageState == StorageState.Red || storageState == StorageState.PayWall) {
            QuotaWarningScenario.StorageFull
        } else {
            QuotaWarningScenario.StorageAlmostFull
        }

    QuotaWarningType.Transfer ->
        if (isTransferOverQuota) {
            QuotaWarningScenario.TransferAllUsed
        } else {
            QuotaWarningScenario.TransferAlmostUsed
        }
}
