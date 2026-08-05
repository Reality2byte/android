package mega.privacy.android.app.appstate.global.quota

import mega.privacy.android.navigation.payment.QuotaWarningTrigger

/**
 * The kind of transfer that hit bandwidth over quota, selecting the copy shown on the warning.
 */
enum class TransferOverQuotaSource {

    /**
     * A download the user started.
     */
    Download,

    /**
     * Media the user is streaming.
     */
    Streaming;

    /**
     * The matching [QuotaWarningTrigger] for the quota-warning upsell screen.
     */
    val quotaWarningTrigger: QuotaWarningTrigger
        get() = when (this) {
            Download -> QuotaWarningTrigger.Download
            Streaming -> QuotaWarningTrigger.Streaming
        }
}
