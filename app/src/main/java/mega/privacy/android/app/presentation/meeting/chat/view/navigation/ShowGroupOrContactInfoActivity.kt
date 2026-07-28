package mega.privacy.android.app.presentation.meeting.chat.view.navigation

import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import mega.privacy.android.app.main.megachat.GroupChatInfoActivity
import mega.privacy.android.app.presentation.meeting.ChatInfoActivity
import mega.privacy.android.app.presentation.meeting.chat.model.ChatUiState
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.navigation.MegaNavigatorEntryPoint
import mega.privacy.android.navigation.destination.ChatNavKey
import timber.log.Timber

fun showGroupOrContactInfoActivity(context: Context, uiState: ChatUiState) {
    with(uiState) {
        when {
            isNoteToSelf -> {
                Intent(context, ChatInfoActivity::class.java).apply {
                    putExtra(ChatNavKey.LEGACY_CHAT_ID, chatId)
                }.also {
                    context.startActivity(it)
                }
            }

            schedIsPending && isActive -> {
                Timber.d("show scheduled meeting info")
                Intent(context, ChatInfoActivity::class.java).apply {
                    putExtra(ChatNavKey.LEGACY_CHAT_ID, scheduledMeeting?.chatId)
                    putExtra(Constants.SCHEDULED_MEETING_ID, scheduledMeeting?.schedId)
                }.also {
                    context.startActivity(it)
                }
            }

            else -> {
                if (isGroup) {
                    Intent(context, GroupChatInfoActivity::class.java).apply {
                        putExtra(Constants.HANDLE, chatId)
                    }.also {
                        context.startActivity(it)
                    }
                } else {
                    EntryPointAccessors.fromApplication(context, MegaNavigatorEntryPoint::class.java)
                        .megaNavigator
                        .openContactInfoActivity(context, chatId)
                }
            }
        }
    }
}