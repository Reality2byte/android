package mega.privacy.android.feature.contact.info.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.tools.screenshot.PreviewTest
import de.palm.composestateevents.consumed
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.domain.entity.contacts.UserChatStatus
import mega.privacy.android.feature.contact.info.model.ContactInfoUiState
import mega.privacy.android.shared.contact.model.AvatarData

class ContactInfoScreenScreenshotTest {

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
        )
    }
}
