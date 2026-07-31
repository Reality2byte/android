package mega.privacy.android.feature.contact.invite.model

import androidx.compose.runtime.Stable
import de.palm.composestateevents.StateEvent
import de.palm.composestateevents.StateEventWithContent
import kotlinx.collections.immutable.ImmutableList
import mega.privacy.android.feature.contact.add.model.ScannedContactDialog
import mega.privacy.android.feature.contact.add.model.ScannedContactInviteFeedback
import mega.privacy.android.feature.contact.picker.PhoneContactsSection

/**
 * Invite contact ui state.
 */
@Stable
sealed interface InviteContactUiState {

    /**
     * Loading.
     */
    data object Loading : InviteContactUiState

    /**
     * Data.
     *
     * @property phoneContactsSection State of the collapsible phone-contacts section.
     * @property manualRecipients Email/phone chips added manually by the user.
     * @property query Current search query, or null when not searching.
     * @property contactLink The current user's contact link used for SMS invitations, or null when
     * not yet loaded.
     * @property invitationResultEvent One-shot event carrying the outcome of an invitation attempt.
     * @property emailValidationEvent One-shot event carrying feedback for a rejected manual email.
     * @property sendSmsEvent One-shot event requesting the SMS invitations be sent.
     * @property ongoingCallConfirmEvent One-shot event requesting confirmation before an action that
     * would interrupt an ongoing call.
     * @property scannedContactDialog Dialog to show as the outcome of a QR scan, or null when none.
     * @property scannedContactSelectEvent One-shot event carrying the handle of a scanned contact
     * that is already in the loaded list so the screen can auto-select it.
     * @property scannedContactInviteEvent One-shot event carrying the outcome of inviting a scanned
     * contact so the screen can surface feedback.
     */
    data class Data(
        val phoneContactsSection: PhoneContactsSection,
        val manualRecipients: ImmutableList<InviteRecipient>,
        val query: String?,
        val contactLink: String?,
        val invitationResultEvent: StateEventWithContent<InvitationResult>,
        val emailValidationEvent: StateEventWithContent<EmailValidationMessage>,
        val sendSmsEvent: StateEventWithContent<SmsInvite>,
        val ongoingCallConfirmEvent: StateEvent,
        val scannedContactDialog: ScannedContactDialog?,
        val scannedContactSelectEvent: StateEventWithContent<Long>,
        val scannedContactInviteEvent: StateEventWithContent<ScannedContactInviteFeedback>,
    ) : InviteContactUiState {
        /**
         * Whether there is any content the user can act on, i.e. at least one manual recipient or a
         * non-hidden phone-contacts section.
         */
        val hasSelectableContent: Boolean
            get() = manualRecipients.isNotEmpty() ||
                phoneContactsSection !is PhoneContactsSection.Hidden
    }
}
