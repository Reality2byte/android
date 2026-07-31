package mega.privacy.android.data.gateway.contact

import mega.privacy.android.domain.entity.contacts.LocalContact
import mega.privacy.android.domain.entity.uri.UriPath

/**
 * User's contacts related gateway
 */
interface ContactGateway {

    /**
     * Get list of local contacts
     *
     * @return List of [LocalContact]
     */
    suspend fun getLocalContacts(): List<LocalContact>

    /**
     * Get list of local contacts from a contact picker session [UriPath].
     *
     * The [UriPath] is returned by the Android system contact picker and can be queried
     * without the READ_CONTACTS permission. Contacts are grouped per contact.
     *
     * @param uriPath The [UriPath] returned by the contact picker.
     * @param includePhoneNumbers When `true`, phone-number rows are also resolved and populated on
     * [LocalContact.phoneNumbers] alongside emails; when `false`, only contacts with email
     * addresses are returned.
     * @return List of [LocalContact]
     */
    suspend fun getLocalContactsFromUri(
        uriPath: UriPath,
        includePhoneNumbers: Boolean,
    ): List<LocalContact>

    /**
     * Get list of local contact's numbers
     *
     * @return List of [LocalContact]
     */
    suspend fun getLocalContactNumbers(): List<LocalContact>

    /**
     * Get list of local contact's email addresses
     *
     * @return List of [LocalContact]
     */
    suspend fun getLocalContactEmailAddresses(): List<LocalContact>
}
