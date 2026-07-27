package mega.privacy.android.feature.contact.info.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import mega.android.core.ui.components.dialogs.BasicDialogButton
import mega.android.core.ui.components.dialogs.BasicDialogRadioOption
import mega.android.core.ui.components.dialogs.BasicRadioDialog
import mega.android.core.ui.components.text.SpannableText
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.domain.entity.chat.ChatPushNotificationMuteOption
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Dialog offering the durations to mute the chat notifications with the contact for.
 *
 * @param muteOptions the mute durations to offer.
 * @param onConfirm invoked with the selected mute option when the confirm button is clicked.
 * @param onDismiss invoked when the dialog is cancelled or dismissed.
 * @param modifier
 */
@Composable
internal fun MutePushNotificationDialogM3(
    muteOptions: List<ChatPushNotificationMuteOption>,
    onConfirm: (ChatPushNotificationMuteOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedOrdinal by rememberSaveable { mutableStateOf<Int?>(null) }
    val options = muteOptions.mapNotNull { option ->
        muteOptionLabel(option)?.let { label ->
            BasicDialogRadioOption(ordinal = option.ordinal, text = label)
        }
    }.toImmutableList()
    BasicRadioDialog(
        modifier = modifier.testTag(MUTE_PUSH_NOTIFICATION_DIALOG_TAG),
        title = SpannableText(
            stringResource(sharedR.string.contact_info_mute_notifications_dialog_title)
        ),
        options = options,
        selectedOption = options.firstOrNull { it.ordinal == selectedOrdinal },
        onOptionSelected = { selectedOrdinal = it.ordinal },
        onDismissRequest = onDismiss,
        buttons = persistentListOf(
            BasicDialogButton(
                text = stringResource(sharedR.string.general_dialog_cancel_button),
                onClick = onDismiss,
            ),
            BasicDialogButton(
                text = stringResource(sharedR.string.general_ok_only),
                onClick = {
                    ChatPushNotificationMuteOption.entries
                        .firstOrNull { it.ordinal == selectedOrdinal }
                        ?.let(onConfirm)
                },
                enabled = selectedOrdinal != null,
            ),
        ),
    )
}

@Composable
private fun muteOptionLabel(option: ChatPushNotificationMuteOption): String? = when (option) {
    ChatPushNotificationMuteOption.Mute30Minutes -> pluralStringResource(
        sharedR.plurals.contact_info_mute_option_minutes,
        MUTE_30_MINUTES,
        MUTE_30_MINUTES,
    )

    ChatPushNotificationMuteOption.Mute1Hour -> pluralStringResource(
        sharedR.plurals.contact_info_mute_option_hours,
        MUTE_1_HOUR,
        MUTE_1_HOUR,
    )

    ChatPushNotificationMuteOption.Mute6Hours -> pluralStringResource(
        sharedR.plurals.contact_info_mute_option_hours,
        MUTE_6_HOURS,
        MUTE_6_HOURS,
    )

    ChatPushNotificationMuteOption.Mute24Hours -> pluralStringResource(
        sharedR.plurals.contact_info_mute_option_hours,
        MUTE_24_HOURS,
        MUTE_24_HOURS,
    )

    ChatPushNotificationMuteOption.MuteUntilThisMorning ->
        stringResource(sharedR.string.contact_info_mute_option_until_this_morning)

    ChatPushNotificationMuteOption.MuteUntilTomorrowMorning ->
        stringResource(sharedR.string.contact_info_mute_option_until_tomorrow_morning)

    ChatPushNotificationMuteOption.MuteUntilTurnBackOn ->
        stringResource(sharedR.string.contact_info_mute_option_until_turned_back_on)

    else -> null
}

internal const val MUTE_PUSH_NOTIFICATION_DIALOG_TAG = "mute_push_notification_dialog"

private const val MUTE_30_MINUTES = 30
private const val MUTE_1_HOUR = 1
private const val MUTE_6_HOURS = 6
private const val MUTE_24_HOURS = 24

@CombinedThemePreviews
@Composable
private fun MutePushNotificationDialogM3Preview() {
    AndroidThemeForPreviews {
        MutePushNotificationDialogM3(
            muteOptions = listOf(
                ChatPushNotificationMuteOption.Mute30Minutes,
                ChatPushNotificationMuteOption.Mute1Hour,
                ChatPushNotificationMuteOption.Mute6Hours,
                ChatPushNotificationMuteOption.Mute24Hours,
                ChatPushNotificationMuteOption.MuteUntilTurnBackOn,
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
