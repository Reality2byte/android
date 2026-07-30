package mega.privacy.android.feature.contact.invite.mapper

import kotlinx.collections.immutable.persistentListOf
import mega.privacy.android.domain.entity.contacts.InviteContactRequest
import mega.privacy.android.feature.contact.invite.model.InvitationResult
import mega.privacy.android.feature.contact.invite.model.InviteMessage
import javax.inject.Inject

/**
 * Maps the raw [InviteContactRequest] results of an email-invitation attempt into the typed,
 * count-based [InvitationResult] consumed by the invite screen. String resolution (singular vs
 * plural, and which resource) is deferred to presentation.
 *
 * When [isFromAchievement] is set the result carries only the number of invitations sent, so the
 * achievements flow can return it instead of surfacing a snackbar.
 */
class InvitationMessageMapper @Inject constructor() {

    /**
     * @param isFromAchievement Whether the invitation was launched from the achievements flow.
     * @param requests The per-email invitation results, positionally aligned with [emails].
     * @param emails The emails that were invited.
     * @return The typed invitation outcome.
     */
    operator fun invoke(
        isFromAchievement: Boolean,
        requests: List<InviteContactRequest>,
        emails: List<String>,
    ): InvitationResult {
        val totalSent = requests.count { it == InviteContactRequest.Sent }
        val totalAlreadyReceived = requests.count { it == InviteContactRequest.AlreadyReceived }
        val totalNotSent = emails.size - totalSent

        return when {
            isFromAchievement -> InvitationResult.Achievement(sentNumber = totalSent)

            totalSent == emails.size -> InvitationResult.Snackbar(
                messages = persistentListOf(InviteMessage.Sent(count = totalSent)),
                actionLabelId = null,
            )

            emails.size == totalAlreadyReceived -> InvitationResult.Snackbar(
                messages = persistentListOf(
                    InviteMessage.AlreadyRequested(count = totalAlreadyReceived)
                ),
                actionLabelId = null,
            )

            totalAlreadyReceived > 0 && totalSent + totalAlreadyReceived == emails.size ->
                InvitationResult.Snackbar(
                    messages = persistentListOf(
                        InviteMessage.Sent(count = totalSent),
                        InviteMessage.AlreadyRequested(count = totalAlreadyReceived),
                    ),
                    actionLabelId = null,
                )

            else -> InvitationResult.Snackbar(
                messages = persistentListOf(
                    InviteMessage.Sent(count = totalSent),
                    InviteMessage.NotSent(count = totalNotSent),
                ),
                actionLabelId = null,
            )
        }
    }
}
