package mega.privacy.android.feature.contact.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsPickerSessionContract
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

/**
 * Activity result contract that opens the Android 17 (API 37) system contacts picker via
 * [ContactsPickerSessionContract.ACTION_PICK_CONTACTS].
 *
 * That picker rejects an intent that only carries the action; it requires the requested data
 * fields to be declared explicitly (otherwise it fails at runtime with
 * `Missing or empty EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS`). [createIntent] therefore maps the
 * [ContactPickMode] to the MIME types the picker should offer, so the mode now drives the picker
 * itself:
 * - [ContactPickMode.Email] requests only email addresses.
 * - [ContactPickMode.EmailAndPhone] requests both email addresses and phone numbers, so phone-only
 *   contacts also surface.
 *
 * [ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS] is set to `false` so a
 * contact having ANY of the requested fields is offered (matching all of them would hide, for
 * example, phone-only contacts in the email+phone flow).
 *
 * No `type` is set on the intent: the action resolves by itself and setting a MIME type would break
 * resolution.
 *
 * [parseResult] returns the session [Uri] handed back by the picker as-is; resolving that Uri into
 * contacts is done by the ViewModel via `GetLocalContactsFromUriUseCase`, so no `ContentResolver`
 * work happens here.
 *
 * These APIs exist only on API 37+, so this contract is only launched on devices at or above the
 * picker's minimum SDK (see [AddContactViewModel.ANDROID_PICKER_MIN_SDK]).
 */
class PickPhoneContactContract : ActivityResultContract<ContactPickMode, Uri?>() {

    @RequiresApi(37)
    override fun createIntent(context: Context, input: ContactPickMode): Intent =
        Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
            putExtra(Intent.EXTRA_USE_SYSTEM_CONTACTS_PICKER, true)
            putStringArrayListExtra(
                ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
                input.requestedDataFields(),
            )
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS, false)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data

    private fun ContactPickMode.requestedDataFields(): ArrayList<String> = when (this) {
        ContactPickMode.Email -> arrayListOf(Email.CONTENT_ITEM_TYPE)
        ContactPickMode.EmailAndPhone -> arrayListOf(Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE)
    }
}
