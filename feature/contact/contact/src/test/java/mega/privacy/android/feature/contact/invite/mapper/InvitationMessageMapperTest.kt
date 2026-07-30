package mega.privacy.android.feature.contact.invite.mapper

import com.google.common.truth.Truth.assertThat
import mega.privacy.android.domain.entity.contacts.InviteContactRequest
import mega.privacy.android.feature.contact.invite.model.InvitationResult
import mega.privacy.android.feature.contact.invite.model.InviteMessage
import org.junit.jupiter.api.Test

class InvitationMessageMapperTest {

    private val underTest = InvitationMessageMapper()

    @Test
    fun `test that Achievement result carries the sent count when invoked from achievements`() {
        val result = underTest(
            isFromAchievement = true,
            requests = listOf(InviteContactRequest.Sent, InviteContactRequest.InvalidStatus),
            emails = listOf("a@test.com", "b@test.com"),
        )

        assertThat(result).isEqualTo(InvitationResult.Achievement(sentNumber = 1))
    }

    @Test
    fun `test that all-sent maps to a single Sent message`() {
        val result = underTest(
            isFromAchievement = false,
            requests = listOf(InviteContactRequest.Sent, InviteContactRequest.Sent),
            emails = listOf("a@test.com", "b@test.com"),
        )

        val snackbar = result as InvitationResult.Snackbar
        assertThat(snackbar.messages).containsExactly(InviteMessage.Sent(count = 2))
        assertThat(snackbar.actionLabelId).isNull()
    }

    @Test
    fun `test that all-already-received maps to a single AlreadyRequested message`() {
        val result = underTest(
            isFromAchievement = false,
            requests = listOf(
                InviteContactRequest.AlreadyReceived,
                InviteContactRequest.AlreadyReceived,
            ),
            emails = listOf("a@test.com", "b@test.com"),
        )

        val snackbar = result as InvitationResult.Snackbar
        assertThat(snackbar.messages)
            .containsExactly(InviteMessage.AlreadyRequested(count = 2))
    }

    @Test
    fun `test that a single already-received maps to a single AlreadyRequested message`() {
        val result = underTest(
            isFromAchievement = false,
            requests = listOf(InviteContactRequest.AlreadyReceived),
            emails = listOf("a@test.com"),
        )

        val snackbar = result as InvitationResult.Snackbar
        assertThat(snackbar.messages)
            .containsExactly(InviteMessage.AlreadyRequested(count = 1))
    }

    @Test
    fun `test that sent plus already-received maps to Sent and AlreadyRequested messages`() {
        val result = underTest(
            isFromAchievement = false,
            requests = listOf(
                InviteContactRequest.Sent,
                InviteContactRequest.AlreadyReceived,
            ),
            emails = listOf("a@test.com", "b@test.com"),
        )

        val snackbar = result as InvitationResult.Snackbar
        assertThat(snackbar.messages).containsExactly(
            InviteMessage.Sent(count = 1),
            InviteMessage.AlreadyRequested(count = 1),
        ).inOrder()
    }

    @Test
    fun `test that partial failures map to Sent and NotSent messages`() {
        val result = underTest(
            isFromAchievement = false,
            requests = listOf(
                InviteContactRequest.Sent,
                InviteContactRequest.InvalidEmail,
            ),
            emails = listOf("a@test.com", "b@test.com"),
        )

        val snackbar = result as InvitationResult.Snackbar
        assertThat(snackbar.messages).containsExactly(
            InviteMessage.Sent(count = 1),
            InviteMessage.NotSent(count = 1),
        ).inOrder()
    }
}
