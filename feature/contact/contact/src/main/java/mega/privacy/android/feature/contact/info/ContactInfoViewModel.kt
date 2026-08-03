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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mega.privacy.android.core.coroutine.asUiStateFlow
import mega.privacy.android.domain.entity.StorageState
import mega.privacy.android.domain.entity.chat.ChatPushNotificationMuteOption
import mega.privacy.android.domain.entity.contacts.ContactInfoState
import mega.privacy.android.domain.entity.contacts.ContactItem
import mega.privacy.android.domain.usecase.account.GetCurrentStorageStateUseCase
import mega.privacy.android.domain.usecase.call.GetChatCallUseCase
import mega.privacy.android.domain.usecase.call.StartCallUseCase
import mega.privacy.android.domain.usecase.chat.CreateChatRoomUseCase
import mega.privacy.android.domain.usecase.chat.Get1On1ChatIdUseCase
import mega.privacy.android.domain.usecase.chat.GetChatMuteOptionListUseCase
import mega.privacy.android.domain.usecase.chat.MuteChatNotificationForChatRoomsUseCase
import mega.privacy.android.domain.usecase.contact.MonitorContactInfoUseCase
import mega.privacy.android.domain.usecase.contact.RemoveContactByEmailUseCase
import mega.privacy.android.domain.usecase.contact.SetUserAliasUseCase
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.feature.contact.info.model.ContactInfoMessage
import mega.privacy.android.feature.contact.info.model.ContactInfoUiState
import mega.privacy.android.feature.contact.list.model.CallEventData
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
    private val get1On1ChatIdUseCase: Get1On1ChatIdUseCase,
    private val getChatCallUseCase: GetChatCallUseCase,
    private val startCallUseCase: StartCallUseCase,
    private val getCurrentStorageStateUseCase: GetCurrentStorageStateUseCase,
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
    private val openChatEventChannel = Channel<StateEventWithContent<Long>>(Channel.BUFFERED)
    private val startCallEventChannel =
        Channel<StateEventWithContent<CallEventData>>(Channel.BUFFERED)
    private val storageOverQuotaEventChannel = Channel<StateEvent>(Channel.BUFFERED)
    private val callInFlightChannel = Channel<Boolean>(Channel.BUFFERED)

    /**
     * Ui state
     */
    val uiState: StateFlow<ContactInfoUiState> by lazy(LazyThreadSafetyMode.NONE) {
        combine(
            monitorContactInfoUseCase(email, chatId),
            monitorConnectivityUseCase(),
            callInFlightChannel.receiveAsFlow().onStart { emit(false) },
            storageOverQuotaEventChannel.receiveAsFlow().onStart { emit(consumed) },
            monitorStateEvents(),
        ) { info, isOnline, isCallInFlight, storageOverQuotaEvent, events ->
            buildDataState(
                info = info,
                isOnline = isOnline,
                isCallInFlight = isCallInFlight,
                storageOverQuotaEvent = storageOverQuotaEvent,
                events = events,
            )
        }.catch {
            Timber.e(it, "Failed to monitor contact info")
            emit(ContactInfoUiState.Loading(closeEvent = triggered))
        }.asUiStateFlow(
            viewModelScope,
            ContactInfoUiState.Loading(closeEvent = consumed),
        )
    }

    private fun monitorStateEvents(): Flow<ContactInfoEvents> = combine(
        closeEventChannel.receiveAsFlow().onStart { emit(consumed) },
        muteOptionsEventChannel.receiveAsFlow().onStart { emit(consumed()) },
        messageEventChannel.receiveAsFlow().onStart { emit(consumed()) },
        openChatEventChannel.receiveAsFlow().onStart { emit(consumed()) },
        startCallEventChannel.receiveAsFlow().onStart { emit(consumed()) },
    ) { closeEvent, muteOptionsEvent, messageEvent, openChatEvent, startCallEvent ->
        ContactInfoEvents(
            closeEvent = closeEvent,
            muteOptionsEvent = muteOptionsEvent,
            messageEvent = messageEvent,
            openChatEvent = openChatEvent,
            startCallEvent = startCallEvent,
        )
    }

    private fun buildDataState(
        info: ContactInfoState,
        isOnline: Boolean,
        isCallInFlight: Boolean,
        storageOverQuotaEvent: StateEvent,
        events: ContactInfoEvents,
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
            enableCallButtons = isOnline && !info.hasOngoingCall && !isCallInFlight,
            isOnline = isOnline,
            showMuteOptionsEvent = events.muteOptionsEvent,
            messageEvent = events.messageEvent,
            openChatEvent = events.openChatEvent,
            startCallEvent = events.startCallEvent,
            storageOverQuotaEvent = storageOverQuotaEvent,
            closeEvent = events.closeEvent,
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
     * Handle the send message action. Fires the open chat event with the 1:1 chat room id,
     * creating the chat room first when none exists. No-op when offline; fires the storage
     * over quota event instead when the account is in paywall state.
     */
    fun sendMessage() {
        viewModelScope.launch {
            val data = uiState.value as? ContactInfoUiState.Data ?: return@launch
            if (!data.isOnline) return@launch
            if (isStorageStatePayWall()) return@launch
            val chatRoomId = data.chatRoomId ?: createChatRoom(data.userHandle) ?: return@launch
            openChatEventChannel.send(triggered(chatRoomId))
        }
    }

    /**
     * Start a call with the contact, or fire the start call event for the ongoing call when one
     * already exists in the 1:1 chat. Disables the call buttons while the call start is in
     * flight. Fires the storage over quota event instead when the account is in paywall state.
     *
     * @param withVideo True to start the call with video enabled.
     */
    fun startCall(withVideo: Boolean) {
        viewModelScope.launch {
            val data = uiState.value as? ContactInfoUiState.Data ?: return@launch
            if (!data.enableCallButtons) return@launch
            if (isStorageStatePayWall()) return@launch
            callInFlightChannel.send(true)
            runCatching {
                val callChatId = get1On1ChatIdUseCase(data.userHandle)
                val existingCall = getChatCallUseCase(callChatId)
                if (existingCall != null) {
                    startCallEventChannel.send(
                        triggered(
                            CallEventData(
                                chatId = callChatId,
                                hasLocalAudio = true,
                                hasLocalVideo = withVideo,
                                isExistingCall = true,
                            )
                        )
                    )
                } else {
                    val call = startCallUseCase(
                        chatId = callChatId,
                        audio = true,
                        video = withVideo,
                    )
                    startCallEventChannel.send(
                        triggered(
                            CallEventData(
                                chatId = callChatId,
                                hasLocalAudio = call?.hasLocalAudio ?: true,
                                hasLocalVideo = call?.hasLocalVideo ?: withVideo,
                                isExistingCall = false,
                            )
                        )
                    )
                }
            }.onFailure { Timber.e(it, "Failed to start call") }
            callInFlightChannel.send(false)
        }
    }

    private suspend fun isStorageStatePayWall(): Boolean {
        val isPayWall = runCatching { getCurrentStorageStateUseCase() }
            .getOrNull() == StorageState.PayWall
        if (isPayWall) {
            storageOverQuotaEventChannel.send(triggered)
        }
        return isPayWall
    }

    /**
     * Show the feedback message for a denied microphone permission when starting a call.
     */
    fun onCallPermissionsDenied() {
        messageEventChannel.trySend(triggered(ContactInfoMessage.MicrophonePermissionDenied))
    }

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

    /**
     * Consume the open chat event once the screen has navigated to the chat.
     */
    fun onOpenChatEventConsumed() {
        openChatEventChannel.trySend(consumed())
    }

    /**
     * Consume the start call event once the screen has navigated to the call.
     */
    fun onStartCallEventConsumed() {
        startCallEventChannel.trySend(consumed())
    }

    /**
     * Consume the storage over quota event once the screen has shown the paywall warning.
     */
    fun onStorageOverQuotaEventConsumed() {
        storageOverQuotaEventChannel.trySend(consumed)
    }

    private fun ContactItem.displayName(): String =
        contactData.fullName ?: email

    private fun fallbackAvatar(title: String?): AvatarData = AvatarData.Initials(
        initials = title?.trim()?.firstOrNull()?.toString()
            ?.uppercase(Locale.getDefault())
            ?: DEFAULT_AVATAR_INITIALS,
        avatarColor = Color.Black,
    )

    private data class ContactInfoEvents(
        val closeEvent: StateEvent,
        val muteOptionsEvent: StateEventWithContent<List<ChatPushNotificationMuteOption>>,
        val messageEvent: StateEventWithContent<ContactInfoMessage>,
        val openChatEvent: StateEventWithContent<Long>,
        val startCallEvent: StateEventWithContent<CallEventData>,
    )

    private companion object {
        const val DEFAULT_AVATAR_INITIALS = "U"
    }
}
