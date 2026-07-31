package mega.privacy.android.feature.contact.picker

/**
 * Selects which contact data the OS multi-select contact picker should surface.
 *
 * @property Email Restricts the picker to contacts with an email address (add/share flow).
 * @property EmailAndPhone Surfaces all contacts so both email- and phone-only contacts can be
 * picked (invite flow).
 */
enum class ContactPickMode {
    Email,
    EmailAndPhone,
}
