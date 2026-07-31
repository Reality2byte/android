package mega.privacy.android.feature.contact.invite.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import mega.privacy.android.shared.contact.model.AvatarData

/**
 * A recipient the user has chosen to invite. Recipients come from three sources: a picked device
 * contact, a manually typed email, or a manually typed phone number. Emails and phone numbers are
 * only distinguished by type at send time.
 */
@Stable
sealed interface InviteRecipient {

    /**
     * A device contact picked by the user. A device contact may expose several emails and/or phone
     * numbers; which of them is actually invited is decided by the selection state.
     *
     * @property name Display name of the device contact.
     * @property emails Emails exposed by the device contact.
     * @property phoneNumbers Phone numbers exposed by the device contact.
     * @property avatar Avatar to render for the device contact.
     */
    data class DeviceContact(
        val name: String,
        val emails: ImmutableList<String>,
        val phoneNumbers: ImmutableList<String>,
        val avatar: AvatarData,
    ) : InviteRecipient

    /**
     * An email typed manually by the user.
     *
     * @property value The email address.
     */
    data class ManualEmail(
        val value: String,
    ) : InviteRecipient

    /**
     * A phone number typed manually by the user.
     *
     * @property value The phone number.
     */
    data class ManualPhone(
        val value: String,
    ) : InviteRecipient
}
