package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaScaffold
import mega.android.core.ui.modifiers.shimmerEffect
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidTheme
import mega.android.core.ui.tokens.theme.DSTokens

/**
 * Full-screen shimmer skeleton for the subscription offer landing screen, shown while the offer is
 * loading. It mirrors the layout of [SubscriptionOfferScreenContent] (edge-to-edge banner, header
 * lines, discounted plan card and pinned buy CTA) so the enter transition has a full-height,
 * non-empty screen to animate.
 *
 * @param modifier
 */
@Composable
fun SubscriptionOfferScreenSkeleton(
    modifier: Modifier = Modifier,
) {
    MegaScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .height(BUTTON_HEIGHT)
                    .shimmerEffect(RoundedCornerShape(8.dp))
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DSTokens.colors.background.pageBackground)
                .padding(innerPadding),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BANNER_HEIGHT)
                    .shimmerEffect(RectangleShape)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(
                    modifier = Modifier
                        .width(96.dp)
                        .height(24.dp)
                        .shimmerEffect(RoundedCornerShape(12.dp))
                )
                Spacer(
                    modifier = Modifier
                        .width(220.dp)
                        .height(28.dp)
                        .shimmerEffect(RoundedCornerShape(8.dp))
                )
                Spacer(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .shimmerEffect(RoundedCornerShape(8.dp))
                )
            }
            OfferPriceCardSkeleton(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun OfferPriceCardSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DSTokens.colors.border.strong,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(
            modifier = Modifier
                .width(64.dp)
                .height(20.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )
        Spacer(
            modifier = Modifier
                .width(120.dp)
                .height(28.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )
        Spacer(
            modifier = Modifier
                .width(200.dp)
                .height(16.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )
        Spacer(
            modifier = Modifier
                .width(170.dp)
                .height(16.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )
    }
}

private val BANNER_HEIGHT = 220.dp
private val BUTTON_HEIGHT = 48.dp

/**
 * Test tag for the full-screen subscription offer skeleton
 */
const val TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_SKELETON = "subscription_offer_screen:skeleton"

@CombinedThemePreviews
@Composable
private fun SubscriptionOfferScreenSkeletonPreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        SubscriptionOfferScreenSkeleton()
    }
}
