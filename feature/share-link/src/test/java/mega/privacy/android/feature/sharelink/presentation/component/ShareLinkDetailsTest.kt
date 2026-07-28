package mega.privacy.android.feature.sharelink.presentation.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class ShareLinkDetailsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `test that the card and link value are displayed`() {
        setContent(link = "https://mega.nz/file/abc123")

        composeRule.onNodeWithTag(SHARE_LINK_DETAILS_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("https://mega.nz/file/abc123").assertIsDisplayed()
    }

    @Test
    fun `test that tapping the copy icon invokes onCopyLink`() {
        var copied = false
        setContent(onCopyLink = { copied = true })

        composeRule.onNodeWithTag(SHARE_LINK_DETAIL_ROW_COPY_TAG).performClick()

        assertThat(copied).isTrue()
    }

    @Test
    fun `test that the key card is hidden when no key is provided`() {
        setContent()

        composeRule.onNodeWithTag(SHARE_LINK_KEY_DETAILS_TAG).assertDoesNotExist()
    }

    @Test
    fun `test that the key card and value are displayed when a key is provided`() {
        setContent(key = "decryptionKey")

        composeRule.onNodeWithTag(SHARE_LINK_KEY_DETAILS_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("decryptionKey").assertIsDisplayed()
    }

    @Test
    fun `test that tapping the key copy icon invokes onCopyKey`() {
        var copied = false
        setContent(key = "decryptionKey", onCopyKey = { copied = true })

        composeRule.onNodeWithTag(SHARE_LINK_KEY_COPY_TAG).performClick()

        assertThat(copied).isTrue()
    }

    @Test
    fun `test that no expiry notice is shown when the link never expires`() {
        setContent(expirationTime = null)

        composeRule.onNodeWithTag(SHARE_LINK_EXPIRY_NOTICE_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(SHARE_LINK_EXPIRED_TAG).assertDoesNotExist()
    }

    @Test
    fun `test that the expiry notice shows the formatted date for a link that has not expired`() {
        setContent(expirationTime = EXPIRY_MILLIS, isExpired = false)

        composeRule.onNodeWithTag(SHARE_LINK_EXPIRY_NOTICE_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(sharedR.string.share_link_expires_on, formattedUtcDate(EXPIRY_MILLIS))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_EXPIRED_TAG).assertDoesNotExist()
    }

    @Test
    fun `test that the expired warning replaces the expiry notice for an expired link`() {
        setContent(expirationTime = EXPIRY_MILLIS, isExpired = true)

        composeRule.onNodeWithTag(SHARE_LINK_EXPIRED_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(sharedR.string.share_link_expired))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(SHARE_LINK_EXPIRY_NOTICE_TAG).assertDoesNotExist()
    }

    private fun setContent(
        link: String = "https://mega.nz/file/abc123",
        onCopyLink: () -> Unit = {},
        key: String? = null,
        onCopyKey: () -> Unit = {},
        expirationTime: Long? = null,
        isExpired: Boolean = false,
    ) {
        composeRule.setContent {
            ShareLinkDetails(
                link = link,
                onCopyLink = onCopyLink,
                key = key,
                onCopyKey = onCopyKey,
                expirationTime = expirationTime,
                isExpired = isExpired,
            )
        }
    }

    // Mirrors the component's own UTC MEDIUM formatting so the assertion is locale-independent.
    private fun formattedUtcDate(millis: Long): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))

    private companion object {
        // A fixed, far-future instant so the rendered date never depends on the clock.
        const val EXPIRY_MILLIS = 1_800_000_000_000L
    }
}
