package mega.privacy.android.feature.contact.info

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.palm.composestateevents.StateEvent
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mega.privacy.android.core.coroutine.asUiStateFlow
import mega.privacy.android.domain.entity.chat.ChatPushNotificationMuteOption
import mega.privacy.android.domain.entity.contacts.ContactInfoState
import mega.privacy.android.domain.entity.contacts.ContactItem
import mega.privacy.android.domain.usecase.chat.CreateChatRoomUseCase
import mega.privacy.android.domain.usecase.chat.GetChatMuteOptionListUseCase
import mega.privacy.android.domain.usecase.chat.MuteChatNotificationForChatRoomsUseCase
import mega.privacy.android.domain.usecase.contact.MonitorContactInfoUseCase
import mega.privacy.android.domain.usecase.contact.RemoveContactByEmailUseCase
import mega.privacy.android.domain.usecase.contact.SetUserAliasUseCase
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.feature.contact.info.model.ContactInfoMessage
import mega.privacy.android.feature.contact.info.model.ContactInfoUiState
import mega.privacy.android.shared.contact.mapper.ContactItemAvatarMapper
import mega.privacy.android.shared.contact.model.AvatarData
import timber.log.Timber
import java.util.Locale

/**
 * Contact info view model. Exposes the aggregated live information of the peer resolved either
 * from an [email] (contact list entry point) or from a [chatId] (1:1 chat entry point) through
 * [MonitorContactInfoUseCase]. When nothing can be resolved the monitor flow fails and a close
 * event is fired so the screen can pop itself.
 *
 * @property email email of the contact, or null when entering from a chat.
 * @property chatId id of the 1:1 chat with the contact, or null when entering by email.
 */
