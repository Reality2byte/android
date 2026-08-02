package mega.privacy.android.feature.payment.presentation.offer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import mega.privacy.android.feature.payment.components.TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_DISMISS
import mega.privacy.android.feature.payment.components.TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR
import mega.privacy.android.feature.payment.components.TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_RETRY
import mega.privacy.android.feature.payment.components.TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubscriptionOfferScreenTest {

    @get:Rule
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setScreen(
        state: SubscriptionOfferState,
        onRetryClick: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeRule.setContent {
            SubscriptionOfferScreen(
                uiState = state,
                onBuyClick = {},
                onDismiss = onDismiss,
                onViewAllPlansClick = {},
                onRetryClick = onRetryClick,
            )
        }
    }

    @Test
    fun `test that loading state shows the skeleton`() {
        setScreen(SubscriptionOfferState(isLoading = true))

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON).assertExists()
        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR).assertDoesNotExist()
    }

    @Test
    fun `test that the skeleton keeps the dismiss affordance available`() {
        var dismissed = false
        setScreen(
            state = SubscriptionOfferState(isLoading = true),
            onDismiss = { dismissed = true },
        )

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_DISMISS).performClick()

        assertThat(dismissed).isTrue()
    }

    @Test
    fun `test that no connection state shows the error message`() {
        setScreen(SubscriptionOfferState(isLoading = false, isConnected = false))

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR).assertExists()
        composeRule.onNodeWithText(
            composeRule.activity.getString(sharedR.string.subscription_quota_no_connection_title)
        ).assertExists()
        composeRule.onNodeWithText(
            composeRule.activity.getString(
                sharedR.string.subscription_offer_no_connection_description
            )
        ).assertExists()
    }

    @Test
    fun `test that no connection state takes precedence over the loading state`() {
        setScreen(SubscriptionOfferState(isLoading = true, isConnected = false))

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR).assertExists()
        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON).assertDoesNotExist()
    }

    @Test
    fun `test that load error state shows the error message while connected`() {
        setScreen(
            SubscriptionOfferState(isLoading = false, isConnected = true, hasLoadError = true)
        )

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR).assertExists()
        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON).assertDoesNotExist()
    }

    @Test
    fun `test that try again click invokes the retry callback`() {
        var retried = false
        setScreen(
            state = SubscriptionOfferState(isLoading = false, isConnected = false),
            onRetryClick = { retried = true },
        )

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_RETRY).performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun `test that the error state keeps the dismiss affordance available`() {
        var dismissed = false
        setScreen(
            state = SubscriptionOfferState(isLoading = false, isConnected = false),
            onDismiss = { dismissed = true },
        )

        composeRule.onNodeWithTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_DISMISS).performClick()

        assertThat(dismissed).isTrue()
    }
}
