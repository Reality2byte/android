package mega.privacy.android.feature.contact.invite.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

/**
 * Outcome of an invitation attempt, surfaced to the user as a one-shot event.
 */
@Stable
sealed interface InvitationResult {

    /**
     * Show a snackbar summarising how the invitations were handled.
     *
     * @property messages Typed, count-based descriptions of the outcome. String resolution (including
     * singular/plural selection) happens in presentation.
     * @property actionLabelId Optional label for the snackbar action, or null when there is no action.
     */
    data class Snackbar(
        val messages: ImmutableList<InviteMessage>,
        @StringRes val actionLabelId: Int?,
    ) : InvitationResult

    /**
     * Return the number of sent invitations back to the achievements flow instead of showing UI.
     *
     * @property sentNumber Number of invitations sent.
     */
    data class Achievement(
        val sentNumber: Int,
    ) : InvitationResult
}

/**
 * A typed, count-based description of an invitation outcome. The concrete string (singular vs plural,
 * and which resource) is resolved in presentation.
 */
sealed interface InviteMessage {

    /**
     * [count] invitations were sent successfully.
     *
     * @property count Number of invitations sent.
     */
    data class Sent(
        val count: Int,
    ) : InviteMessage

    /**
     * [count] invited users had already sent the current user an invitation.
     *
     * @property count Number of recipients who had already requested.
     */
    data class AlreadyRequested(
        val count: Int,
    ) : InviteMessage

    /**
     * [count] invitations could not be sent.
     *
     * @property count Number of invitations that failed.
     */
    data class NotSent(
        val count: Int,
    ) : InviteMessage
}

/**
 * Request to invite the given [phoneNumbers] via SMS using the current user's [contactLink].
 *
 * @property phoneNumbers Phone numbers to invite by SMS.
 * @property contactLink The current user's contact link to embed in the SMS.
 */
@Stable
data class SmsInvite(
    val phoneNumbers: ImmutableList<String>,
    val contactLink: String,
)

/**
 * Feedback shown for a manually entered email that cannot be invited. Mirrors the domain
 * [mega.privacy.android.domain.entity.contacts.EmailInvitationsInputValidity] non-valid cases and
 * adds an invalid-input case for malformed entries. String resolution happens in presentation.
 */
sealed interface EmailValidationMessage {

    /**
     * The entered email is the current user's own email.
     */
    data object MyOwnEmail : EmailValidationMessage

    /**
     * The entered email is already saved as a contact.
     */
    data object AlreadyInContacts : EmailValidationMessage

    /**
     * The entered email has already been invited and is in a pending state.
     */
    data object Pending : EmailValidationMessage

    /**
     * The entered text is not a valid email address.
     */
    data object InvalidInput : EmailValidationMessage
}
