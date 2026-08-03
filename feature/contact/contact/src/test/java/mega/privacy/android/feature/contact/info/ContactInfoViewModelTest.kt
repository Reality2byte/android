package mega.privacy.android.feature.contact.info

import androidx.compose.ui.graphics.Color
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.StateEventWithContentTriggered
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.entity.StorageState
import mega.privacy.android.domain.entity.call.ChatCall
import mega.privacy.android.domain.entity.chat.ChatPushNotificationMuteOption
import mega.privacy.android.domain.entity.contacts.ContactData
import mega.privacy.android.domain.entity.contacts.ContactInfoState
import mega.privacy.android.domain.entity.contacts.ContactItem
import mega.privacy.android.domain.entity.contacts.UserChatStatus
import mega.privacy.android.domain.entity.user.UserVisibility
import mega.privacy.android.domain.exception.ContactDoesNotExistException
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(CoroutineMainDispatcherExtension::class)
class ContactInfoViewModelTest {

    private lateinit var underTest: ContactInfoViewModel

    private val monitorContactInfoUseCase = mock<MonitorContactInfoUseCase>()
    private val monitorConnectivityUseCase = mock<MonitorConnectivityUseCase>()
    private val setUserAliasUseCase = mock<SetUserAliasUseCase>()
    private val removeContactByEmailUseCase = mock<RemoveContactByEmailUseCase>()
    private val createChatRoomUseCase = mock<CreateChatRoomUseCase>()
    private val muteChatNotificationForChatRoomsUseCase =
        mock<MuteChatNotificationForChatRoomsUseCase>()
    private val getChatMuteOptionListUseCase = mock<GetChatMuteOptionListUseCase>()
    private val get1On1ChatIdUseCase = mock<Get1On1ChatIdUseCase>()
    private val getChatCallUseCase = mock<GetChatCallUseCase>()
    private val startCallUseCase = mock<StartCallUseCase>()
    private val getCurrentStorageStateUseCase = mock<GetCurrentStorageStateUseCase>()
    private val contactItemAvatarMapper = mock<ContactItemAvatarMapper>()

    @BeforeEach
    fun setUp() {
        whenever(monitorConnectivityUseCase()).thenReturn(flowOf(true))
        whenever(contactItemAvatarMapper(any())).thenReturn(AVATAR)
        whenever { getCurrentStorageStateUseCase() }.thenReturn(StorageState.Green)
        underTest = createViewModel(email = EMAIL, chatId = null)
    }

    @AfterEach
    fun tearDown() {
        reset(
            monitorContactInfoUseCase,
            monitorConnectivityUseCase,
            setUserAliasUseCase,
            removeContactByEmailUseCase,
            createChatRoomUseCase,
            muteChatNotificationForChatRoomsUseCase,
            getChatMuteOptionListUseCase,
            get1On1ChatIdUseCase,
            getChatCallUseCase,
            startCallUseCase,
            getCurrentStorageStateUseCase,
            contactItemAvatarMapper,
        )
    }

    private fun createViewModel(email: String?, chatId: Long?) = ContactInfoViewModel(
        email = email,
        chatId = chatId,
        monitorContactInfoUseCase = monitorContactInfoUseCase,
        monitorConnectivityUseCase = monitorConnectivityUseCase,
        setUserAliasUseCase = setUserAliasUseCase,
        removeContactByEmailUseCase = removeContactByEmailUseCase,
        createChatRoomUseCase = createChatRoomUseCase,
        muteChatNotificationForChatRoomsUseCase = muteChatNotificationForChatRoomsUseCase,
        getChatMuteOptionListUseCase = getChatMuteOptionListUseCase,
        get1On1ChatIdUseCase = get1On1ChatIdUseCase,
        getChatCallUseCase = getChatCallUseCase,
        startCallUseCase = startCallUseCase,
        getCurrentStorageStateUseCase = getCurrentStorageStateUseCase,
        contactItemAvatarMapper = contactItemAvatarMapper,
    )

    private fun stubContactInfo(state: ContactInfoState = createContactInfoState()) {
        whenever(monitorContactInfoUseCase(EMAIL, null)).thenReturn(flowOf(state))
    }

    @Test
    fun `test that initial state is Loading`() = runTest {
        stubContactInfo()

        assertThat(underTest.uiState.value)
            .isEqualTo(ContactInfoUiState.Loading(closeEvent = consumed))
    }