@HiltViewModel(assistedFactory = ContactInfoViewModel.Factory::class)
internal class ContactInfoViewModel @AssistedInject constructor(
    @Assisted private val email: String?,
    @Assisted private val chatId: Long?,
    private val monitorContactInfoUseCase: MonitorContactInfoUseCase,
    private val monitorConnectivityUseCase: MonitorConnectivityUseCase,
    private val setUserAliasUseCase: SetUserAliasUseCase,
    private val removeContactByEmailUseCase: RemoveContactByEmailUseCase,
    private val createChatRoomUseCase: CreateChatRoomUseCase,
    private val muteChatNotificationForChatRoomsUseCase: MuteChatNotificationForChatRoomsUseCase,
    private val getChatMuteOptionListUseCase: GetChatMuteOptionListUseCase,
    private val contactItemAvatarMapper: ContactItemAvatarMapper,
) : ViewModel() {

    /**
     * Factory for assisted creation, supplying the entry point arguments from the navigation key.
     */
    @AssistedFactory
    interface Factory {
        /**
         * @param email email of the contact, or null when entering from a chat.
         * @param chatId id of the 1:1 chat with the contact, or null when entering by email.
         */
        fun create(email: String?, chatId: Long?): ContactInfoViewModel
    }

    private val closeEventChannel = Channel<StateEvent>(Channel.BUFFERED)
    private val muteOptionsEventChannel =
        Channel<StateEventWithContent<List<ChatPushNotificationMuteOption>>>(Channel.BUFFERED)
    private val messageEventChannel =
        Channel<StateEventWithContent<ContactInfoMessage>>(Channel.BUFFERED)

    /**
     * Ui state
     */
    val uiState: StateFlow<ContactInfoUiState> by lazy(LazyThreadSafetyMode.NONE) {
        combine(
            monitorContactInfoUseCase(email, chatId),
            monitorConnectivityUseCase(),
            closeEventChannel.receiveAsFlow().onStart { emit(consumed) },
            muteOptionsEventChannel.receiveAsFlow().onStart { emit(consumed()) },
            messageEventChannel.receiveAsFlow().onStart { emit(consumed()) },
        ) { info, isOnline, closeEvent, muteOptionsEvent, messageEvent ->
            buildDataState(
                info = info,
                isOnline = isOnline,
                closeEvent = closeEvent,
                muteOptionsEvent = muteOptionsEvent,
                messageEvent = messageEvent,
            )
        }.catch {
            Timber.e(it, "Failed to monitor contact info")
            emit(ContactInfoUiState.Loading(closeEvent = triggered))
        }.asUiStateFlow(
            viewModelScope,
            ContactInfoUiState.Loading(closeEvent = consumed),
        )
    }

    private fun buildDataState(
        info: ContactInfoState,
        isOnline: Boolean,
        closeEvent: StateEvent,
        muteOptionsEvent: StateEventWithContent<List<ChatPushNotificationMuteOption>>,
        messageEvent: StateEventWithContent<ContactInfoMessage>,
    ): ContactInfoUiState {
        val contact = info.contactItem
        return ContactInfoUiState.Data(
            displayName = contact?.displayName() ?: info.chatTitle.orEmpty(),
            nickname = contact?.contactData?.alias,
            email = contact?.email,
            userHandle = info.userHandle,
            chatRoomId = info.chatRoomId,
            isFromContacts = email != null,
            avatar = contact?.let { contactItemAvatarMapper(it) }
                ?: fallbackAvatar(info.chatTitle),
            userChatStatus = info.userChatStatus,
            lastSeenMinutes = info.lastGreenMinutes,
            areCredentialsVerified = contact?.areCredentialsVerified ?: false,
            isNotificationEnabled = info.isNotificationsMuted?.let { !it },
            notificationsMutedUntilTimestamp = info.notificationsMutedUntilTimestamp,
            retentionTimeSeconds = info.retentionTimeSeconds,
            inSharesCount = info.inSharesCount,
            enableCallButtons = isOnline && !info.hasOngoingCall,
            isOnline = isOnline,
            showMuteOptionsEvent = muteOptionsEvent,
            messageEvent = messageEvent,
            closeEvent = closeEvent,
        )
    }

    /**
     * Update or remove the nickname of the contact. Passing null removes the nickname.
     */
    fun updateNickname(newNickname: String?) {
        viewModelScope.launch {
            val data = uiState.value as? ContactInfoUiState.Data ?: return@launch
            if (data.email == null) return@launch
            if (data.nickname != null && data.nickname == newNickname) return@launch
            runCatching { setUserAliasUseCase(newNickname, data.userHandle) }
                .onSuccess {
                    messageEventChannel.send(
                        triggered(
                            if (newNickname == null) {
                                ContactInfoMessage.NicknameRemoved
                            } else {
                                ContactInfoMessage.NicknameAdded
                            }
                        )
                    )
                }
                .onFailure { Timber.e(it, "Failed to update nickname") }
        }
    }

    /**
     * Remove the contact from the user's account. Fires the close event when removal succeeds.
     */
    fun removeContact() {
        viewModelScope.launch {
            val contactEmail = (uiState.value as? ContactInfoUiState.Data)?.email ?: return@launch
            val isRemoved = runCatching { removeContactByEmailUseCase(contactEmail) }
                .getOrElse {
                    Timber.w(it, "Exception removing contact.")
                    false
                }
            if (isRemoved) {
                closeEventChannel.send(triggered)
            }
        }
    }

    /**
     * Handle the chat notifications toggle. Creates the 1:1 chat room first when none exists
     * (the monitor picks the new chat room up automatically). Unmutes the chat when notifications
     * are muted, otherwise fires the mute options event so the screen can offer the mute options.
     */
    fun onNotificationsToggled() {
        viewModelScope.launch {
            val data = uiState.value as? ContactInfoUiState.Data ?: return@launch
            val chatRoomId = data.chatRoomId ?: createChatRoom(data.userHandle) ?: return@launch
            if (data.isNotificationEnabled == false) {
                runCatching {
                    muteChatNotificationForChatRoomsUseCase(
                        listOf(chatRoomId),
                        ChatPushNotificationMuteOption.Unmute,
                    )
                }.onFailure { Timber.e(it, "Failed to unmute chat notifications") }
            } else {
                muteOptionsEventChannel.send(triggered(getChatMuteOptionListUseCase()))
            }
        }
    }

    private suspend fun createChatRoom(userHandle: Long): Long? =
        runCatching { createChatRoomUseCase(isGroup = false, userHandles = listOf(userHandle)) }
            .onFailure {
                Timber.e(it, "Failed to create chat room")
                messageEventChannel.send(triggered(ContactInfoMessage.ChatCreationError))
            }
            .getOrNull()

    /**
     * Mute the chat notifications with the selected mute option.
     */
    fun onMuteOptionSelected(option: ChatPushNotificationMuteOption) {
        viewModelScope.launch {
            val chatRoomId =
                (uiState.value as? ContactInfoUiState.Data)?.chatRoomId ?: return@launch
            runCatching { muteChatNotificationForChatRoomsUseCase(listOf(chatRoomId), option) }
                .onFailure { Timber.e(it, "Failed to mute chat notifications") }
        }
    }

    /**
     * Consume the mute options event once the screen has shown the mute options.
     */
    fun onMuteOptionsEventConsumed() {
        muteOptionsEventChannel.trySend(consumed())
    }

    /**
     * Consume the message event once the screen has shown the message.
     */
    fun onMessageEventConsumed() {
        messageEventChannel.trySend(consumed())
    }

    /**
     * Consume the close event once the screen has navigated back.
     */
    fun onCloseEventConsumed() {
        closeEventChannel.trySend(consumed)
    }

    private fun ContactItem.displayName(): String =
        contactData.fullName ?: email

    private fun fallbackAvatar(title: String?): AvatarData = AvatarData.Initials(
        initials = title?.trim()?.firstOrNull()?.toString()
            ?.uppercase(Locale.getDefault())
            ?: DEFAULT_AVATAR_INITIALS,
        avatarColor = Color.Black,
    )

    private companion object {
        const val DEFAULT_AVATAR_INITIALS = "U"
    }
}
