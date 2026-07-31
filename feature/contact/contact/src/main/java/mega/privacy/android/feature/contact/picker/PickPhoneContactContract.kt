package mega.privacy.android.feature.contact.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContract

/**
 * Activity result contract that opens the OS multi-select contact picker
 * (`ACTION_PICK_CONTACTS`), requesting either email-only contacts or all contacts depending on the
 * [ContactPickMode] input.
 *
 * The intent `type` controls which contacts the picker surfaces:
 * - [ContactPickMode.Email] uses `Email.CONTENT_TYPE`, so only contacts with an email address are
 *   selectable (the add/share flow, unchanged behaviour).
 * - [ContactPickMode.EmailAndPhone] uses the general `Contacts.CONTENT_TYPE` directory type rather
 *   than a data-kind type, so phone-only contacts are surfaced alongside email contacts (the invite
 *   flow). A broadened type is preferred over dropping the `type` filter entirely because it keeps
 *   the picker pointed at the contacts directory while still surfacing every contact.
 *
 * [parseResult] returns the session [Uri] handed back by the picker as-is; resolving that Uri into
 * contacts is done by the ViewModel via `GetLocalContactsFromUriUseCase`, so no `ContentResolver`
 * work happens here.
 *
 * This contract is only launched on devices at or above `ACTION_PICK_CONTACTS`'s minimum SDK
 * (see [AddContactViewModel.ANDROID_PICKER_MIN_SDK]).
 */
class PickPhoneContactContract : ActivityResultContract<ContactPickMode, Uri?>() {

    override fun createIntent(context: Context, input: ContactPickMode): Intent =
        Intent(ACTION_PICK_CONTACTS).apply {
            type = when (input) {
                ContactPickMode.Email -> ContactsContract.CommonDataKinds.Email.CONTENT_TYPE
                ContactPickMode.EmailAndPhone -> ContactsContract.Contacts.CONTENT_TYPE
            }
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data

    private companion object {
        const val ACTION_PICK_CONTACTS = "android.intent.action.PICK_CONTACTS"
    }
}
