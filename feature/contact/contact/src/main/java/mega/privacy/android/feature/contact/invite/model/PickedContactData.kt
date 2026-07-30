package mega.privacy.android.feature.contact.invite.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

/**
 * Emails and phone numbers of the device contacts just picked from the OS contact picker, carried by
 * a one-shot event so the screen can auto-select them in its selection state. Emails and phone
 * numbers are only distinguished by type here; a phone-only contact contributes to [phoneNumbers]
 * with no [emails] entry.
 *
 * @property emails Emails exposed by the newly-picked contacts.
 * @property phoneNumbers Phone numbers exposed by the newly-picked contacts.
 */
@Stable
data class PickedContactData(
    val emails: ImmutableList<String>,
    val phoneNumbers: ImmutableList<String>,
)
