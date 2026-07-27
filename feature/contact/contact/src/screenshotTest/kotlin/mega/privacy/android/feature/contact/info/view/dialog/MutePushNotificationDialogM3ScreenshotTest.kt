package mega.privacy.android.feature.contact.info.view.dialog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.domain.entity.chat.ChatPushNotificationMuteOption

class MutePushNotificationDialogM3ScreenshotTest {

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun MutePushNotificationOptions() {
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

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun MutePushNotificationOptionsUntilMorning() {
        AndroidThemeForPreviews {
            MutePushNotificationDialogM3(
                muteOptions = listOf(
                    ChatPushNotificationMuteOption.Mute30Minutes,
                    ChatPushNotificationMuteOption.Mute1Hour,
                    ChatPushNotificationMuteOption.Mute6Hours,
                    ChatPushNotificationMuteOption.Mute24Hours,
                    ChatPushNotificationMuteOption.MuteUntilTomorrowMorning,
                ),
                onConfirm = {},
                onDismiss = {},
            )
        }
    }

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun MutePushNotificationOptionsUntilThisMorning() {
        AndroidThemeForPreviews {
            MutePushNotificationDialogM3(
                muteOptions = listOf(
                    ChatPushNotificationMuteOption.Mute30Minutes,
                    ChatPushNotificationMuteOption.Mute1Hour,
                    ChatPushNotificationMuteOption.Mute6Hours,
                    ChatPushNotificationMuteOption.Mute24Hours,
                    ChatPushNotificationMuteOption.MuteUntilThisMorning,
                ),
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
