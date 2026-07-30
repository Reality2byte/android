package mega.privacy.android.feature.contact.picker

import mega.privacy.android.shared.contact.model.AvatarData

/**
 * Mutually exclusive dialogs shown as the outcome of scanning a contact QR code.
 */
sealed interface ScannedContactDialog {
    /**
     * The scanned code is not a valid MEGA contact link, or the contact link query failed.
     */
    data object InvalidCode : ScannedContactDialog

    /**
     * The barcode scanner module is still downloading and the scan cannot start yet.
     */
    data object ScannerNotInstalled : ScannedContactDialog

    /**
     * The scanned user is already a contact but cannot be selected in this picker.
     *
     * @property email Email of the already-added contact.
     */
    data class AlreadyAdded(
        val email: String,
    ) : ScannedContactDialog

    /**
     * The scanned user was found and is not yet a contact, so they can be invited.
     *
     * @property contactName Display name of the scanned contact.
     * @property email Email of the scanned contact.
     * @property handle Handle of the scanned contact, needed to send the invitation.
     * @property avatar Avatar of the scanned contact.
     */
    data class Found(
        val contactName: String,
        val email: String,
        val handle: Long,
        val avatar: AvatarData,
    ) : ScannedContactDialog
}