    @Test
    fun `test that state is Loaded with contact data when the monitor emits`() = runTest {
        stubContactInfo()

        underTest.uiState.test {
            val actual = awaitDataState()
            assertThat(actual.displayName).isEqualTo(FULL_NAME)
            assertThat(actual.nickname).isEqualTo(ALIAS)
            assertThat(actual.email).isEqualTo(EMAIL)
            assertThat(actual.userHandle).isEqualTo(USER_HANDLE)
            assertThat(actual.chatRoomId).isEqualTo(CHAT_ID)
            assertThat(actual.isFromContacts).isTrue()
            assertThat(actual.avatar).isEqualTo(AVATAR)
            assertThat(actual.userChatStatus).isEqualTo(UserChatStatus.Online)
            assertThat(actual.lastSeenMinutes).isNull()
            assertThat(actual.areCredentialsVerified).isFalse()
            assertThat(actual.isOnline).isTrue()
            assertThat(actual.closeEvent).isEqualTo(consumed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that Loaded has null chatRoomId when no chat room exists for the contact`() =
        runTest {
            stubContactInfo(createContactInfoState(chatRoomId = null))

            underTest.uiState.test {
                assertThat(awaitDataState().chatRoomId).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that isFromContacts is false when initialised with chat id`() = runTest {
        underTest = createViewModel(email = null, chatId = CHAT_ID)
        whenever(monitorContactInfoUseCase(null, CHAT_ID))
            .thenReturn(flowOf(createContactInfoState()))

        underTest.uiState.test {
            val actual = awaitDataState()
            assertThat(actual.displayName).isEqualTo(FULL_NAME)
            assertThat(actual.email).isEqualTo(EMAIL)
            assertThat(actual.chatRoomId).isEqualTo(CHAT_ID)
            assertThat(actual.isFromContacts).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that state is a reduced Loaded when the chat peer is not a contact`() = runTest {
        underTest = createViewModel(email = null, chatId = CHAT_ID)
        whenever(monitorContactInfoUseCase(null, CHAT_ID)).thenReturn(
            flowOf(
                createContactInfoState(
                    contactItem = null,
                    chatTitle = CHAT_TITLE,
                    userHandle = PEER_HANDLE,
                )
            )
        )

        underTest.uiState.test {
            val actual = awaitDataState()
            assertThat(actual.displayName).isEqualTo(CHAT_TITLE)
            assertThat(actual.email).isNull()
            assertThat(actual.nickname).isNull()
            assertThat(actual.userHandle).isEqualTo(PEER_HANDLE)
            assertThat(actual.chatRoomId).isEqualTo(CHAT_ID)
            assertThat(actual.showSharedFolders).isFalse()
            assertThat(actual.showShareContact).isFalse()
            assertThat(actual.showVerifyCredentials).isFalse()
            assertThat(actual.avatar)
                .isEqualTo(AvatarData.Initials(initials = "B", avatarColor = Color.Black))
            assertThat(actual.closeEvent).isEqualTo(consumed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that nickname is null when the contact has no alias`() = runTest {
        stubContactInfo(createContactInfoState(contactItem = createContactItem(alias = null)))

        underTest.uiState.test {
            assertThat(awaitDataState().nickname).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that displayName falls back to email when full name is null`() = runTest {
        stubContactInfo(createContactInfoState(contactItem = createContactItem(fullName = null)))

        underTest.uiState.test {
            assertThat(awaitDataState().displayName).isEqualTo(EMAIL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that close event is triggered when the peer cannot be resolved`() = runTest {
        whenever(monitorContactInfoUseCase(EMAIL, null))
            .thenReturn(flow { throw ContactDoesNotExistException() })

        underTest.uiState.test {
            awaitUntil { it.closeEvent == triggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that close event is triggered when the monitor flow fails`() = runTest {
        whenever(monitorContactInfoUseCase(EMAIL, null))
            .thenReturn(flow { throw RuntimeException("monitor failed") })

        underTest.uiState.test {
            awaitUntil { it.closeEvent == triggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that state stays up to date when the monitor emits updates`() = runTest {
        val contactInfoUpdates = MutableSharedFlow<ContactInfoState>()
        whenever(monitorContactInfoUseCase(EMAIL, null)).thenReturn(contactInfoUpdates)

        underTest.uiState.test {
            awaitItem()
            contactInfoUpdates.emit(createContactInfoState())
            awaitDataState { it.userChatStatus == UserChatStatus.Online }
            contactInfoUpdates.emit(
                createContactInfoState(
                    contactItem = createContactItem(
                        alias = "NewNick",
                        fullName = "New Name",
                        status = UserChatStatus.Busy,
                    ),
                    userChatStatus = UserChatStatus.Busy,
                    lastGreenMinutes = 42,
                )
            )
            val actual = awaitDataState { it.userChatStatus == UserChatStatus.Busy }
            assertThat(actual.nickname).isEqualTo("NewNick")
            assertThat(actual.displayName).isEqualTo("New Name")
            assertThat(actual.lastSeenMinutes).isEqualTo(42)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that isOnline and call buttons update when connectivity changes`() = runTest {
        stubContactInfo()
        whenever(monitorConnectivityUseCase()).thenReturn(flowOf(true, false))

        underTest.uiState.test {
            val actual = awaitDataState { !it.isOnline }
            assertThat(actual.isOnline).isFalse()
            assertThat(actual.enableCallButtons).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that notifications are enabled when the chat is not muted`() = runTest {
        stubContactInfo(createContactInfoState(isNotificationsMuted = false))

        underTest.uiState.test {
            val actual = awaitDataState()
            assertThat(actual.isNotificationEnabled).isTrue()
            assertThat(actual.notificationsMutedUntilTimestamp).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that notifications are disabled without a timestamp when muted until turned back on`() =
        runTest {
            stubContactInfo(createContactInfoState(isNotificationsMuted = true))

            underTest.uiState.test {
                val actual = awaitDataState()
                assertThat(actual.isNotificationEnabled).isFalse()
                assertThat(actual.notificationsMutedUntilTimestamp).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that notifications are disabled with a timestamp when muted for a period`() =
        runTest {
            stubContactInfo(
                createContactInfoState(
                    isNotificationsMuted = true,
                    notificationsMutedUntilTimestamp = MUTED_UNTIL_TIMESTAMP,
                )
            )

            underTest.uiState.test {
                val actual = awaitDataState()
                assertThat(actual.isNotificationEnabled).isFalse()
                assertThat(actual.notificationsMutedUntilTimestamp)
                    .isEqualTo(MUTED_UNTIL_TIMESTAMP)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that notification state is null when the contact has no chat room`() = runTest {
        stubContactInfo(
            createContactInfoState(chatRoomId = null, isNotificationsMuted = null)
        )

        underTest.uiState.test {
            assertThat(awaitDataState().isNotificationEnabled).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that retention time is mapped to the state`() = runTest {
        stubContactInfo(createContactInfoState(retentionTimeSeconds = RETENTION_TIME))

        underTest.uiState.test {
            assertThat(awaitDataState().retentionTimeSeconds).isEqualTo(RETENTION_TIME)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that in shares count is mapped to the state`() = runTest {
        stubContactInfo(createContactInfoState(inSharesCount = 3))

        underTest.uiState.test {
            assertThat(awaitDataState().inSharesCount).isEqualTo(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that call buttons are disabled when a call is ongoing in the chat`() = runTest {
        stubContactInfo(createContactInfoState(hasOngoingCall = true))

        underTest.uiState.test {
            assertThat(awaitDataState().enableCallButtons).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that updateNickname sets the alias through the use case`() = runTest {
        stubContactInfo()
        underTest.uiState.test {
            awaitDataState()
            cancelAndIgnoreRemainingEvents()
        }

        underTest.updateNickname("NewNick")

        verify(setUserAliasUseCase).invoke("NewNick", USER_HANDLE)
    }

    @Test
    fun `test that updateNickname removes the alias when called with null`() = runTest {
        stubContactInfo()
        underTest.uiState.test {
            awaitDataState()
            cancelAndIgnoreRemainingEvents()
        }

        underTest.updateNickname(null)

        verify(setUserAliasUseCase).invoke(null, USER_HANDLE)
    }

    @Test
    fun `test that updateNickname does not call the use case when the nickname is unchanged`() =
        runTest {
            stubContactInfo()
            underTest.uiState.test {
                awaitDataState()
                cancelAndIgnoreRemainingEvents()
            }

            underTest.updateNickname(ALIAS)

            verify(setUserAliasUseCase, never()).invoke(anyOrNull(), any())
        }

    @Test
    fun `test that updateNickname does not call the use case when the peer is not a contact`() =
        runTest {
            underTest = createViewModel(email = null, chatId = CHAT_ID)
            whenever(monitorContactInfoUseCase(null, CHAT_ID)).thenReturn(
                flowOf(
                    createContactInfoState(
                        contactItem = null,
                        chatTitle = CHAT_TITLE,
                        userHandle = PEER_HANDLE,
                    )
                )
            )
            underTest.uiState.test {
                awaitDataState()
                cancelAndIgnoreRemainingEvents()
            }

            underTest.updateNickname("NewNick")

            verify(setUserAliasUseCase, never()).invoke(anyOrNull(), any())
        }

    @Test
    fun `test that removeContact triggers the close event when removal succeeds`() = runTest {
        stubContactInfo()
        whenever(removeContactByEmailUseCase(EMAIL)).thenReturn(true)

        underTest.uiState.test {
            awaitDataState()
            underTest.removeContact()
            awaitUntil { it.closeEvent == triggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that removeContact does not trigger the close event when removal fails`() = runTest {
        stubContactInfo()
        whenever(removeContactByEmailUseCase(EMAIL))
            .thenThrow(RuntimeException("removal failed"))

        underTest.uiState.test {
            awaitDataState()
            underTest.removeContact()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(underTest.uiState.value.closeEvent).isEqualTo(consumed)
    }

    @Test
    fun `test that onCloseEventConsumed resets the close event`() = runTest {
        stubContactInfo()
        whenever(removeContactByEmailUseCase(EMAIL)).thenReturn(true)

        underTest.uiState.test {
            awaitDataState()
            underTest.removeContact()
            awaitUntil { it.closeEvent == triggered }
            underTest.onCloseEventConsumed()
            awaitUntil { it.closeEvent == consumed }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that onNotificationsToggled unmutes the chat when notifications are muted`() =
        runTest {
            stubContactInfo(createContactInfoState(isNotificationsMuted = true))

            underTest.uiState.test {
                awaitDataState { it.isNotificationEnabled == false }
                underTest.onNotificationsToggled()
                cancelAndIgnoreRemainingEvents()
            }

            verify(muteChatNotificationForChatRoomsUseCase)
                .invoke(listOf(CHAT_ID), ChatPushNotificationMuteOption.Unmute)
        }

    @Test
    fun `test that onNotificationsToggled emits the mute options event when notifications are enabled`() =
        runTest {
            stubContactInfo(createContactInfoState(isNotificationsMuted = false))
            whenever(getChatMuteOptionListUseCase(any())).thenReturn(MUTE_OPTIONS)

            underTest.uiState.test {
                awaitDataState { it.isNotificationEnabled == true }
                underTest.onNotificationsToggled()
                val actual = awaitDataState {
                    it.showMuteOptionsEvent is StateEventWithContentTriggered
                }
                val event =
                    actual.showMuteOptionsEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(MUTE_OPTIONS)
                cancelAndIgnoreRemainingEvents()
            }

            verify(muteChatNotificationForChatRoomsUseCase, never()).invoke(any(), any())
        }

    @Test
    fun `test that onNotificationsToggled creates the chat room first when none exists`() =
        runTest {
            val contactInfoUpdates = MutableSharedFlow<ContactInfoState>()
            whenever(monitorContactInfoUseCase(EMAIL, null)).thenReturn(contactInfoUpdates)
            whenever(createChatRoomUseCase(isGroup = false, userHandles = listOf(USER_HANDLE)))
                .thenReturn(NEW_CHAT_ID)
            whenever(getChatMuteOptionListUseCase(any())).thenReturn(MUTE_OPTIONS)

            underTest.uiState.test {
                awaitItem()
                contactInfoUpdates.emit(
                    createContactInfoState(chatRoomId = null, isNotificationsMuted = null)
                )
                awaitDataState()
                underTest.onNotificationsToggled()
                awaitDataState { it.showMuteOptionsEvent is StateEventWithContentTriggered }
                contactInfoUpdates.emit(
                    createContactInfoState(
                        chatRoomId = NEW_CHAT_ID,
                        isNotificationsMuted = false,
                    )
                )
                awaitDataState { it.chatRoomId == NEW_CHAT_ID }
                cancelAndIgnoreRemainingEvents()
            }

            verify(createChatRoomUseCase).invoke(isGroup = false, userHandles = listOf(USER_HANDLE))
        }

    @Test
    fun `test that onNotificationsToggled does nothing more when chat room creation fails`() =
        runTest {
            stubContactInfo(
                createContactInfoState(chatRoomId = null, isNotificationsMuted = null)
            )
            whenever(createChatRoomUseCase(any(), any()))
                .thenThrow(RuntimeException("creation failed"))

            underTest.uiState.test {
                awaitDataState()
                underTest.onNotificationsToggled()
                cancelAndIgnoreRemainingEvents()
            }

            verify(getChatMuteOptionListUseCase, never()).invoke(any())
            verify(muteChatNotificationForChatRoomsUseCase, never()).invoke(any(), any())
        }

    @Test
    fun `test that updateNickname triggers the nickname added message when the nickname is set`() =
        runTest {
            stubContactInfo()
            whenever(setUserAliasUseCase("NewNick", USER_HANDLE)).thenReturn("NewNick")

            underTest.uiState.test {
                awaitDataState()
                underTest.updateNickname("NewNick")
                val actual = awaitDataState {
                    it.messageEvent is StateEventWithContentTriggered
                }
                val event = actual.messageEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(ContactInfoMessage.NicknameAdded)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that updateNickname triggers the nickname removed message when the nickname is removed`() =
        runTest {
            stubContactInfo()
            whenever(setUserAliasUseCase(null, USER_HANDLE)).thenReturn(null)

            underTest.uiState.test {
                awaitDataState()
                underTest.updateNickname(null)
                val actual = awaitDataState {
                    it.messageEvent is StateEventWithContentTriggered
                }
                val event = actual.messageEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(ContactInfoMessage.NicknameRemoved)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that updateNickname does not trigger a message when setting the nickname fails`() =
        runTest {
            stubContactInfo()
            whenever(setUserAliasUseCase("NewNick", USER_HANDLE))
                .thenThrow(RuntimeException("set alias failed"))

            underTest.uiState.test {
                awaitDataState()
                underTest.updateNickname("NewNick")
                cancelAndIgnoreRemainingEvents()
            }

            assertThat((underTest.uiState.value as ContactInfoUiState.Data).messageEvent)
                .isNotInstanceOf(StateEventWithContentTriggered::class.java)
        }

    @Test
    fun `test that onNotificationsToggled triggers the chat creation error message when chat room creation fails`() =
        runTest {
            stubContactInfo(
                createContactInfoState(chatRoomId = null, isNotificationsMuted = null)
            )
            whenever(createChatRoomUseCase(any(), any()))
                .thenThrow(RuntimeException("creation failed"))

            underTest.uiState.test {
                awaitDataState()
                underTest.onNotificationsToggled()
                val actual = awaitDataState {
                    it.messageEvent is StateEventWithContentTriggered
                }
                val event = actual.messageEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(ContactInfoMessage.ChatCreationError)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that onMuteOptionSelected mutes the chat with the selected option`() = runTest {
        stubContactInfo()

        underTest.uiState.test {
            awaitDataState()
            underTest.onMuteOptionSelected(ChatPushNotificationMuteOption.Mute6Hours)
            cancelAndIgnoreRemainingEvents()
        }

        verify(muteChatNotificationForChatRoomsUseCase)
            .invoke(listOf(CHAT_ID), ChatPushNotificationMuteOption.Mute6Hours)
    }

    @Test
    fun `test that onMuteOptionSelected does not call the use case when there is no chat room`() =
        runTest {
            stubContactInfo(
                createContactInfoState(chatRoomId = null, isNotificationsMuted = null)
            )

            underTest.uiState.test {
                awaitDataState()
                underTest.onMuteOptionSelected(ChatPushNotificationMuteOption.Mute6Hours)
                cancelAndIgnoreRemainingEvents()
            }

            verify(muteChatNotificationForChatRoomsUseCase, never()).invoke(any(), any())
        }

    @Test
    fun `test that onMessageEventConsumed resets the message event`() = runTest {
        stubContactInfo()
        whenever(setUserAliasUseCase("NewNick", USER_HANDLE)).thenReturn("NewNick")

        underTest.uiState.test {
            awaitDataState()
            underTest.updateNickname("NewNick")
            awaitDataState { it.messageEvent is StateEventWithContentTriggered }
            underTest.onMessageEventConsumed()
            awaitDataState { it.messageEvent !is StateEventWithContentTriggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that onMuteOptionsEventConsumed resets the mute options event`() = runTest {
        stubContactInfo(createContactInfoState(isNotificationsMuted = false))
        whenever(getChatMuteOptionListUseCase(any())).thenReturn(MUTE_OPTIONS)

        underTest.uiState.test {
            awaitDataState { it.isNotificationEnabled == true }
            underTest.onNotificationsToggled()
            awaitDataState { it.showMuteOptionsEvent is StateEventWithContentTriggered }
            underTest.onMuteOptionsEventConsumed()
            awaitDataState { it.showMuteOptionsEvent !is StateEventWithContentTriggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that sendMessage triggers the open chat event when a chat room exists`() = runTest {
        stubContactInfo()

        underTest.uiState.test {
            awaitDataState()
            underTest.sendMessage()
            val actual = awaitDataState { it.openChatEvent is StateEventWithContentTriggered }
            val event = actual.openChatEvent as StateEventWithContentTriggered
            assertThat(event.content).isEqualTo(CHAT_ID)
            cancelAndIgnoreRemainingEvents()
        }

        verify(createChatRoomUseCase, never()).invoke(any(), any())
    }

    @Test
    fun `test that sendMessage creates the chat room first when none exists`() = runTest {
        stubContactInfo(createContactInfoState(chatRoomId = null, isNotificationsMuted = null))
        whenever(createChatRoomUseCase(isGroup = false, userHandles = listOf(USER_HANDLE)))
            .thenReturn(NEW_CHAT_ID)

        underTest.uiState.test {
            awaitDataState()
            underTest.sendMessage()
            val actual = awaitDataState { it.openChatEvent is StateEventWithContentTriggered }
            val event = actual.openChatEvent as StateEventWithContentTriggered
            assertThat(event.content).isEqualTo(NEW_CHAT_ID)
            cancelAndIgnoreRemainingEvents()
        }

        verify(createChatRoomUseCase).invoke(isGroup = false, userHandles = listOf(USER_HANDLE))
    }

    @Test
    fun `test that sendMessage triggers the chat creation error message when chat room creation fails`() =
        runTest {
            stubContactInfo(createContactInfoState(chatRoomId = null, isNotificationsMuted = null))
            whenever(createChatRoomUseCase(any(), any()))
                .thenThrow(RuntimeException("creation failed"))

            underTest.uiState.test {
                awaitDataState()
                underTest.sendMessage()
                val actual = awaitDataState { it.messageEvent is StateEventWithContentTriggered }
                val event = actual.messageEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(ContactInfoMessage.ChatCreationError)
                assertThat(actual.openChatEvent)
                    .isNotInstanceOf(StateEventWithContentTriggered::class.java)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that sendMessage does nothing when offline`() = runTest {
        stubContactInfo()
        whenever(monitorConnectivityUseCase()).thenReturn(flowOf(false))

        underTest.uiState.test {
            awaitDataState { !it.isOnline }
            underTest.sendMessage()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat((underTest.uiState.value as ContactInfoUiState.Data).openChatEvent)
            .isNotInstanceOf(StateEventWithContentTriggered::class.java)
    }

    @Test
    fun `test that sendMessage triggers the storage over quota event when the account is in paywall state`() =
        runTest {
            stubContactInfo()
            whenever { getCurrentStorageStateUseCase() }.thenReturn(StorageState.PayWall)

            underTest.uiState.test {
                awaitDataState()
                underTest.sendMessage()
                val actual = awaitDataState { it.storageOverQuotaEvent == triggered }
                assertThat(actual.openChatEvent)
                    .isNotInstanceOf(StateEventWithContentTriggered::class.java)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that startCall triggers the start call event for a new call when none exists`() =
        runTest {
            stubContactInfo()
            whenever(get1On1ChatIdUseCase(USER_HANDLE)).thenReturn(CHAT_ID)
            whenever(getChatCallUseCase(CHAT_ID)).thenReturn(null)
            whenever(startCallUseCase(chatId = CHAT_ID, audio = true, video = true))
                .thenReturn(ChatCall(chatId = CHAT_ID, callId = 1L, hasLocalAudio = true, hasLocalVideo = true))

            underTest.uiState.test {
                awaitDataState()
                underTest.startCall(withVideo = true)
                val actual = awaitDataState {
                    it.startCallEvent is StateEventWithContentTriggered
                }
                val event = actual.startCallEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(
                    CallEventData(
                        chatId = CHAT_ID,
                        hasLocalAudio = true,
                        hasLocalVideo = true,
                        isExistingCall = false,
                    )
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that startCall triggers the start call event for the existing call when one is ongoing`() =
        runTest {
            stubContactInfo()
            whenever(get1On1ChatIdUseCase(USER_HANDLE)).thenReturn(CHAT_ID)
            whenever(getChatCallUseCase(CHAT_ID))
                .thenReturn(ChatCall(chatId = CHAT_ID, callId = 1L))

            underTest.uiState.test {
                awaitDataState()
                underTest.startCall(withVideo = false)
                val actual = awaitDataState {
                    it.startCallEvent is StateEventWithContentTriggered
                }
                val event = actual.startCallEvent as StateEventWithContentTriggered
                assertThat(event.content).isEqualTo(
                    CallEventData(
                        chatId = CHAT_ID,
                        hasLocalAudio = true,
                        hasLocalVideo = false,
                        isExistingCall = true,
                    )
                )
                cancelAndIgnoreRemainingEvents()
            }

            verify(startCallUseCase, never()).invoke(any(), any(), any())
        }

    @Test
    fun `test that call buttons are disabled while the call start is in flight`() = runTest {
        stubContactInfo()
        val callStart = CompletableDeferred<ChatCall?>()
        whenever(get1On1ChatIdUseCase(USER_HANDLE)).thenReturn(CHAT_ID)
        whenever(getChatCallUseCase(CHAT_ID)).thenReturn(null)
        whenever(startCallUseCase(chatId = CHAT_ID, audio = true, video = false))
            .doSuspendableAnswer { callStart.await() }

        underTest.uiState.test {
            awaitDataState { it.enableCallButtons }
            underTest.startCall(withVideo = false)
            awaitDataState { !it.enableCallButtons }
            callStart.complete(null)
            awaitDataState { it.enableCallButtons }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that startCall re-enables the call buttons without an event when starting the call fails`() =
        runTest {
            stubContactInfo()
            whenever(get1On1ChatIdUseCase(USER_HANDLE))
                .thenThrow(RuntimeException("start call failed"))

            underTest.uiState.test {
                awaitDataState()
                underTest.startCall(withVideo = false)
                cancelAndIgnoreRemainingEvents()
            }

            val actual = underTest.uiState.value as ContactInfoUiState.Data
            assertThat(actual.enableCallButtons).isTrue()
            assertThat(actual.startCallEvent)
                .isNotInstanceOf(StateEventWithContentTriggered::class.java)
        }

    @Test
    fun `test that startCall triggers the storage over quota event when the account is in paywall state`() =
        runTest {
            stubContactInfo()
            whenever { getCurrentStorageStateUseCase() }.thenReturn(StorageState.PayWall)

            underTest.uiState.test {
                awaitDataState()
                underTest.startCall(withVideo = false)
                awaitDataState { it.storageOverQuotaEvent == triggered }
                cancelAndIgnoreRemainingEvents()
            }

            verify(get1On1ChatIdUseCase, never()).invoke(any())
        }

    @Test
    fun `test that startCall does nothing when the call buttons are disabled`() = runTest {
        stubContactInfo(createContactInfoState(hasOngoingCall = true))

        underTest.uiState.test {
            awaitDataState { !it.enableCallButtons }
            underTest.startCall(withVideo = false)
            cancelAndIgnoreRemainingEvents()
        }

        verify(get1On1ChatIdUseCase, never()).invoke(any())
    }

    @Test
    fun `test that onCallPermissionsDenied triggers the microphone permission message`() =
        runTest {
            stubContactInfo()

            underTest.uiState.test {
                awaitDataState()
                underTest.onCallPermissionsDenied()
                val actual = awaitDataState {
                    it.messageEvent is StateEventWithContentTriggered
                }
                val event = actual.messageEvent as StateEventWithContentTriggered
                assertThat(event.content)
                    .isEqualTo(ContactInfoMessage.MicrophonePermissionDenied)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `test that onOpenChatEventConsumed resets the open chat event`() = runTest {
        stubContactInfo()

        underTest.uiState.test {
            awaitDataState()
            underTest.sendMessage()
            awaitDataState { it.openChatEvent is StateEventWithContentTriggered }
            underTest.onOpenChatEventConsumed()
            awaitDataState { it.openChatEvent !is StateEventWithContentTriggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that onStartCallEventConsumed resets the start call event`() = runTest {
        stubContactInfo()
        whenever(get1On1ChatIdUseCase(USER_HANDLE)).thenReturn(CHAT_ID)
        whenever(getChatCallUseCase(CHAT_ID)).thenReturn(null)
        whenever(startCallUseCase(chatId = CHAT_ID, audio = true, video = false))
            .thenReturn(null)

        underTest.uiState.test {
            awaitDataState()
            underTest.startCall(withVideo = false)
            awaitDataState { it.startCallEvent is StateEventWithContentTriggered }
            underTest.onStartCallEventConsumed()
            awaitDataState { it.startCallEvent !is StateEventWithContentTriggered }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that onStorageOverQuotaEventConsumed resets the storage over quota event`() =
        runTest {
            stubContactInfo()
            whenever { getCurrentStorageStateUseCase() }.thenReturn(StorageState.PayWall)

            underTest.uiState.test {
                awaitDataState()
                underTest.sendMessage()
                awaitDataState { it.storageOverQuotaEvent == triggered }
                underTest.onStorageOverQuotaEventConsumed()
                awaitDataState { it.storageOverQuotaEvent == consumed }
                cancelAndIgnoreRemainingEvents()
            }
        }

    private suspend fun ReceiveTurbine<ContactInfoUiState>.awaitDataState(
        predicate: (ContactInfoUiState.Data) -> Boolean = { true },
    ): ContactInfoUiState.Data {
        var item = awaitItem()
        while (item !is ContactInfoUiState.Data || !predicate(item)) {
            item = awaitItem()
        }
        return item
    }

    private suspend fun ReceiveTurbine<ContactInfoUiState>.awaitUntil(
        predicate: (ContactInfoUiState) -> Boolean,
    ): ContactInfoUiState {
        var item = awaitItem()
        while (!predicate(item)) {
            item = awaitItem()
        }
        return item
    }

    private fun createContactInfoState(
        contactItem: ContactItem? = createContactItem(),
        chatRoomId: Long? = CHAT_ID,
        chatTitle: String? = null,
        userHandle: Long = USER_HANDLE,
        userChatStatus: UserChatStatus = UserChatStatus.Online,
        lastGreenMinutes: Int? = null,
        isNotificationsMuted: Boolean? = false,
        notificationsMutedUntilTimestamp: Long? = null,
        retentionTimeSeconds: Long? = null,
        inSharesCount: Int = 0,
        hasOngoingCall: Boolean = false,
    ) = ContactInfoState(
        contactItem = contactItem,
        chatRoomId = chatRoomId,
        chatTitle = chatTitle,
        userHandle = userHandle,
        userChatStatus = userChatStatus,
        lastGreenMinutes = lastGreenMinutes,
        isNotificationsMuted = isNotificationsMuted,
        notificationsMutedUntilTimestamp = notificationsMutedUntilTimestamp,
        retentionTimeSeconds = retentionTimeSeconds,
        inSharesCount = inSharesCount,
        hasOngoingCall = hasOngoingCall,
    )

    private fun createContactItem(
        alias: String? = ALIAS,
        fullName: String? = FULL_NAME,
        status: UserChatStatus = UserChatStatus.Online,
    ) = ContactItem(
        handle = USER_HANDLE,
        email = EMAIL,
        contactData = ContactData(
            fullName = fullName,
            alias = alias,
            avatarUri = null,
            userVisibility = UserVisibility.Visible,
        ),
        defaultAvatarColor = null,
        visibility = UserVisibility.Visible,
        timestamp = 0L,
        areCredentialsVerified = false,
        status = status,
        lastSeen = null,
        chatroomId = null,
    )

    companion object {
        private const val EMAIL = "contact@mega.nz"
        private const val USER_HANDLE = 42L
        private const val PEER_HANDLE = 7L
        private const val CHAT_ID = 123L
        private const val NEW_CHAT_ID = 456L
        private const val ALIAS = "Ally"
        private const val FULL_NAME = "Alice Anderson"
        private const val CHAT_TITLE = "Bob"
        private const val MUTED_UNTIL_TIMESTAMP = 1893456000L
        private const val RETENTION_TIME = 3600L
        private val AVATAR = AvatarData.Initials(initials = "A", avatarColor = Color(0xFF2E7D32))
        private val MUTE_OPTIONS = listOf(
            ChatPushNotificationMuteOption.Mute30Minutes,
            ChatPushNotificationMuteOption.MuteUntilTurnBackOn,
        )
    }
}
