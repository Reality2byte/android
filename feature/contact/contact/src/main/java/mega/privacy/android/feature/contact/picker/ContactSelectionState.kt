package mega.privacy.android.feature.contact.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Compose-owned selection state for the contacts multi-select picker. MEGA contacts are keyed by the
 * stable contact handle, while phone contacts are keyed by email (they have no MEGA handle). Selection
 * is held independently of the (filterable) contact list, so a selected contact stays selected across
 * search/filter changes and reappears selected when the filter clears.
 *
 * Manually typed emails (share flow) are a third, independently keyed set so they can be rendered
 * and removed as free-text entries without colliding with the phone-contact rows.
 *
 * Phone numbers (invite flow) are a fourth set keyed by the raw number. It carries both picked
 * device-contact phone numbers and manually typed ones, since only send time distinguishes email
 * from phone.
 *
 * @param initialSelectedHandles MEGA contact handles to pre-select.
 * @param initialSelectedPhoneEmails phone-contact emails to pre-select.
 * @param initialSelectedManualEmails manually entered emails to pre-select.
 * @param initialSelectedPhoneNumbers phone numbers to pre-select.
 */
@Stable
class ContactSelectionState(
    initialSelectedHandles: Set<Long> = emptySet(),
    initialSelectedPhoneEmails: Set<String> = emptySet(),
    initialSelectedManualEmails: Set<String> = emptySet(),
    initialSelectedPhoneNumbers: Set<String> = emptySet(),
) {
    var selectedHandles: Set<Long> by mutableStateOf(initialSelectedHandles)
        private set

    var selectedPhoneEmails: Set<String> by mutableStateOf(initialSelectedPhoneEmails)
        private set

    var selectedManualEmails: Set<String> by mutableStateOf(initialSelectedManualEmails)
        private set

    var selectedPhoneNumbers: Set<String> by mutableStateOf(initialSelectedPhoneNumbers)
        private set

    /**
     * Number of currently selected items across MEGA contacts, phone contacts, manual emails and
     * phone numbers.
     */
    val selectedItemsCount: Int
        get() = selectedHandles.size + selectedPhoneEmails.size + selectedManualEmails.size +
            selectedPhoneNumbers.size

    /**
     * Toggle the selection of the MEGA contact with the given [handle].
     */
    fun toggleSelection(handle: Long) {
        selectedHandles = if (handle in selectedHandles) {
            selectedHandles - handle
        } else {
            selectedHandles + handle
        }
    }

    /**
     * Toggle the selection of the phone contact with the given [email].
     */
    fun togglePhoneSelection(email: String) {
        selectedPhoneEmails = if (email in selectedPhoneEmails) {
            selectedPhoneEmails - email
        } else {
            selectedPhoneEmails + email
        }
    }

    /**
     * Select the MEGA contact with the given [handle] without deselecting any existing selection.
     */
    fun selectHandle(handle: Long) {
        selectedHandles = selectedHandles + handle
    }

    /**
     * Select the phone contacts with the given [emails] without deselecting any existing selection.
     */
    fun selectPhoneEmails(emails: Collection<String>) {
        selectedPhoneEmails = selectedPhoneEmails + emails
    }

    /**
     * Select the manually entered [email] without deselecting any existing selection.
     */
    fun selectManualEmail(email: String) {
        selectedManualEmails = selectedManualEmails + email
    }

    /**
     * Remove the manually entered [email] from the selection.
     */
    fun removeManualEmail(email: String) {
        selectedManualEmails = selectedManualEmails - email
    }

    /**
     * Toggle the selection of the given phone [number].
     */
    fun togglePhoneNumber(number: String) {
        selectedPhoneNumbers = if (number in selectedPhoneNumbers) {
            selectedPhoneNumbers - number
        } else {
            selectedPhoneNumbers + number
        }
    }

    /**
     * Select the given phone [numbers] without deselecting any existing selection.
     */
    fun selectPhoneNumbers(numbers: Collection<String>) {
        selectedPhoneNumbers = selectedPhoneNumbers + numbers
    }

    /**
     * Remove the given phone [number] from the selection.
     */
    fun removePhoneNumber(number: String) {
        selectedPhoneNumbers = selectedPhoneNumbers - number
    }

    /**
     * Whether the given phone [number] is currently selected.
     */
    fun isPhoneNumberSelected(number: String): Boolean = number in selectedPhoneNumbers

    /**
     * Whether [email] is already selected as a phone contact or manual entry, compared
     * case-insensitively.
     */
    fun isEmailSelected(email: String): Boolean =
        (selectedPhoneEmails + selectedManualEmails).any { it.equals(email, ignoreCase = true) }

    /**
     * Clear the current selection.
     */
    fun deselectAll() {
        selectedHandles = emptySet()
        selectedPhoneEmails = emptySet()
        selectedManualEmails = emptySet()
        selectedPhoneNumbers = emptySet()
    }

    companion object {
        val Saver: Saver<ContactSelectionState, List<List<String>>> = Saver(
            save = { state ->
                listOf(
                    state.selectedHandles.map { it.toString() },
                    state.selectedPhoneEmails.toList(),
                    state.selectedManualEmails.toList(),
                    state.selectedPhoneNumbers.toList(),
                )
            },
            restore = { saved ->
                ContactSelectionState(
                    initialSelectedHandles = saved.getOrNull(0)
                        ?.map { it.toLong() }
                        ?.toSet()
                        ?: emptySet(),
                    initialSelectedPhoneEmails = saved.getOrNull(1)?.toSet() ?: emptySet(),
                    initialSelectedManualEmails = saved.getOrNull(2)?.toSet() ?: emptySet(),
                    initialSelectedPhoneNumbers = saved.getOrNull(3)?.toSet() ?: emptySet(),
                )
            },
        )
    }
}

/**
 * Remember a [ContactSelectionState] that survives recomposition and configuration changes.
 */
@Composable
fun rememberContactSelectionState(
    initialSelectedHandles: Set<Long> = emptySet(),
    initialSelectedPhoneEmails: Set<String> = emptySet(),
    initialSelectedManualEmails: Set<String> = emptySet(),
    initialSelectedPhoneNumbers: Set<String> = emptySet(),
): ContactSelectionState =
    rememberSaveable(saver = ContactSelectionState.Saver) {
        ContactSelectionState(
            initialSelectedHandles = initialSelectedHandles,
            initialSelectedPhoneEmails = initialSelectedPhoneEmails,
            initialSelectedManualEmails = initialSelectedManualEmails,
            initialSelectedPhoneNumbers = initialSelectedPhoneNumbers,
        )
    }
