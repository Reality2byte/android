package mega.privacy.android.feature.contact.info.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.palm.composestateevents.EventEffect
import de.palm.composestateevents.consumed
import mega.android.core.ui.components.LocalSnackBarHostState
import mega.android.core.ui.components.MegaScaffoldWithTopAppBarScrollBehavior
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.toolbar.AppBarNavigationType
import mega.android.core.ui.components.toolbar.MegaTopAppBar
import mega.android.core.ui.extensions.showAutoDurationSnackbar
import mega.android.core.ui.modifiers.shimmerEffect
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.domain.entity.chat.ChatPushNotificationMuteOption
import mega.privacy.android.domain.entity.contacts.UserChatStatus
import mega.android.core.ui.theme.values.TextColor
import mega.privacy.android.feature.contact.info.model.ContactInfoMessage
import mega.privacy.android.feature.contact.info.model.ContactInfoUiState
import mega.privacy.android.feature.contact.info.view.dialog.MutePushNotificationDialogM3
import mega.privacy.android.feature.contact.info.view.dialog.NicknameDialog
import mega.privacy.android.feature.contact.info.view.dialog.RemoveContactConfirmationDialog
import mega.privacy.android.shared.contact.model.AvatarData
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Contact info screen: shows a shimmer placeholder while the contact is being resolved, then the
 * resolved contact's info sections via [ContactInfoContent]. Owns the visibility of the nickname,
 * remove-contact and mute-options dialogs.
 *
 * @param state
 * @param onNavigateBack invoked when the user navigates back.
 * @param onSendMessageClick
 * @param onStartAudioCallClick
 * @param onStartVideoCallClick
 * @param onUpdateNickname invoked with the new nickname when the nickname dialog is confirmed,
 * or with null when the nickname is removed.
 * @param onVerifyCredentialsClick
 * @param onShareContactClick
 * @param onSharedFoldersClick
 * @param onNotificationToggled invoked with the new checked value when the notifications toggle
 * is switched.
 * @param onMuteOptionSelected invoked with the selected option when the mute options dialog is
 * confirmed.
 * @param onMuteOptionsEventConsumed invoked once the mute options event has been consumed.
 * @param onSharedFilesClick
 * @param onManageChatHistoryClick
 * @param onRemoveContact invoked when the remove-contact dialog is confirmed.
 * @param onMessageEventConsumed invoked once the message event has been consumed.
 * @param modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactInfoScreen(
    state: ContactInfoUiState,
    onNavigateBack: () -> Unit,
    onSendMessageClick: () -> Unit,
    onStartAudioCallClick: () -> Unit,
    onStartVideoCallClick: () -> Unit,
    onUpdateNickname: (String?) -> Unit,
    onVerifyCredentialsClick: () -> Unit,
    onShareContactClick: () -> Unit,
    onSharedFoldersClick: () -> Unit,
    onNotificationToggled: (Boolean) -> Unit,
    onMuteOptionSelected: (ChatPushNotificationMuteOption) -> Unit,
    onMuteOptionsEventConsumed: () -> Unit,
    onSharedFilesClick: () -> Unit,
    onManageChatHistoryClick: () -> Unit,
    onRemoveContact: () -> Unit,
    onMessageEventConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MegaScaffoldWithTopAppBarScrollBehavior(
        modifier = modifier
            .fillMaxSize()
            .testTag(CONTACT_INFO_SCREEN_TAG),
        topBar = {
            MegaTopAppBar(
                title = stringResource(sharedR.string.contacts_action_contact_info),
                navigationType = AppBarNavigationType.Back(onNavigateBack),
            )
        },
    ) { padding ->
        when (state) {
            is ContactInfoUiState.Loading -> ContactInfoLoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag(CONTACT_INFO_LOADING_TAG),
            )

            is ContactInfoUiState.Data -> {
                var showNicknameDialog by rememberSaveable { mutableStateOf(false) }
                var showRemoveContactDialog by rememberSaveable { mutableStateOf(false) }
                var muteOptions by rememberSaveable {
                    mutableStateOf<List<ChatPushNotificationMuteOption>?>(null)
                }
                val snackbarHostState = LocalSnackBarHostState.current
                val nicknameAddedMessage =
                    stringResource(sharedR.string.contact_info_nickname_added)
                val nicknameRemovedMessage =
                    stringResource(sharedR.string.contact_info_nickname_removed)
                val chatCreationErrorMessage =
                    stringResource(sharedR.string.contact_info_create_chat_error)
                EventEffect(
                    event = state.messageEvent,
                    onConsumed = onMessageEventConsumed,
                ) { message ->
                    snackbarHostState?.showAutoDurationSnackbar(
                        when (message) {
                            ContactInfoMessage.NicknameAdded -> nicknameAddedMessage
                            ContactInfoMessage.NicknameRemoved -> nicknameRemovedMessage
                            ContactInfoMessage.ChatCreationError -> chatCreationErrorMessage
                        }
                    )
                }
                EventEffect(
                    event = state.showMuteOptionsEvent,
                    onConsumed = onMuteOptionsEventConsumed,
                ) { options ->
                    muteOptions = options
                }
                ContactInfoContent(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onSendMessageClick = onSendMessageClick,
                    onStartAudioCallClick = onStartAudioCallClick,
                    onStartVideoCallClick = onStartVideoCallClick,
                    onNicknameClick = { showNicknameDialog = true },
                    onVerifyCredentialsClick = onVerifyCredentialsClick,
                    onShareContactClick = onShareContactClick,
                    onSharedFoldersClick = onSharedFoldersClick,
                    onNotificationToggled = onNotificationToggled,
                    onSharedFilesClick = onSharedFilesClick,
                    onManageChatHistoryClick = onManageChatHistoryClick,
                    onRemoveContactClick = { showRemoveContactDialog = true },
                )
                if (showNicknameDialog) {
                    NicknameDialog(
                        nickname = state.nickname,
                        onConfirm = { newNickname ->
                            onUpdateNickname(newNickname)
                            showNicknameDialog = false
                        },
                        onRemove = {
                            onUpdateNickname(null)
                            showNicknameDialog = false
                        },
                        onDismiss = { showNicknameDialog = false },
                    )
                }
                if (showRemoveContactDialog) {
                    RemoveContactConfirmationDialog(
                        onConfirm = {
                            onRemoveContact()
                            showRemoveContactDialog = false
                        },
                        onDismiss = { showRemoveContactDialog = false },
                    )
                }
                muteOptions?.let { options ->
                    MutePushNotificationDialogM3(
                        muteOptions = options,
                        onConfirm = { option ->
                            onMuteOptionSelected(option)
                            muteOptions = null
                        },
                        onDismiss = { muteOptions = null },
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactInfoLoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .shimmerEffect(CircleShape),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(RoundedCornerShape(4.dp)),
            )
        }
        SKELETON_ROW_WIDTHS.forEach { widthFraction ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(RoundedCornerShape(4.dp)),
            )
        }
    }
}

