package mega.privacy.android.app.appstate.content.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import mega.privacy.android.app.appstate.content.navigation.model.StorageQuotaWarning
import mega.privacy.android.core.coroutine.logAndSwallowExceptions
import mega.privacy.android.domain.entity.account.StorageQuotaWarningTrigger
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.domain.usecase.account.MonitorStorageStateUseCase
import mega.privacy.android.domain.usecase.account.SetStorageQuotaWarningShownUseCase
import mega.privacy.android.domain.usecase.account.ShouldShowStorageQuotaWarningUseCase
import mega.privacy.android.domain.usecase.featureflag.GetFeatureFlagValueUseCase
import mega.privacy.android.domain.usecase.transfers.uploads.MonitorSuccessfulUploadsUseCase
import javax.inject.Inject

@HiltViewModel
class StorageStatusViewModel @Inject constructor(
    private val monitorStorageStateUseCase: MonitorStorageStateUseCase,
    private val monitorSuccessfulUploadsUseCase: MonitorSuccessfulUploadsUseCase,
    private val shouldShowStorageQuotaWarningUseCase: ShouldShowStorageQuotaWarningUseCase,
    private val setStorageQuotaWarningShownUseCase: SetStorageQuotaWarningShownUseCase,
    private val getFeatureFlagValueUseCase: GetFeatureFlagValueUseCase,
) : ViewModel() {

    /**
     * Storage quota warnings to show, in the order they are raised.
     *
     * Cold on purpose: collect only once the account's nodes are fetched, and collect afresh per
     * session. [MonitorStorageStateUseCase] reads the current state on subscription, which is
     * meaningless before the fetch, and a stale [distinctUntilChanged] would otherwise swallow the
     * warning for a second account sitting at the same state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val quotaWarnings: Flow<StorageQuotaWarning> = monitorStorageStateUseCase()
        .distinctUntilChanged()
        .flatMapLatest { storageState ->
            merge(
                flowOf(StorageQuotaWarningTrigger.LoginOrReload),
                monitorSuccessfulUploadsUseCase()
                    .map { StorageQuotaWarningTrigger.UploadSuccess },
            ).map { trigger -> storageState to trigger }
        }
        .filter { (storageState, trigger) ->
            runCatching { shouldShowStorageQuotaWarningUseCase(storageState, trigger) }
                .logAndSwallowExceptions()
                .getOrDefault(false)
        }
        .map { (storageState, trigger) ->
            StorageQuotaWarning(
                storageState = storageState,
                trigger = trigger,
                isUpsellEnabled = isQuotaWarningUpsellEnabled(),
            )
        }
        .logAndSwallowExceptions()

    /**
     * Spend the trigger's daily allowance, once the screen has actually shown the warning. Kept out
     * of [quotaWarnings] so a warning raised while nothing is collecting costs nothing.
     */
    suspend fun onQuotaWarningShown(warning: StorageQuotaWarning) {
        runCatching { setStorageQuotaWarningShownUseCase(warning.storageState, warning.trigger) }
            .logAndSwallowExceptions()
    }

    private suspend fun isQuotaWarningUpsellEnabled() =
        runCatching { getFeatureFlagValueUseCase(ApiFeatures.QuotaWarningUpsellScreen) }
            .getOrElse { false }
}
