package mega.privacy.android.feature.chat.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mega.privacy.android.feature.chat.list.ChatListViewModel
import mega.privacy.android.feature.chat.list.view.ChatListScreen
import mega.privacy.android.navigation.contract.NavigationHandler
import mega.privacy.android.navigation.destination.ShowChatMessagesNavKey

/**
 * Chat list entry rendering the Compose [ChatListScreen] with Chats and Meetings tabs.
 *
 * Hosted by the app module's gated `ChatListNavKey` destination.
 *
 * @param navigationHandler
 * @param showMeetingTab Whether the Meetings tab should be initially selected.
 */
@SuppressLint("ComposeViewModelInjection")
@Composable
fun ChatListEntry(
    navigationHandler: NavigationHandler,
    showMeetingTab: Boolean,
) {
    val viewModel = hiltViewModel<ChatListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatListScreen(
        uiState = uiState,
        showMeetingTab = showMeetingTab,
        onBackClick = navigationHandler::back,
        onItemClick = { chatId ->
            navigationHandler.navigate(ShowChatMessagesNavKey(chatId))
        },
    )
}
