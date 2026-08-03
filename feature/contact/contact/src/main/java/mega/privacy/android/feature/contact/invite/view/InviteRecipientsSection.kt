package mega.privacy.android.feature.contact.invite.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.chip.MegaChip
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.components.inputfields.TextInputField
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.values.IconColor
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Recipient entry for the invite flow: a free-text field that accepts either an email address or a
 * phone number, plus the already-selected recipients rendered as removable chips.
 *
 * Unlike the add/share flow the field does not validate synchronously: [onSubmitInput] hands the raw
 * text to the ViewModel, which decides whether it becomes a recipient (surfaced back through the
 * selection) or a validation snackbar. The field is therefore cleared optimistically on submit.
 *
 * @param emails the selected email recipients (picked-contact and manually typed), rendered as chips.
 * @param phoneNumbers the selected phone-number recipients, rendered as chips.
 * @param onSubmitInput invoked with the trimmed typed text when the add affordance is used.
 * @param onRemoveEmail invoked with the email of a clicked chip to remove it from the selection.
 * @param onRemovePhoneNumber invoked with the phone number of a clicked chip to remove it.
 * @param modifier
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InviteRecipientsSection(
    emails: Set<String>,
    phoneNumbers: Set<String>,
    onSubmitInput: (String) -> Unit,
    onRemoveEmail: (String) -> Unit,
    onRemovePhoneNumber: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var textValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    fun submit() {
        val input = textValue.text.trim()
        if (input.isEmpty()) return
        onSubmitInput(input)
        textValue = TextFieldValue("")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(INVITE_RECIPIENTS_SECTION_TAG),
    ) {
        TextInputField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(INVITE_RECIPIENTS_INPUT_TAG),
            keyboardType = KeyboardType.Email,
            textFieldValue = textValue,
            capitalization = KeyboardCapitalization.None,
            placeholder = stringResource(sharedR.string.invite_contacts_manual_input_placeholder),
            onValueChanged = { textValue = it },
            trailingView = {
                IconButton(
                    modifier = Modifier.testTag(INVITE_RECIPIENTS_ADD_TAG),
                    onClick = ::submit,
                    enabled = textValue.text.isNotBlank(),
                ) {
                    MegaIcon(
                        modifier = Modifier.size(24.dp),
                        painter = rememberVectorPainter(IconPack.Medium.Thin.Outline.Plus),
                        contentDescription = stringResource(sharedR.string.invite_contacts_share_link_action),
                        tint = IconColor.Accent,
                    )
                }
            },
        )
        if (emails.isNotEmpty() || phoneNumbers.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag(INVITE_RECIPIENTS_CHIPS_TAG),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                emails.forEach { email ->
                    MegaChip(
                        selected = false,
                        content = email,
                        trailingPainter = rememberVectorPainter(IconPack.Medium.Thin.Outline.X),
                        onClick = { onRemoveEmail(email) },
                    )
                }
                phoneNumbers.forEach { phone ->
                    MegaChip(
                        selected = false,
                        content = phone,
                        trailingPainter = rememberVectorPainter(IconPack.Medium.Thin.Outline.X),
                        onClick = { onRemovePhoneNumber(phone) },
                    )
                }
            }
        }
    }
}

private class InviteRecipientsProvider : PreviewParameterProvider<Pair<Set<String>, Set<String>>> {
    override val values: Sequence<Pair<Set<String>, Set<String>>> = sequenceOf(
        emptySet<String>() to emptySet(),
        setOf("guest@example.com") to setOf("+1555123456"),
    )
}

@CombinedThemePreviews
@Composable
private fun InviteRecipientsSectionPreview(
    @PreviewParameter(InviteRecipientsProvider::class) recipients: Pair<Set<String>, Set<String>>,
) {
    AndroidThemeForPreviews {
        InviteRecipientsSection(
            emails = recipients.first,
            phoneNumbers = recipients.second,
            onSubmitInput = {},
            onRemoveEmail = {},
            onRemovePhoneNumber = {},
        )
    }
}
