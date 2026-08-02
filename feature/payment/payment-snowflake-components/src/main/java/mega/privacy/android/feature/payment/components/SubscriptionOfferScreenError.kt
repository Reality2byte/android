package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaScaffold
import mega.android.core.ui.components.button.SecondaryFilledButton
import mega.android.core.ui.components.state.EmptyStateView
import mega.android.core.ui.components.text.SpannableText
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidTheme
import mega.android.core.ui.tokens.theme.DSTokens
import mega.privacy.android.icon.pack.R as iconPackR
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Full-screen "Couldn't connect" state for the subscription offer landing screen, shown when the
 * device is offline or the offer could not be fetched. The buy CTA is intentionally absent: there is
 * no offer to purchase, so the only action is retrying.
 *
 * @param onRetryClick called when the "Try again" button is tapped
 * @param onDismissClick called when the dismiss (X) icon is tapped
 * @param modifier
 */
@Composable
fun SubscriptionOfferScreenError(
    onRetryClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MegaScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { SubscriptionOfferTopBar(onDismissClick = onDismissClick) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DSTokens.colors.background.pageBackground)
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateView(
                modifier = Modifier
                    .widthIn(max = SUBSCRIPTION_OFFER_CONTENT_MAX_WIDTH)
                    .padding(horizontal = 16.dp),
                imagePainter = painterResource(iconPackR.drawable.ic_no_cloud),
                title = stringResource(sharedR.string.subscription_quota_no_connection_title),
                description = SpannableText(
                    text = stringResource(sharedR.string.subscription_offer_no_connection_description),
                ),
                primaryAction = {
                    SecondaryFilledButton(
                        modifier = Modifier.testTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_RETRY),
                        text = stringResource(sharedR.string.subscription_quota_no_connection_retry_button),
                        onClick = onRetryClick,
                    )
                },
            )
        }
    }
}

/**
 * Test tag for the subscription offer screen error state
 */
const val TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_ERROR = "subscription_offer_screen:error"

/**
 * Test tag for the subscription offer screen error state retry button
 */
const val TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_RETRY = "subscription_offer_screen:retry"

@CombinedThemePreviews
@Composable
private fun SubscriptionOfferScreenErrorPreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        SubscriptionOfferScreenError(onRetryClick = {}, onDismissClick = {})
    }
}

@Preview(name = "Landscape", showBackground = true, widthDp = 740, heightDp = 360)
@Composable
private fun SubscriptionOfferScreenErrorLandscapePreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        SubscriptionOfferScreenError(onRetryClick = {}, onDismissClick = {})
    }
}