private val SKELETON_ROW_WIDTHS = listOf(0.7f, 0.5f, 0.6f, 0.4f)

internal const val CONTACT_INFO_SCREEN_TAG = "contact_info_screen"
internal const val CONTACT_INFO_LOADING_TAG = "contact_info_screen:loading_view"
internal const val CONTACT_INFO_NAME_TAG = "contact_info_screen:text_name"
internal const val CONTACT_INFO_EMAIL_TAG = "contact_info_screen:text_email"

@CombinedThemePreviews
@Composable
private fun ContactInfoScreenLoadingPreview() {
    AndroidThemeForPreviews {
        ContactInfoScreen(
            state = ContactInfoUiState.Loading(closeEvent = consumed),
            onNavigateBack = {},
            onSendMessageClick = {},
            onStartAudioCallClick = {},
            onStartVideoCallClick = {},
            onUpdateNickname = {},
            onVerifyCredentialsClick = {},
            onShareContactClick = {},
            onSharedFoldersClick = {},
            onNotificationToggled = {},
            onMuteOptionSelected = {},
            onMuteOptionsEventConsumed = {},
            onSharedFilesClick = {},
            onManageChatHistoryClick = {},
            onRemoveContact = {},
            onMessageEventConsumed = {},
        )
    }
}

@CombinedThemePreviews
@Composable
private fun ContactInfoScreenLoadedPreview() {
    AndroidThemeForPreviews {
        ContactInfoScreen(
            state = ContactInfoUiState.Data(
                displayName = "Alice Anderson",
                nickname = "Ally",
                email = "alice@example.com",
                userHandle = 1L,
                chatRoomId = 123L,
                isFromContacts = true,
                avatar = AvatarData.Initials(initials = "A", avatarColor = Color(0xFF2E7D32)),
                userChatStatus = UserChatStatus.Online,
                lastSeenMinutes = null,
                areCredentialsVerified = true,
                isNotificationEnabled = true,
                notificationsMutedUntilTimestamp = null,
                retentionTimeSeconds = SECONDS_IN_DAY,
                inSharesCount = 3,
                enableCallButtons = true,
                isOnline = true,
                showMuteOptionsEvent = consumed(),
                messageEvent = consumed(),
                closeEvent = consumed,
            ),
            onNavigateBack = {},
            onSendMessageClick = {},
            onStartAudioCallClick = {},
            onStartVideoCallClick = {},
            onUpdateNickname = {},
            onVerifyCredentialsClick = {},
            onShareContactClick = {},
            onSharedFoldersClick = {},
            onNotificationToggled = {},
            onMuteOptionSelected = {},
            onMuteOptionsEventConsumed = {},
            onSharedFilesClick = {},
            onManageChatHistoryClick = {},
            onRemoveContact = {},
            onMessageEventConsumed = {},
        )
    }
}
