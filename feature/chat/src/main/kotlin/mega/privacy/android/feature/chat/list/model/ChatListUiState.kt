package mega.privacy.android.feature.chat.list.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

/**
 * UI state for the chat list screen.
 */
@Stable
sealed interface ChatListUiState {

    /**
     * Chats and meetings are still loading.
     */
    data object Loading : ChatListUiState

    /**
     * Chats and meetings are loaded.
     *
     * @property chats Chat rooms of the Chats tab.
     * @property meetings Chat rooms of the Meetings tab.
     */
    data class Data(
        val chats: ImmutableList<ChatRoomUiItem>,
        val meetings: ImmutableList<ChatRoomUiItem>,
    ) : ChatListUiState
}
