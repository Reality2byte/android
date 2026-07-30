package mega.privacy.android.feature.payment.presentation.upgrade

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class OfferExpiredDialogTest {

    @get:Rule
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setContent(onDismiss: () -> Unit = {}) = composeRule.setContent {
        OfferExpiredDialog(onDismiss = onDismiss)
    }

    @Test
    fun `test that OfferExpiredDialog shows the title, description and action`() {
        setContent()

        composeRule.onNodeWithTag(TEST_TAG_OFFER_EXPIRED_DIALOG).assertExists()
        composeRule.onNodeWithText(
            context.getString(sharedR.string.subscription_offer_expired_dialog_title)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(sharedR.string.subscription_offer_expired_dialog_description)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(sharedR.string.subscription_offer_expired_dialog_button)
        ).assertIsDisplayed()
    }

    @Test
    fun `test that OfferExpiredDialog calls onDismiss when the action is clicked`() {
        val onDismiss = mock<() -> Unit>()
        setContent(onDismiss = onDismiss)

        composeRule.onNodeWithText(
            context.getString(sharedR.string.subscription_offer_expired_dialog_button)
        ).performClick()

        verify(onDismiss).invoke()
    }
}
