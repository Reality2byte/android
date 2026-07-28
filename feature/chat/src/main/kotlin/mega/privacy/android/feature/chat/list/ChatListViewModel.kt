package mega.privacy.android.feature.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import mega.privacy.android.core.coroutine.asUiStateFlow
import mega.privacy.android.domain.usecase.chat.GetChatListItemUseCase
import mega.privacy.android.domain.usecase.chat.GetChatsUseCase
import mega.privacy.android.domain.usecase.chat.GetChatsUseCase.ChatRoomType
import mega.privacy.android.feature.chat.list.mapper.ChatRoomTimestampMapper
import mega.privacy.android.feature.chat.list.mapper.ChatRoomUiItemMapper
import mega.privacy.android.feature.chat.list.model.ChatListUiState
import mega.privacy.android.feature.chat.list.model.ChatRoomUiItem
import timber.log.Timber
import javax.inject.Inject

/**
 * View model for the chat list screen exposing the Chats and Meetings tabs content.
 */
@HiltViewModel
internal class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase,
    private val getChatListItemUseCase: GetChatListItemUseCase,
    private val chatRoomTimestampMapper: ChatRoomTimestampMapper,
    private val chatRoomUiItemMapper: ChatRoomUiItemMapper,
) : ViewModel() {

    /**
     * UI state for the chat list screen.
     */
    val uiState: StateFlow<ChatListUiState> by lazy(LazyThreadSafetyMode.NONE) {
        combine(
            chatRoomsFlow(ChatRoomType.NON_MEETINGS),
            chatRoomsFlow(ChatRoomType.MEETINGS),
        ) { chats, meetings ->
            ChatListUiState.Data(
                chats = chats,
                meetings = meetings,
            )
        }.catch { e ->
            Timber.e(e, "Failed to load chat list")
        }.asUiStateFlow(
            viewModelScope,
            ChatListUiState.Loading,
        )
    }

    private fun chatRoomsFlow(chatRoomType: ChatRoomType): Flow<ImmutableList<ChatRoomUiItem>> =
        getChatsUseCase(
            chatRoomType = chatRoomType,
            lastMessage = { chatId -> getChatListItemUseCase(chatId)?.lastMessage.orEmpty() },
            lastTimeMapper = chatRoomTimestampMapper::getLastTimeFormatted,
            meetingTimeMapper = chatRoomTimestampMapper::getMeetingTimeFormatted,
            headerTimeMapper = { _, _ -> null },
        ).map { items ->
            items.map(chatRoomUiItemMapper::invoke).toImmutableList()
        }.catch { e ->
            Timber.e(e, "Failed to load $chatRoomType chat rooms")
        }
}
