package mega.privacy.android.app.appstate.content.navigation.model

import mega.privacy.android.domain.entity.StorageState
import mega.privacy.android.domain.entity.account.StorageQuotaWarningTrigger

/**
 * A storage quota warning that has passed the trigger rules and is ready to be shown.
 *
 * @property isUpsellEnabled true when the quota-warning upsell screen replaces the legacy dialog
 */
data class StorageQuotaWarning(
    val storageState: StorageState,
    val trigger: StorageQuotaWarningTrigger,
    val isUpsellEnabled: Boolean,
)
