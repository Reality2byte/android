package mega.privacy.android.feature.contact.info.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import mega.android.core.ui.components.dialogs.BasicInputDialog
import mega.android.core.ui.components.dialogs.MegaDialogProperties
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Dialog to set, edit or remove the nickname of a contact.
 *
 * When [nickname] is null the dialog is in "set" mode: empty field and a cancel button. When a
 * nickname exists the dialog is in "edit" mode: the field is pre-filled with the nickname selected
 * and the secondary button removes the nickname instead (removal is non-destructive and instantly
 * reversible, and the dialog can still be cancelled by tapping outside or pressing back).
 *
 * @param nickname current nickname of the contact, or null when none is set.
 * @param onConfirm invoked with the entered nickname when the set button is clicked.
 * @param onRemove invoked when the remove button is clicked in edit mode.
 * @param onDismiss invoked when the dialog is cancelled or dismissed.
 * @param modifier
 */
@Composable
internal fun NicknameDialog(
    nickname: String?,
    onConfirm: (String) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = nickname.orEmpty(),
                selection = TextRange(0, nickname.orEmpty().length),
            )
        )
    }
    val isEditing = nickname != null
    BasicInputDialog(
        modifier = modifier.testTag(NICKNAME_DIALOG_TAG),
        title = stringResource(
            if (isEditing) {
                sharedR.string.contact_info_edit_nickname
            } else {
                sharedR.string.contact_info_set_nickname
            }
        ),
        inputValue = input,
        onValueChange = { input = it },
        placeholder = stringResource(sharedR.string.contact_info_nickname_dialog_hint),
        positiveButtonText = stringResource(sharedR.string.contact_info_nickname_dialog_set_button),
        onPositiveButtonClicked = { onConfirm(input.text) },
        negativeButtonText = stringResource(
            if (isEditing) {
                sharedR.string.general_remove
            } else {
                sharedR.string.general_dialog_cancel_button
            }
        ),
        onNegativeButtonClicked = if (isEditing) onRemove else onDismiss,
        dialogProperties = MegaDialogProperties.default.copy(
            isPositiveButtonEnabled = input.text.isNotBlank(),
        ),
        onDismiss = onDismiss,
    )
}

internal const val NICKNAME_DIALOG_TAG = "nickname_dialog"

@CombinedThemePreviews
@Composable
private fun NicknameDialogSetPreview() {
    AndroidThemeForPreviews {
        NicknameDialog(
            nickname = null,
            onConfirm = {},
            onRemove = {},
            onDismiss = {},
        )
    }
}

@CombinedThemePreviews
@Composable
private fun NicknameDialogEditPreview() {
    AndroidThemeForPreviews {
        NicknameDialog(
            nickname = "Ally",
            onConfirm = {},
            onRemove = {},
            onDismiss = {},
        )
    }
}
