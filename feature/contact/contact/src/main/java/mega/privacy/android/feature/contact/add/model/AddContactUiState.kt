package mega.privacy.android.feature.contact.add.model

import de.palm.composestateevents.StateEventWithContent
import kotlinx.collections.immutable.ImmutableList
import mega.privacy.android.feature.contact.picker.PhoneContactsSection
import mega.privacy.android.feature.contact.picker.ScannedContactDialog
import mega.privacy.android.feature.contact.picker.ScannedContactInviteFeedback
import mega.privacy.android.shared.contact.model.ContactItemUiState

/**
 * Add contact ui state
 */
sealed interface AddContactUiState {
    /**
     * Loading
     */
    data object Loading : AddContactUiState

    /**
     * Data
     *
     * @property contacts MEGA contacts to display, already filtered by [query].
     * @property query Current search query, or null when not searching.
     * @property showUserLimitWarning Whether to show the call user-limit warning (meeting flow only).
     * @property isContactVerificationWarningEnabled Whether the account-level contact-verification
     * warning is enabled. Gates the unverified-recipient warning surfaced by the share flow.
     * @property phoneContactsSection State of the collapsible phone-contacts section.
     * @property phoneContactsPickedEvent One-shot event carrying the emails newly added by the system
     * picker so the screen can auto-select them. Only fired on the post-17 picker path.
     * @property scannedContactDialog Dialog to show as the outcome of a QR scan, or null when none.
     * @property scannedContactSelectEvent One-shot event carrying the handle of a scanned contact
     * that is already in the loaded list so the screen can auto-select it.
     * @property scannedContactInviteEvent One-shot event carrying the outcome of inviting a
     * scanned contact so the screen can surface feedback.
     */
    data class Data(
        val contacts: ImmutableList<ContactItemUiState>,
        val query: String?,
        val showUserLimitWarning: Boolean,
        val isContactVerificationWarningEnabled: Boolean,
        val phoneContactsSection: PhoneContactsSection,
        val phoneContactsPickedEvent: StateEventWithContent<List<String>>,
        val scannedContactDialog: ScannedContactDialog?,
        val scannedContactSelectEvent: StateEventWithContent<Long>,
        val scannedContactInviteEvent: StateEventWithContent<ScannedContactInviteFeedback>,
    ) : AddContactUiState {
        /**
         * Whether there are no contacts to display (no contacts at all, or none match the query).
         */
        val isEmpty: Boolean get() = contacts.isEmpty()
    }
}
