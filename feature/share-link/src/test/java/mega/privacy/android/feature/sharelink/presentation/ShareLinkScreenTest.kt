package mega.privacy.android.feature.sharelink.presentation

import android.content.ClipDescription
import android.os.Build
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import mega.privacy.android.analytics.test.AnalyticsTestRule
import mega.privacy.mobile.analytics.event.LinkCopyAllLinksButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkCopyDecryptionKeyButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkCopyLinkButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkCopyPasswordButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkCopyrightAgreeButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkCopyrightCancelButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkCopyrightWarningDialogEvent
import mega.privacy.mobile.analytics.event.LinkHiddenItemsCancelButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkHiddenItemsContinueButtonPressedEvent
import mega.privacy.mobile.analytics.event.LinkHiddenItemsWarningDialogEvent
import mega.privacy.mobile.analytics.event.LinkShareButtonPressedEvent
import mega.privacy.mobile.analytics.event.ShareLinkScreenEvent
import mega.privacy.android.feature.sharelink.presentation.component.SHARE_LINK_DETAILS_TAG
import mega.privacy.android.feature.sharelink.presentation.component.SHARE_LINK_KEY_COPY_TAG
import mega.privacy.android.feature.sharelink.presentation.component.SHARE_LINK_KEY_DETAILS_TAG
import mega.privacy.android.feature.sharelink.presentation.component.SHARE_LINK_PASSWORD_COPY_TAG
import mega.privacy.android.feature.sharelink.presentation.component.SHARE_LINK_PASSWORD_DETAILS_TAG
import mega.privacy.android.feature.sharelink.presentation.component.SHARE_LINK_PASSWORD_PROTECTED_TAG
import mega.privacy.android.icon.pack.R as iconPackR
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareLinkScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val analyticsRule = AnalyticsTestRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val data = ShareLinkUiState.Data(
        nodeLinks = listOf(
            ShareLinkNodeItem(
                handle = 1L,
                name = "Presentation.pdf",
                isFolder = false,
                iconRes = iconPackR.drawable.ic_pdf_medium_solid,
                sizeInBytes = 10L * 1024 * 1024,
                modificationTime = 1_749_000_000L,
                childFolderCount = null,
                childFileCount = null,
                link = "https://mega.nz/file/abc123#decryptionKey",
                linkWithoutKey = "https://mega.nz/file/abc123",
                key = "decryptionKey",
            ),
        ),
        accountType = null,
    )

    private val multiNodeData = ShareLinkUiState.Data(
        nodeLinks = listOf(
            ShareLinkNodeItem(
                handle = 1L,
                name = "Documents",
                isFolder = true,
                iconRes = iconPackR.drawable.ic_folder_medium_solid,
                sizeInBytes = null,
                modificationTime = null,
                childFolderCount = 6,
                childFileCount = 12,
                link = "https://mega.nz/folder/abc123#folderKey",
                linkWithoutKey = "https://mega.nz/folder/abc123",
                key = "folderKey",
            ),
            ShareLinkNodeItem(
                handle = 2L,
                name = "Presentation.pdf",
                isFolder = false,
                iconRes = iconPackR.drawable.ic_pdf_medium_solid,
                sizeInBytes = 10L * 1024 * 1024,
                modificationTime = 1_749_000_000L,
                childFolderCount = null,
                childFileCount = null,
                link = "https://mega.nz/file/def456#fileKey",
                linkWithoutKey = "https://mega.nz/file/def456",
                key = "fileKey",
            ),
        ),
        accountType = null,
    )

    private val passwordData = data.copy(
        isPasswordSet = true,
        password = "s3cretPass",
        linkWithPassword = "https://mega.nz/#P!encryptedLink",
    )

    @Test
    fun `test that every shared node and one access banner are displayed in the multi-node state`() {
        setContent(uiState = multiNodeData)

        composeRule.onNodeWithTag(SHARE_LINK_MULTI_NODE_LIST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Documents").assertIsDisplayed()
        composeRule.onNodeWithText("Presentation.pdf").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_ACCESS_BANNER_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that folder content info is displayed for a folder node in the multi-node state`() {
        setContent(uiState = multiNodeData)

        val folderInfo = context.resources.getQuantityString(
            sharedR.plurals.info_num_folders_and_files, 6, 6,
        ) + context.resources.getQuantityString(sharedR.plurals.info_num_files, 12, 12)
        composeRule.onNodeWithText(folderInfo).assertIsDisplayed()
    }

    @Test
    fun `test that tapping a node's copy icon copies that node's link to the clipboard`() {
        val clipboard = FakeClipboard()
        setContent(uiState = multiNodeData, clipboard = clipboard)

        composeRule.onAllNodesWithContentDescription(context.getString(sharedR.string.general_copy))[0]
            .performClick()
        composeRule.waitForIdle()

        assertThat(clipboard.clipEntry?.clipData?.getItemAt(0)?.text)
            .isEqualTo(multiNodeData.nodeLinks[0].link)
    }

    @Test
    fun `test that all links are copied to the clipboard once when the multi-node screen opens`() {
        val clipboard = FakeClipboard()
        var copiedCount = 0
        setContent(
            uiState = multiNodeData,
            clipboard = clipboard,
            onLinksCopied = { copiedCount++ },
        )
        composeRule.waitForIdle()

        val expected = multiNodeData.nodeLinks.joinToString(separator = "\n") { it.link }
        assertThat(clipboard.clipEntry?.clipData?.getItemAt(0)?.text).isEqualTo(expected)
        assertThat(copiedCount).isEqualTo(1)
    }

    @Test
    fun `test that the node header, link access banner and link field are displayed in the Data state`() {
        setContent(uiState = data)

        composeRule.onNodeWithText("Presentation.pdf").assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_ACCESS_BANNER_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_DETAILS_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that the settings action is shown for a single node`() {
        setContent(uiState = data)

        composeRule.onNodeWithTag(ShareLinkSettingsAction.testTag, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun `test that the settings action is hidden for multiple nodes`() {
        val multiNode = data.copy(nodeLinks = data.nodeLinks + data.primary.copy(handle = 2L))

        setContent(uiState = multiNode)

        composeRule.onNodeWithTag(ShareLinkSettingsAction.testTag, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `test that the copyright consent screen is displayed in the CopyrightConsent state`() {
        setContent(uiState = ShareLinkUiState.CopyrightConsent)

        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(sharedR.string.copyright_screen_title))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_AGREE_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_DISAGREE_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tapping Agree invokes onCopyrightAgreed`() {
        var agreed = false
        setContent(
            uiState = ShareLinkUiState.CopyrightConsent,
            onCopyrightAgreed = { agreed = true },
        )

        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_AGREE_TAG).performClick()

        assertThat(agreed).isTrue()
    }

    @Test
    fun `test that tapping Disagree invokes onCopyrightDisagreed`() {
        var disagreed = false
        setContent(
            uiState = ShareLinkUiState.CopyrightConsent,
            onCopyrightDisagreed = { disagreed = true },
        )

        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_DISAGREE_TAG).performClick()

        assertThat(disagreed).isTrue()
    }

    @Test
    fun `test that the loading placeholder is displayed in the Loading state`() {
        setContent(uiState = ShareLinkUiState.Loading)

        composeRule.onNodeWithTag(SHARE_LINK_LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that the error message is displayed in the Error state`() {
        setContent(uiState = ShareLinkUiState.Error)

        composeRule.onNodeWithTag(SHARE_LINK_ERROR_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tapping the share button invokes onShareLink`() {
        var shared = false
        setContent(uiState = data, onShareLink = { shared = true })

        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).performClick()

        assertThat(shared).isTrue()
    }

    @Test
    fun `test that tapping the copy icon invokes onCopyLink`() {
        var copied = false
        setContent(uiState = data, onCopyLink = { copied = true })

        composeRule.onNodeWithContentDescription(context.getString(sharedR.string.general_copy))
            .performClick()

        assertThat(copied).isTrue()
    }

    @Test
    fun `test that tapping the copy icon copies the link to the clipboard`() {
        val clipboard = FakeClipboard()
        setContent(uiState = data, clipboard = clipboard)

        composeRule.onNodeWithContentDescription(context.getString(sharedR.string.general_copy))
            .performClick()
        composeRule.waitForIdle()

        assertThat(clipboard.clipEntry?.clipData?.getItemAt(0)?.text).isEqualTo(data.primary.link)
    }

    @Test
    fun `test that the key card and key-less link are displayed when the key is separate`() {
        setContent(uiState = data.copy(isKeySeparate = true))

        composeRule.onNodeWithText("https://mega.nz/file/abc123").assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_KEY_DETAILS_TAG).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("decryptionKey").assertIsDisplayed()
    }

    @Test
    fun `test that the key card is hidden when the key is not separate`() {
        setContent(uiState = data)

        composeRule.onNodeWithTag(SHARE_LINK_KEY_DETAILS_TAG).assertDoesNotExist()
    }

    @Test
    fun `test that the access banner mentions the key when the key is separate`() {
        setContent(uiState = data.copy(isKeySeparate = true))

        composeRule.onNodeWithText(
            context.resources.getQuantityString(
                sharedR.plurals.share_link_access_banner_description_with_key,
                data.handles.size,
            )
        ).assertIsDisplayed()
    }

    @Test
    fun `test that tapping the key copy icon invokes onCopyKey`() {
        var copied = false
        setContent(uiState = data.copy(isKeySeparate = true), onCopyKey = { copied = true })

        composeRule.onNodeWithTag(SHARE_LINK_KEY_COPY_TAG).performScrollTo().performClick()

        assertThat(copied).isTrue()
    }

    @Test
    fun `test that tapping the key copy icon copies the key to the clipboard`() {
        val clipboard = FakeClipboard()
        setContent(uiState = data.copy(isKeySeparate = true), clipboard = clipboard)

        composeRule.onNodeWithTag(SHARE_LINK_KEY_COPY_TAG).performScrollTo().performClick()
        composeRule.waitForIdle()

        assertThat(clipboard.clipEntry?.clipData?.getItemAt(0)?.text).isEqualTo(data.primary.key)
    }

    @Test
    fun `test that the sensitive-items warning dialog is shown for the SensitiveWarning state`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1))

        composeRule.onNodeWithTag(SHARE_LINK_SENSITIVE_WARNING_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(sharedR.string.hidden_items)).assertIsDisplayed()
    }

    @Test
    fun `test that the sensitive-items warning is absent in the Loading state`() {
        setContent(uiState = ShareLinkUiState.Loading)

        composeRule.onNodeWithTag(SHARE_LINK_SENSITIVE_WARNING_TAG).assertDoesNotExist()
    }

    @Test
    fun `test that confirming the sensitive-items warning invokes onSensitiveWarningConfirmed`() {
        var confirmed = false
        setContent(
            uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1),
            onSensitiveWarningConfirmed = { confirmed = true },
        )

        composeRule.onNodeWithText(context.getString(sharedR.string.button_continue)).performClick()

        assertThat(confirmed).isTrue()
    }

    @Test
    fun `test that cancelling the sensitive-items warning invokes onSensitiveWarningDismissed`() {
        var dismissed = false
        setContent(
            uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1),
            onSensitiveWarningDismissed = { dismissed = true },
        )

        composeRule.onNodeWithText(context.getString(sharedR.string.general_dialog_cancel_button))
            .performClick()

        assertThat(dismissed).isTrue()
    }

    @Test
    fun `test that the single-item warning uses the singular description`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1))

        composeRule.onNodeWithText(context.getString(sharedR.string.share_hidden_item_link_description))
            .assertIsDisplayed()
    }

    @Test
    fun `test that the multi-item warning uses the plural description`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 3))

        composeRule.onNodeWithText(context.getString(sharedR.string.share_hidden_item_links_description))
            .assertIsDisplayed()
    }

    @Test
    fun `test that the folder warning uses the folder description`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Folder, 1))

        composeRule.onNodeWithText(context.getString(sharedR.string.share_hidden_folder_description))
            .assertIsDisplayed()
    }

    @Test
    fun `test that the share button shares the plain link for a single node`() {
        var shared: String? = null
        setContent(uiState = data, onShareLink = { shared = it })

        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).performClick()

        assertThat(shared).isEqualTo(data.primary.link)
    }

    @Test
    fun `test that the share button shares the key-less link when the key is separate`() {
        var shared: String? = null
        setContent(uiState = data.copy(isKeySeparate = true), onShareLink = { shared = it })

        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).performClick()

        assertThat(shared).isEqualTo(data.primary.linkWithoutKey)
    }

    @Test
    fun `test that the share button shares the password link when a password is set`() {
        var shared: String? = null
        setContent(uiState = passwordData, onShareLink = { shared = it })

        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).performClick()

        assertThat(shared).isEqualTo(passwordData.linkWithPassword)
    }

    @Test
    fun `test that the share button shares every link joined by newlines for multiple nodes`() {
        var shared: String? = null
        setContent(uiState = multiNodeData, onShareLink = { shared = it })

        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).performClick()

        val expected = multiNodeData.nodeLinks.joinToString(separator = "\n") { it.link }
        assertThat(shared).isEqualTo(expected)
    }

    @Test
    fun `test that the password-protected helper and password card are displayed when a password is set`() {
        setContent(uiState = passwordData)

        composeRule.onNodeWithText(context.getString(sharedR.string.share_link_password_protected_label))
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_PROTECTED_TAG, useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_DETAILS_TAG).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `test that the password card shows the masked password`() {
        setContent(uiState = passwordData)

        val masked = "•".repeat(passwordData.password!!.length)
        composeRule.onNodeWithText(masked).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `test that the access banner uses the password description when a password is set`() {
        setContent(uiState = passwordData)

        composeRule.onNodeWithText(
            context.getString(sharedR.string.share_link_access_password_description)
        ).assertIsDisplayed()
    }

    @Test
    fun `test that the password card is hidden when no password is set`() {
        setContent(uiState = data)

        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_DETAILS_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_PROTECTED_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `test that tapping the password copy icon invokes onCopyPassword`() {
        var copied = false
        setContent(uiState = passwordData, onCopyPassword = { copied = true })

        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_COPY_TAG).performScrollTo().performClick()

        assertThat(copied).isTrue()
    }

    @Test
    fun `test that tapping the password copy icon copies the real password to the clipboard`() {
        val clipboard = FakeClipboard()
        setContent(uiState = passwordData, clipboard = clipboard)

        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_COPY_TAG).performScrollTo().performClick()
        composeRule.waitForIdle()

        assertThat(clipboard.clipEntry?.clipData?.getItemAt(0)?.text)
            .isEqualTo(passwordData.password)
    }

    @Test
    fun `test that the copied password clip is flagged sensitive`() {
        val clipboard = FakeClipboard()
        setContent(uiState = passwordData, clipboard = clipboard)

        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_COPY_TAG).performScrollTo().performClick()
        composeRule.waitForIdle()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val sensitive = clipboard.clipEntry?.clipData?.description?.extras
                ?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE)
            assertThat(sensitive).isTrue()
        }
    }

    @Test
    fun `test that the screen view event is tracked once when the screen is shown`() {
        setContent(uiState = data)

        assertThat(analyticsRule.events.filterIsInstance<ShareLinkScreenEvent>()).hasSize(1)
    }

    @Test
    fun `test that tapping Share tracks the share button event`() {
        setContent(uiState = data)

        composeRule.onNodeWithTag(SHARE_LINK_SHARE_BUTTON_TAG).performClick()

        assertThat(analyticsRule.events).contains(LinkShareButtonPressedEvent)
    }

    @Test
    fun `test that tapping a node's copy icon tracks the copy link event`() {
        setContent(uiState = data)

        composeRule.onNodeWithContentDescription(context.getString(sharedR.string.general_copy))
            .performClick()

        assertThat(analyticsRule.events).contains(LinkCopyLinkButtonPressedEvent)
    }

    @Test
    fun `test that tapping the key copy icon tracks the copy decryption key event`() {
        setContent(uiState = data.copy(isKeySeparate = true))

        composeRule.onNodeWithTag(SHARE_LINK_KEY_COPY_TAG, useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        assertThat(analyticsRule.events).contains(LinkCopyDecryptionKeyButtonPressedEvent)
    }

    @Test
    fun `test that tapping the password copy icon tracks the copy password event`() {
        setContent(uiState = passwordData)

        composeRule.onNodeWithTag(SHARE_LINK_PASSWORD_COPY_TAG, useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        assertThat(analyticsRule.events).contains(LinkCopyPasswordButtonPressedEvent)
    }

    @Test
    fun `test that opening the multi-node screen tracks the copy all links event`() {
        setContent(uiState = multiNodeData)
        composeRule.waitForIdle()

        assertThat(analyticsRule.events).contains(LinkCopyAllLinksButtonPressedEvent)
    }

    @Test
    fun `test that the copyright warning displayed event is tracked in the CopyrightConsent state`() {
        setContent(uiState = ShareLinkUiState.CopyrightConsent)

        assertThat(analyticsRule.events.filterIsInstance<LinkCopyrightWarningDialogEvent>())
            .hasSize(1)
    }

    @Test
    fun `test that the copyright warning displayed event is not tracked in the Data state`() {
        setContent(uiState = data)

        assertThat(analyticsRule.events).doesNotContain(LinkCopyrightWarningDialogEvent)
    }

    @Test
    fun `test that tapping Agree tracks the copyright agree event`() {
        setContent(uiState = ShareLinkUiState.CopyrightConsent)

        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_AGREE_TAG).performClick()

        assertThat(analyticsRule.events).contains(LinkCopyrightAgreeButtonPressedEvent)
    }

    @Test
    fun `test that tapping Cancel tracks the copyright cancel event`() {
        setContent(uiState = ShareLinkUiState.CopyrightConsent)

        composeRule.onNodeWithTag(SHARE_LINK_COPYRIGHT_DISAGREE_TAG).performClick()

        assertThat(analyticsRule.events).contains(LinkCopyrightCancelButtonPressedEvent)
    }

    @Test
    fun `test that the hidden-items warning displayed event is tracked in the SensitiveWarning state`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1))

        assertThat(analyticsRule.events.filterIsInstance<LinkHiddenItemsWarningDialogEvent>())
            .hasSize(1)
    }

    @Test
    fun `test that confirming the hidden-items warning tracks the continue event`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1))

        composeRule.onNodeWithText(context.getString(sharedR.string.button_continue)).performClick()

        assertThat(analyticsRule.events).contains(LinkHiddenItemsContinueButtonPressedEvent)
    }

    @Test
    fun `test that cancelling the hidden-items warning tracks the cancel event`() {
        setContent(uiState = ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, 1))

        composeRule.onNodeWithText(context.getString(sharedR.string.general_dialog_cancel_button))
            .performClick()

        assertThat(analyticsRule.events).contains(LinkHiddenItemsCancelButtonPressedEvent)
    }

    private fun setContent(
        uiState: ShareLinkUiState,
        onBack: () -> Unit = {},
        onOpenSettings: () -> Unit = {},
        onShareLink: (String) -> Unit = {},
        onCopyLink: () -> Unit = {},
        onCopyKey: () -> Unit = {},
        onCopyPassword: () -> Unit = {},
        onLinksCopied: () -> Unit = {},
        onSensitiveWarningConfirmed: () -> Unit = {},
        onSensitiveWarningDismissed: () -> Unit = {},
        onCopyrightAgreed: () -> Unit = {},
        onCopyrightDisagreed: () -> Unit = {},
        clipboard: Clipboard = FakeClipboard(),
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalClipboard provides clipboard) {
                ShareLinkScreen(
                    uiState = uiState,
                    onBack = onBack,
                    onOpenSettings = onOpenSettings,
                    onShareLink = onShareLink,
                    onCopyLink = onCopyLink,
                    onCopyKey = onCopyKey,
                    onCopyPassword = onCopyPassword,
                    onLinksCopied = onLinksCopied,
                    onSensitiveWarningConfirmed = onSensitiveWarningConfirmed,
                    onSensitiveWarningDismissed = onSensitiveWarningDismissed,
                    onCopyrightAgreed = onCopyrightAgreed,
                    onCopyrightDisagreed = onCopyrightDisagreed,
                )
            }
        }
    }

    private class FakeClipboard : Clipboard {
        var clipEntry: ClipEntry? = null
            private set

        override suspend fun getClipEntry(): ClipEntry? = clipEntry

        override suspend fun setClipEntry(clipEntry: ClipEntry?) {
            this.clipEntry = clipEntry
        }

        override val nativeClipboard: NativeClipboard
            get() = throw UnsupportedOperationException("Not used in tests")
    }
}
