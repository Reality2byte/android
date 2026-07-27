package mega.privacy.android.feature.contact.info.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import mega.android.core.ui.components.dialogs.BasicDialog
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Confirmation dialog shown before removing the contact.
 *
 * @param onConfirm invoked when the remove button is clicked.
 * @param onDismiss invoked when the dialog is cancelled or dismissed.
 * @param modifier
 */
@Composable
internal fun RemoveContactConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicDialog(
        modifier = modifier.testTag(REMOVE_CONTACT_CONFIRMATION_DIALOG_TAG),
        title = stringResource(sharedR.string.contact_info_remove_contact_dialog_title),
        description = stringResource(sharedR.string.contact_info_remove_contact_dialog_message),
        positiveButtonText = stringResource(sharedR.string.general_remove),
        onPositiveButtonClicked = onConfirm,
        negativeButtonText = stringResource(sharedR.string.general_dialog_cancel_button),
        onNegativeButtonClicked = onDismiss,
        onDismiss = onDismiss,
    )
}

internal const val REMOVE_CONTACT_CONFIRMATION_DIALOG_TAG = "remove_contact_confirmation_dialog"

@CombinedThemePreviews
@Composable
private fun RemoveContactConfirmationDialogPreview() {
    AndroidThemeForPreviews {
        RemoveContactConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
