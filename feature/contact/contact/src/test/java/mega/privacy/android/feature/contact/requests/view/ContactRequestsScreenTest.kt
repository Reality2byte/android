package mega.privacy.android.feature.contact.requests.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import mega.android.core.ui.components.contact.state.ContactItemStatus
import mega.privacy.android.domain.entity.contacts.ContactRequestAction
import mega.privacy.android.feature.contact.requests.model.ContactRequestTab
import mega.privacy.android.feature.contact.requests.model.ContactRequestUiItem
import mega.privacy.android.feature.contact.requests.model.ContactRequestsUiState
import mega.privacy.android.shared.contact.model.AvatarData
import mega.privacy.android.shared.contact.model.ContactItemUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactRequestsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `test that loading view is displayed when state is Loading`() {
        setScreen(ContactRequestsUiState.Loading)
        composeTestRule.onNodeWithTag(CONTACT_REQUESTS_LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that received requests are shown when selected tab is Received`() {
        setScreen(
            dataState(
                received = requests(isOutgoing = false, name = "Received Contact"),
                selectedTab = ContactRequestTab.Received,
            )
        )
        composeTestRule.onNodeWithTag(CONTACT_REQUESTS_LIST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Received Contact").assertIsDisplayed()
    }

    @Test
    fun `test that sent requests are shown when selected tab is Sent`() {
        setScreen(
            dataState(
                sent = requests(isOutgoing = true, name = "Sent Contact"),
                selectedTab = ContactRequestTab.Sent,
            )
        )
        composeTestRule.onNodeWithTag(CONTACT_REQUESTS_LIST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sent Contact").assertIsDisplayed()
    }

    @Test
    fun `test that empty state is shown when received tab has no requests`() {
        setScreen(dataState(received = persistentListOf(), selectedTab = ContactRequestTab.Received))
        composeTestRule.onNodeWithTag(CONTACT_REQUESTS_EMPTY_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that empty state is shown when sent tab has no requests`() {
        setScreen(dataState(sent = persistentListOf(), selectedTab = ContactRequestTab.Sent))
        composeTestRule.onNodeWithTag(CONTACT_REQUESTS_EMPTY_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tapping the sent tab invokes onTabSelected with Sent`() {
        var selected: ContactRequestTab? = null
        setScreen(
            dataState(selectedTab = ContactRequestTab.Received),
            onTabSelected = { selected = it },
        )
        composeTestRule.onNodeWithText("Sent requests")
            .performSemanticsAction(SemanticsActions.OnClick)
        assertThat(selected).isEqualTo(ContactRequestTab.Sent)
    }

    @Test
    fun `test that tapping a request row opens the actions sheet`() {
        setScreen(
            dataState(
                received = requests(isOutgoing = false),
                selectedTab = ContactRequestTab.Received,
            )
        )
        composeTestRule.onAllNodesWithTag("contact_item_view:row")[0].performClick()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTIONS_SHEET_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that received sheet shows accept decline and ignore actions`() {
        setScreen(
            dataState(
                received = requests(isOutgoing = false),
                selectedTab = ContactRequestTab.Received,
            )
        )
        composeTestRule.onAllNodesWithTag("contact_item_view:row")[0].performClick()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_ACCEPT_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_DECLINE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_IGNORE_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tapping accept invokes onItemAction with Accept`() {
        var action: ContactRequestAction? = null
        setScreen(
            dataState(
                received = requests(isOutgoing = false),
                selectedTab = ContactRequestTab.Received,
            ),
            onItemAction = { _, requestAction -> action = requestAction },
        )
        composeTestRule.onAllNodesWithTag("contact_item_view:row")[0].performClick()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_ACCEPT_TAG).performClick()
        composeTestRule.waitForIdle()
        assertThat(action).isEqualTo(ContactRequestAction.Accept)
    }

    @Test
    fun `test that sent sheet shows reinvite and remove actions`() {
        setScreen(
            dataState(
                sent = requests(isOutgoing = true),
                selectedTab = ContactRequestTab.Sent,
            )
        )
        composeTestRule.onAllNodesWithTag("contact_item_view:row")[0].performClick()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_REINVITE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_REMOVE_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tapping remove invokes onItemAction with Delete`() {
        var action: ContactRequestAction? = null
        setScreen(
            dataState(
                sent = requests(isOutgoing = true),
                selectedTab = ContactRequestTab.Sent,
            ),
            onItemAction = { _, requestAction -> action = requestAction },
        )
        composeTestRule.onAllNodesWithTag("contact_item_view:row")[0].performClick()
        composeTestRule.onNodeWithTag(CONTACT_REQUEST_ACTION_REMOVE_TAG).performClick()
        composeTestRule.waitForIdle()
        assertThat(action).isEqualTo(ContactRequestAction.Delete)
    }

    private fun setScreen(
        state: ContactRequestsUiState,
        onTabSelected: (ContactRequestTab) -> Unit = {},
        onItemAction: (ContactRequestUiItem, ContactRequestAction) -> Unit = { _, _ -> },
        onBack: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            ContactRequestsScreen(
                state = state,
                onTabSelected = onTabSelected,
                onItemAction = onItemAction,
                onBack = onBack,
            )
        }
    }

    private fun dataState(
        received: ImmutableList<ContactRequestUiItem> = persistentListOf(),
        sent: ImmutableList<ContactRequestUiItem> = persistentListOf(),
        selectedTab: ContactRequestTab = ContactRequestTab.Received,
    ) = ContactRequestsUiState.Data(
        received = received,
        sent = sent,
        selectedTab = selectedTab,
    )

    private fun requests(
        isOutgoing: Boolean,
        name: String = "Contact",
    ): ImmutableList<ContactRequestUiItem> = listOf(
        request(handle = 1L, displayName = name, isOutgoing = isOutgoing),
    ).toImmutableList()

    private fun request(
        handle: Long,
        displayName: String,
        isOutgoing: Boolean,
    ) = ContactRequestUiItem(
        handle = handle,
        isOutgoing = isOutgoing,
        contact = ContactItemUiState(
            handle = handle,
            displayName = displayName,
            status = ContactItemStatus.Unknown,
            lastSeen = null,
            avatar = AvatarData.Initials(
                initials = displayName.first().toString(),
                avatarColor = Color(0xFF2E7D32),
            ),
            isVerified = false,
            email = "$handle@test.com",
        ),
        createdTime = "2 days ago",
    )
}
