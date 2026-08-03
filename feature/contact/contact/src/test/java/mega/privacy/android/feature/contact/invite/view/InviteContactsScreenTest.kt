package mega.privacy.android.feature.contact.invite.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.StateEvent
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.collections.immutable.persistentListOf
import mega.android.core.ui.components.contact.state.ContactItemStatus
import mega.privacy.android.feature.contact.invite.model.EmailValidationMessage
import mega.privacy.android.feature.contact.invite.model.InviteContactUiState
import mega.privacy.android.feature.contact.invite.model.PickedContactData
import mega.privacy.android.feature.contact.picker.PhoneContactsSection
import mega.privacy.android.feature.contact.picker.ScannedContactDialog
import mega.privacy.android.shared.contact.components.SCANNED_CONTACT_INVALID_CODE_DIALOG_TAG
import mega.privacy.android.shared.contact.model.AvatarData
import mega.privacy.android.shared.contact.model.ContactItemUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InviteContactsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `test that the shimmer loading view is displayed when state is Loading`() {
        setScreen(InviteContactUiState.Loading)

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_LOADING_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(INVITE_CONTACTS_LIST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that the allow access CTA is displayed when the section is PermissionRequired`() {
        setScreen(dataState(phoneSection = PhoneContactsSection.PermissionRequired))

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_ALLOW_ACCESS_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that the select phone contacts CTA is displayed when the picker is available`() {
        setScreen(dataState(phoneSection = PhoneContactsSection.PickerAvailable(persistentListOf())))

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_SELECT_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that picked contact rows render including a phone-only contact`() {
        setScreen(
            dataState(
                phoneSection = PhoneContactsSection.PickerAvailable(
                    persistentListOf(
                        phoneContact(displayName = "Alice", email = "alice@test.com"),
                        phoneContact(displayName = "Bob", email = ""),
                    ),
                ),
            )
        )

        composeTestRule.onAllNodesWithTag(CONTACT_ITEM_VIEW_ROW).assertCountEquals(2)
    }

    @Test
    fun `test that submitting the recipient field invokes the validate callback`() {
        var submitted: String? = null
        setScreen(
            dataState(phoneSection = PhoneContactsSection.PickerAvailable(persistentListOf())),
            onSubmitManualInput = { submitted = it },
        )

        typeRecipientAndAdd("guest@test.com")

        assertThat(submitted).isEqualTo("guest@test.com")
    }

    @Test
    fun `test that an accepted manual email is folded in as a chip`() {
        setScreen(dataState(manualEmailAcceptedEvent = triggered("guest@test.com")))

        composeTestRule.onNodeWithContentDescription("guest@test.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 contact").assertIsDisplayed()
        composeTestRule.onNodeWithTag(INVITE_CONTACTS_FAB_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that an accepted manual phone number is folded in as a chip`() {
        setScreen(dataState(manualPhoneAcceptedEvent = triggered("+15551234567")))

        composeTestRule.onNodeWithContentDescription("+15551234567").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 contact").assertIsDisplayed()
    }

    @Test
    fun `test that picked contacts are folded in as chips`() {
        setScreen(
            dataState(
                phoneContactsPickedEvent = triggered(
                    PickedContactData(
                        emails = persistentListOf("alice@test.com"),
                        phoneNumbers = persistentListOf("+15551234567"),
                    )
                ),
            )
        )

        composeTestRule.onNodeWithContentDescription("alice@test.com").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("+15551234567").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 contacts").assertIsDisplayed()
    }

    @Test
    fun `test that clicking a chip removes the recipient`() {
        setScreen(
            dataState(),
            initialSelectedManualEmails = setOf("guest@test.com"),
        )

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_FAB_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("guest@test.com").performClick()

        composeTestRule.onNodeWithContentDescription("guest@test.com").assertDoesNotExist()
        composeTestRule.onNodeWithTag(INVITE_CONTACTS_FAB_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that the fab is hidden until a recipient is selected`() {
        setScreen(dataState())

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_FAB_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that the fab is displayed when a recipient is pre-selected`() {
        setScreen(dataState(), initialSelectedPhoneNumbers = setOf("+15551234567"))

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_FAB_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that the subtitle reflects the selection count`() {
        setScreen(
            dataState(),
            initialSelectedManualEmails = setOf("a@test.com", "b@test.com"),
        )

        composeTestRule.onNodeWithText("2 contacts").assertIsDisplayed()
    }

    @Test
    fun `test that confirming reports the selected recipients`() {
        var confirmedEmails: Set<String>? = null
        var confirmedPhones: Set<String>? = null
        setScreen(
            dataState(),
            initialSelectedManualEmails = setOf("guest@test.com"),
            initialSelectedPhoneNumbers = setOf("+15551234567"),
            onInvite = { emails, phones ->
                confirmedEmails = emails
                confirmedPhones = phones
            },
        )

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_FAB_TAG).performClick()

        assertThat(confirmedEmails).containsExactly("guest@test.com")
        assertThat(confirmedPhones).containsExactly("+15551234567")
    }

    @Test
    fun `test that clicking the scan QR action invokes the callback`() {
        var scanClicked = false
        setScreen(dataState(), onScanClick = { scanClicked = true })

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_SCAN_QR_TAG).performClick()

        assertThat(scanClicked).isTrue()
    }

    @Test
    fun `test that the share link action invokes the callback`() {
        var shareClicked = false
        setScreen(dataState(), onShareLink = { shareClicked = true })

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_SHARE_LINK_TAG).performClick()

        assertThat(shareClicked).isTrue()
    }

    @Test
    fun `test that the my QR code action invokes the callback`() {
        var qrClicked = false
        setScreen(dataState(), onOpenMyQr = { qrClicked = true })

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_MY_QR_CODE_TAG).performClick()

        assertThat(qrClicked).isTrue()
    }

    @Test
    fun `test that the ongoing call confirmation dialog is shown and confirming starts the scan`() {
        var scanConfirmed = false
        setScreen(
            dataState(ongoingCallConfirmEvent = triggered),
            onScanConfirmed = { scanConfirmed = true },
        )

        composeTestRule.onNodeWithTag(INVITE_CONTACTS_OPEN_CAMERA_DIALOG_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Open").performClick()

        assertThat(scanConfirmed).isTrue()
    }

    @Test
    fun `test that the invalid code dialog is displayed when the state carries InvalidCode`() {
        setScreen(dataState(scannedContactDialog = ScannedContactDialog.InvalidCode))

        composeTestRule.onNodeWithTag(SCANNED_CONTACT_INVALID_CODE_DIALOG_TAG).assertIsDisplayed()
    }

    private fun typeRecipientAndAdd(input: String) {
        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasTestTag(INVITE_RECIPIENTS_SECTION_TAG)),
            useUnmergedTree = true,
        ).performTextInput(input)
        composeTestRule.onNodeWithTag(INVITE_RECIPIENTS_ADD_TAG).performClick()
    }

    private fun setScreen(
        state: InviteContactUiState,
        onSearchQueryChange: (String?) -> Unit = {},
        onInvite: (Set<String>, Set<String>) -> Unit = { _, _ -> },
        onSubmitManualInput: (String) -> Unit = {},
        onScanClick: () -> Unit = {},
        onScanConfirmed: () -> Unit = {},
        onShareLink: () -> Unit = {},
        onOpenMyQr: () -> Unit = {},
        initialSelectedManualEmails: Set<String> = emptySet(),
        initialSelectedPhoneNumbers: Set<String> = emptySet(),
    ) {
        composeTestRule.setContent {
            InviteContactsScreen(
                state = state,
                onSearchQueryChange = onSearchQueryChange,
                onInvite = onInvite,
                onBack = {},
                onSubmitManualInput = onSubmitManualInput,
                onScanClick = onScanClick,
                onScanConfirmed = onScanConfirmed,
                onShareLink = onShareLink,
                onOpenMyQr = onOpenMyQr,
                initialSelectedManualEmails = initialSelectedManualEmails,
                initialSelectedPhoneNumbers = initialSelectedPhoneNumbers,
            )
        }
    }

    private fun dataState(
        phoneSection: PhoneContactsSection = PhoneContactsSection.PickerAvailable(persistentListOf()),
        ongoingCallConfirmEvent: StateEvent = consumed,
        phoneContactsPickedEvent: StateEventWithContent<PickedContactData> = consumed(),
        manualEmailAcceptedEvent: StateEventWithContent<String> = consumed(),
        manualPhoneAcceptedEvent: StateEventWithContent<String> = consumed(),
        emailValidationEvent: StateEventWithContent<EmailValidationMessage> = consumed(),
        scannedContactDialog: ScannedContactDialog? = null,
    ) = InviteContactUiState.Data(
        phoneContactsSection = phoneSection,
        query = null,
        contactLink = null,
        invitationResultEvent = consumed(),
        emailValidationEvent = emailValidationEvent,
        sendSmsEvent = consumed(),
        ongoingCallConfirmEvent = ongoingCallConfirmEvent,
        phoneContactsPickedEvent = phoneContactsPickedEvent,
        manualEmailAcceptedEvent = manualEmailAcceptedEvent,
        manualPhoneAcceptedEvent = manualPhoneAcceptedEvent,
        scannedContactDialog = scannedContactDialog,
        scannedContactSelectEvent = consumed(),
        scannedContactInviteEvent = consumed(),
    )

    private fun phoneContact(
        displayName: String,
        email: String,
    ) = ContactItemUiState(
        handle = -1L,
        displayName = displayName,
        status = ContactItemStatus.Unknown,
        lastSeen = null,
        avatar = AvatarData.Initials(
            initials = displayName.first().toString(),
            avatarColor = Color.Gray,
        ),
        isVerified = false,
        email = email,
    )

    private companion object {
        const val CONTACT_ITEM_VIEW_ROW = "contact_item_view:row"
    }
}
