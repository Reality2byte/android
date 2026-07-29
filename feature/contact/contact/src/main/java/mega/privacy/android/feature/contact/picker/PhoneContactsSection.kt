package mega.privacy.android.feature.contact.picker

import kotlinx.collections.immutable.ImmutableList
import mega.privacy.android.shared.contact.model.ContactItemUiState

/**
 * State of the collapsible "Phone contacts" section shown above the MEGA contact list.
 */
sealed interface PhoneContactsSection {
    /**
     * The section is not shown at all (e.g. this flow does not surface phone contacts).
     */
    data object Hidden : PhoneContactsSection

    /**
     * READ_CONTACTS permission is required before phone contacts can be listed (pre-17 path).
     */
    data object PermissionRequired : PhoneContactsSection

    /**
     * Phone contacts have been bulk-loaded and are ready to display (pre-17 path).
     *
     * @property contacts the emailable phone contacts, filtered by the current query.
     */
    data class Loaded(
        val contacts: ImmutableList<ContactItemUiState>,
    ) : PhoneContactsSection

    /**
     * The system contact picker is available (post-17 path). Contacts appear here only after the
     * user picks them.
     *
     * @property picked the contacts picked so far, filtered by the current query.
     */
    data class PickerAvailable(
        val picked: ImmutableList<ContactItemUiState>,
    ) : PhoneContactsSection
}
