package mega.privacy.android.app.deeplinks.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import mega.android.core.ui.components.dialogs.BasicDialog
import mega.android.core.ui.components.dialogs.BasicDialogButton
import mega.android.core.ui.components.text.SpannableText
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.shared.original.core.ui.preview.CombinedThemeRtlPreviews
import mega.privacy.android.shared.resources.R as sharedR

@Composable
internal fun DeepLinkContentUnavailableDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicDialog(
        modifier = modifier.testTag(DEEP_LINK_CONTENT_UNAVAILABLE_DIALOG_TEST_TAG),
        title = SpannableText(stringResource(sharedR.string.deep_link_content_unavailable_dialog_message)),
        buttons = persistentListOf(
            BasicDialogButton(
                text = stringResource(sharedR.string.general_ok),
                onClick = onDismiss
            )
        ),
        onDismissRequest = onDismiss,
    )
}

@CombinedThemeRtlPreviews
@Composable
private fun DeepLinkContentUnavailableDialogPreview() {
    AndroidThemeForPreviews {
        DeepLinkContentUnavailableDialog(onDismiss = {})
    }
}

internal const val DEEP_LINK_CONTENT_UNAVAILABLE_DIALOG_TEST_TAG =
    "deep_link_content_unavailable_dialog"
