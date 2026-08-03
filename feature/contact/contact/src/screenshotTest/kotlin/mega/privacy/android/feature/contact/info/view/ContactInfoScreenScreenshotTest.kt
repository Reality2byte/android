package mega.privacy.android.feature.contact.info.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import de.palm.composestateevents.consumed
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.values.IconColor
import mega.android.core.ui.theme.values.TextColor
import mega.privacy.android.domain.entity.contacts.UserChatStatus
import mega.privacy.android.feature.contact.info.model.ContactInfoUiState
import mega.privacy.android.shared.contact.model.AvatarData
import mega.privacy.android.shared.resources.R as sharedR

class ContactInfoScreenScreenshotTest {

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun ContactInfoSendFileAction() {
        AndroidThemeForPreviews {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MegaIcon(
                    painter = SendFileMenuAction.getIconPainter(),
                    tint = IconColor.Primary,
                )
                MegaText(
                    text = SendFileMenuAction.getDescription(),
                    textColor = TextColor.Primary,
                )
            }
        }
    }

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun ContactInfoMicrophonePermissionSnackbar() {
        AndroidThemeForPreviews {
            Snackbar(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(
                        sharedR.string.contact_info_microphone_permission_message
                    ),
                )
            }
        }
    }

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun ContactInfoScreenLoading() {
        AndroidThemeForPreviews {
            ContactInfoScreenUnderTest(state = ContactInfoUiState.Loading(closeEvent = consumed))
        }
    }

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun ContactInfoScreenLoaded() {
        AndroidThemeForPreviews {
            ContactInfoScreenUnderTest(
                state = ContactInfoUiState.Data(
                    displayName = "Alice Anderson",
                    nickname = "Ally",
                    email = "alice@example.com",
                    userHandle = 1L,
                    chatRoomId = 123L,
                    isFromContacts = true,
                    avatar = AvatarData.Initials(
                        initials = "A",
                        avatarColor = Color(0xFF2E7D32),
                    ),
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
                    openChatEvent = consumed(),
                    startCallEvent = consumed(),
                    storageOverQuotaEvent = consumed,
                    closeEvent = consumed,
                ),
            )
        }
    }

    @Composable
    private fun ContactInfoScreenUnderTest(state: ContactInfoUiState) {
        ContactInfoScreen(
            state = state,
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
            onSendFileClick = {},
        )
    }
}
