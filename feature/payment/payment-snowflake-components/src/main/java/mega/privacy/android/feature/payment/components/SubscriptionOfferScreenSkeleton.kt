package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaScaffold
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidTheme
import mega.android.core.ui.tokens.theme.DSTokens

/**
 * Full-screen shimmer skeleton for the subscription offer landing screen, shown while the offer is
 * loading. Per the design the banner artwork and the buy CTA are left out entirely while loading,
 * so only the dismiss affordance, the header lines and the plan card placeholder are drawn.
 *
 * It fills the available height on purpose: the screen's slide-up enter transition derives its
 * travel distance from the entering content height, so a short skeleton would slide barely at all.
 *
 * @param onDismissClick called when the dismiss (X) icon is tapped
 * @param modifier
 */
@Composable
fun SubscriptionOfferScreenSkeleton(
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MegaScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { SubscriptionOfferTopBar(onDismissClick = onDismissClick) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DSTokens.colors.background.pageBackground)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = SUBSCRIPTION_OFFER_CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(HEADER_FULL_WIDTH_LINES) {
                    SkeletonBar(Modifier.fillMaxWidth().height(16.dp))
                }
                SkeletonBar(Modifier.width(112.dp).height(16.dp))
            }
            PlanCardSkeleton(
                modifier = Modifier
                    .widthIn(max = SUBSCRIPTION_OFFER_CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private const val HEADER_FULL_WIDTH_LINES = 3

/**
 * Test tag for the full-screen subscription offer skeleton
 */
const val TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON = "subscription_offer_screen:skeleton"

@CombinedThemePreviews
@Composable
private fun SubscriptionOfferScreenSkeletonPreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        SubscriptionOfferScreenSkeleton(onDismissClick = {})
    }
}

@Preview(name = "Landscape", showBackground = true, widthDp = 740, heightDp = 360)
@Composable
private fun SubscriptionOfferScreenSkeletonLandscapePreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        SubscriptionOfferScreenSkeleton(onDismissClick = {})
    }
}
